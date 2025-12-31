package com.ts.messenger.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.ts.messenger.authentication.Interceptor;
import com.ts.messenger.communication.Message;
import com.ts.messenger.communication.Message.MessageStatus;
import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.entities.MessageEntity;
import com.ts.messenger.multi.tenant.RequestContext;
import com.ts.messenger.services.GuestService;
import com.ts.messenger.services.HotelService;
import com.ts.messenger.services.MessageService;
import com.ts.messenger.utils.Utils;

@Controller
public class MessagingController {
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	private GuestService guestService;
	
	@Autowired
	Interceptor interceptor;
	
	@Autowired	
	HotelService hotelService;
	
	@Autowired
	MessageService messageService;

	/** synchronized added to get messages in the right order **/
	@MessageMapping("/sendmsg")
	public void sendmsg(
			SimpMessageHeaderAccessor headerAccessor, 
			String messageStr) {
		
		Utils.printLogMessage("MessagingController.greeting", "[/sendmsg] Message: " + messageStr);
		
		try {

			/* Fill and validate message fields */
			Message message = new Message().fillData(messageStr, MessageStatus.SENT.name());
			if (message.getErrorMessage() != null) {
				Utils.printLogMessage("MessagingController.greeting", "[/sendmsg] Error Message: " + message.getErrorMessage());
				sendPrivateMessage(message);
				return;
			}
			
			/* Assign db customer */
			RequestContext.setCustomerId(message.getCustomerId());

			message = message.fillDateTime();
		
			// Check Credentials
			GuestRoomEntity guestRoomEntity = guestService.checkIfExist(message.getRoomNumber(), message.getHotelId());
			
			if (guestRoomEntity == null) {
				message.setErrorMessage("No chat available for this guest room.");
				sendPrivateMessage(message);
				return;
			
			} else {

				if (!guestRoomEntity.getSecurityToken().equals(message.getChatSecurityToken())) {
					message.setErrorMessage("Invalid chatSecurityToken");
					Utils.printLogMessage("MessagingController.greeting", "[/sendmsg] Error Message: " + message.getErrorMessage());
					sendPrivateMessage(message);
					return;
				}
				
				/**
				   * When any hotel open his chat view, SYSTEM will display all guests who have hotel_username = {hotel_session} + all NULL hotel_username 
				   * When someone contact this guest_username, SYSTEM will allocate this chat to this hotel by updating the hotel_username = {hotel_session} 
				   * I will add a feature in the hotel view to assign a chat to another hotel or put it NULL for all
				   * Hotel_username = utilisateur.username (utilisateur table)

				 */
				
				if (!message.isGuest() && guestRoomEntity.getHotelUsername() == null) {
					GuestRoomEntity gre = guestService.addHotel(guestRoomEntity, message.getSender());
					sendHotelPrivateMessage(message.getHotelId(), "refresh_guest_list");
				}
				else if (!message.isGuest() && !guestRoomEntity.getHotelUsername().equalsIgnoreCase(message.getSender())) {
					message.setErrorMessage("Hotel access denied.");
					sendPrivateMessage(message);
					return;
				}
				else if (message.isGuest() && !guestRoomEntity.getGuestUsername().equalsIgnoreCase(message.getSender())) {
					message.setErrorMessage("Guest access denied.");
					sendPrivateMessage(message);
					return;
				}
			}
			
			/* If guest room exist, prepare and send message */
			message.setReceiver(message.isGuest() ? guestRoomEntity.getHotelUsername() : guestRoomEntity.getGuestUsername());
			
			sendPrivateMessage(message);
			MessageEntity msgEntity = new MessageEntity(message, guestRoomEntity);
			messageService.save(msgEntity);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public synchronized void sendHotelPrivateMessage(String hotelId, String actionKey) {
		JSONObject response = new JSONObject();
		response.put("actionKey", actionKey);
		simpMessagingTemplate.convertAndSend(String.format(privateEndpoint, hotelId), response.toString());
	}


	private String privateEndpoint = "/topic/private/%s";
	
	public synchronized void sendPrivateMessage(Message message) {
		String response = new JSONObject(message).toString();
		simpMessagingTemplate.convertAndSend(String.format(privateEndpoint, message.getChatIdentifier()), response);
	}

	/* Might be used later */
//	private String channelEndpoint = "/topic/channel";
//	private String userEndpoint = "/topic/user/%s";
//	public synchronized void sendUserMessage(Message message, int userId) {
//		String response = new JSONObject(message).toString();
//		simpMessagingTemplate.convertAndSend(String.format(userEndpoint, userId + ""), response);
//	}
//	
//	public synchronized void sendChannelMessage(Message message) {
//		String response = new JSONObject(message).toString();
//		simpMessagingTemplate.convertAndSend(channelEndpoint, response);
//	}

}