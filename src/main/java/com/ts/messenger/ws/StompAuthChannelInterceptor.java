package com.ts.messenger.ws;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.ts.messenger.authentication.Interceptor;
import com.ts.messenger.entities.GuestRoomEntity;
import com.ts.messenger.multi.tenant.RequestContext;
import com.ts.messenger.services.GuestService;
import com.ts.messenger.utils.Constants;

/**
 * Secures the STOMP endpoint:
 *  - CONNECT must carry a valid "authorization" bearer token issued for "customerId"
 *  - SUBSCRIBE to a chat topic requires the chat security token of that chat
 *  - SUBSCRIBE to a hotel channel requires a hotel authorized for the messenger service
 *  - SEND is only allowed to the application destination, clients can never publish to the broker directly
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(StompAuthChannelInterceptor.class);

	public static final String SESSION_CUSTOMER_ID = "messenger.customerId";

	public static final String PRIVATE_TOPIC_PREFIX = "/topic/private/";
	public static final String ERRORS_QUEUE = "/user/queue/errors";
	public static final String SEND_DESTINATION = "/app/sendmsg";

	@Autowired
	@Lazy
	private Interceptor interceptor;

	@Autowired
	@Lazy
	private GuestService guestService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {

		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null)
			return message;

		StompCommand command = accessor.getCommand();
		Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

		if (command == StompCommand.CONNECT || command == StompCommand.STOMP) {
			String customerId = accessor.getFirstNativeHeader("customerId");
			int auth = interceptor.authenticateCustomer(accessor.getFirstNativeHeader("authorization"), customerId);
			if (auth != Constants.SUCCESS || sessionAttributes == null)
				throw new MessagingException("Authentication error");
			sessionAttributes.put(SESSION_CUSTOMER_ID, customerId);
			return message;
		}

		if (command != StompCommand.SUBSCRIBE && command != StompCommand.SEND)
			return message;

		String customerId = sessionAttributes != null ? (String) sessionAttributes.get(SESSION_CUSTOMER_ID) : null;
		if (customerId == null) {
			LOG.warn("Rejected {} from an unauthenticated session", command);
			return null;
		}

		String destination = accessor.getDestination();
		if (destination == null)
			return null;

		if (command == StompCommand.SEND)
			return SEND_DESTINATION.equals(destination) ? message : reject(command, destination);

		/* SUBSCRIBE */
		if (ERRORS_QUEUE.equals(destination))
			return message;

		if (!destination.startsWith(PRIVATE_TOPIC_PREFIX))
			return reject(command, destination);

		try {
			RequestContext.setCustomerId(customerId);
			return isSubscriptionAllowed(accessor, customerId, destination) ? message : reject(command, destination);
		} catch (Exception e) {
			LOG.warn("Subscription check failed: {}", e.getMessage());
			return null;
		} finally {
			RequestContext.clear();
		}
	}

	private boolean isSubscriptionAllowed(StompHeaderAccessor accessor, String customerId, String destination) {

		String chatSecurityToken = accessor.getFirstNativeHeader("chatSecurityToken");

		/* Hotel channel: /topic/private/{hotelId} */
		if (chatSecurityToken == null) {
			String hotelId = destination.substring(PRIVATE_TOPIC_PREFIX.length());
			return !hotelId.isEmpty() && interceptor.checkHotelAuthorization(hotelId) == Constants.SUCCESS;
		}

		/* Chat topic: /topic/private/{customerId}.{hotelId}.{roomNumber}.{guestUsername} */
		String hotelId = accessor.getFirstNativeHeader("hotelId");
		String roomNumber = accessor.getFirstNativeHeader("roomNumber");
		String guestUsername = accessor.getFirstNativeHeader("guestUsername");
		if (hotelId == null || roomNumber == null || guestUsername == null)
			return false;

		GuestRoomEntity guestRoom = guestService.findAuthorizedChat(roomNumber, hotelId, guestUsername, chatSecurityToken);
		if (guestRoom == null)
			return false;

		String expected = PRIVATE_TOPIC_PREFIX + com.ts.messenger.communication.Message.buildChatIdentifier(customerId, hotelId, guestRoom.getRoomNumber(), guestRoom.getGuestUsername());
		return expected.equals(destination);
	}

	private Message<?> reject(StompCommand command, String destination) {
		LOG.warn("Rejected {} to {}", command, destination);
		return null;
	}
}
