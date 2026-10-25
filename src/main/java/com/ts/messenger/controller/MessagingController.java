package com.ts.messenger.controller;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.ts.messenger.communication.Message;
import com.ts.messenger.communication.Message.MessageStatus;
import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.entities.MessageEntity;
import com.ts.messenger.multi.tenant.RequestContext;
import com.ts.messenger.services.GuestService;
import com.ts.messenger.services.HotelService;
import com.ts.messenger.services.MessageService;
import com.ts.messenger.services.UserService;
import com.ts.messenger.utils.SecureTokens;
import com.ts.messenger.ws.StompAuthChannelInterceptor;

@Controller
public class MessagingController {

	private static final Logger LOG = LoggerFactory.getLogger(MessagingController.class);

	private static final String PRIVATE_ENDPOINT = "/topic/private/%s";
	private static final String ERRORS_QUEUE = "/queue/errors";

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	private GuestService guestService;

	@Autowired
	HotelService hotelService;

	@Autowired
	MessageService messageService;

	@Autowired
	UserService userService;

	@MessageMapping("/sendmsg")
	public void sendmsg(
			SimpMessageHeaderAccessor headerAccessor,
			String messageStr) {

		String sessionId = headerAccessor.getSessionId();

		try {

			/* Fill and validate message fields */
			Message message = new Message().fillData(messageStr, MessageStatus.SENT.name());
			if (message.getErrorMessage() != null) {
				sendError(sessionId, message);
				return;
			}

			/* The message must target the customer the WebSocket session was authenticated for */
			Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
			String sessionCustomerId = sessionAttributes != null ? (String) sessionAttributes.get(StompAuthChannelInterceptor.SESSION_CUSTOMER_ID) : null;
			if (sessionCustomerId == null || !sessionCustomerId.equals(message.getCustomerId())) {
				message.setErrorMessage("Invalid customerId.");
				sendError(sessionId, message);
				return;
			}

			/* Assign db customer */
			RequestContext.setCustomerId(message.getCustomerId());

			/* Timezone comes from the hotel configuration, not from the client */
			message.setHotelTimezone(hotelService.getHotelTimezone(message.getHotelId()));
			message = message.fillDateTime();

			// Check Credentials
			GuestRoomEntity guestRoomEntity = guestService.checkIfExist(message.getRoomNumber(), message.getHotelId());

			if (guestRoomEntity == null) {
				message.setErrorMessage("No chat available for this guest room.");
				sendError(sessionId, message);
				return;
			}

			if (!SecureTokens.equals(guestRoomEntity.getSecurityToken(), message.getChatSecurityToken())) {
				message.setErrorMessage("Invalid chatSecurityToken");
				sendError(sessionId, message);
				return;
			}

			/**
			   * When any hotel open his chat view, SYSTEM will display all guests who have hotel_username = {hotel_session} + all NULL hotel_username
			   * When someone contact this guest_username, SYSTEM will allocate this chat to this hotel by updating the hotel_username = {hotel_session}
			   * I will add a feature in the hotel view to assign a chat to another hotel or put it NULL for all
			   * Hotel_username = utilisateur.username (utilisateur table)
			 */
			if (!message.isGuest()) {

				/* Hotel sender must be an existing staff user */
				if (userService.getByUserLogin(message.getSender()) == null) {
					message.setErrorMessage("Hotel access denied.");
					sendError(sessionId, message);
					return;
				}

				if (guestRoomEntity.getHotelUsername() == null) {
					GuestRoomEntity assigned = guestService.addHotel(guestRoomEntity, message.getSender());
					if (assigned != null)
						guestRoomEntity = assigned;
					sendHotelPrivateMessage(message.getHotelId(), "refresh_guest_list");
				}
				else if (!guestRoomEntity.getHotelUsername().equalsIgnoreCase(message.getSender())) {
					message.setErrorMessage("Hotel access denied.");
					sendError(sessionId, message);
					return;
				}
			}
			else if (!guestRoomEntity.getGuestUsername().equalsIgnoreCase(message.getSender())) {
				message.setErrorMessage("Guest access denied.");
				sendError(sessionId, message);
				return;
			}

			/* If guest room exist, prepare and send message. Topic is always derived from the stored chat. */
			message.setReceiver(message.isGuest() ? guestRoomEntity.getHotelUsername() : guestRoomEntity.getGuestUsername());
			message.setChatIdentifier(Message.buildChatIdentifier(message.getCustomerId(), message.getHotelId(), guestRoomEntity.getRoomNumber(), guestRoomEntity.getGuestUsername()));

			/* Persist first so a delivered status always means stored */
			messageService.save(new MessageEntity(message, guestRoomEntity));
			sendPrivateMessage(message);

		} catch (Exception e) {
			LOG.error("Unable to process chat message", e);
			Message error = new Message();
			error.setErrorMessage("Message could not be sent, please try again.");
			sendError(sessionId, error);
		} finally {
			RequestContext.clear();
		}
	}


	public synchronized void sendHotelPrivateMessage(String hotelId, String actionKey) {
		JSONObject response = new JSONObject();
		response.put("actionKey", actionKey);
		simpMessagingTemplate.convertAndSend(String.format(PRIVATE_ENDPOINT, hotelId), response.toString());
	}

	public synchronized void sendPrivateMessage(Message message) {
		simpMessagingTemplate.convertAndSend(String.format(PRIVATE_ENDPOINT, message.getChatIdentifier()), message.toClientJson());
	}

	/** Errors are only delivered to the WebSocket session that caused them */
	private void sendError(String sessionId, Message message) {
		if (sessionId == null)
			return;
		LOG.debug("Rejected chat message: {}", message.getErrorMessage());
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
		headers.setSessionId(sessionId);
		headers.setLeaveMutable(true);
		simpMessagingTemplate.convertAndSendToUser(sessionId, ERRORS_QUEUE, message.toClientJson(), headers.getMessageHeaders());
	}

}
