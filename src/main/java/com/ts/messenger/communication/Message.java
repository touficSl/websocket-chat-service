package com.ts.messenger.communication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.ts.messenger.utils.Constants;
import com.ts.messenger.utils.Utils;

public class Message {

	/* Input limits */
	public static final int MAX_CONTENT_LENGTH = 20000;
	public static final int MAX_NAME_LENGTH = 255;
	public static final int MAX_IMAGE_URL_LENGTH = 2048;

	private static final Pattern MSG_ID = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");
	/* Only plain http(s) URLs without characters that could break out of an attribute / CSS context */
	private static final Pattern SAFE_IMAGE_URL = Pattern.compile("^https?://[^\\s'\"()<>\\\\`]+$");

	private String customerId;
	private String chatSecurityToken;

	private String hotelId;

	private String sender;
	private String senderType;
	private String receiver;

	private String content;
	private String contentType;
	private String contentStatus;

	private String date;
	private String time;

	private String roomNumber;
	private String roomTypeCode;
	private String reservationNumber;

	/* true: Guest, false: hotel */
	private boolean isGuest;
	private String chatIdentifier;

	/* if not null - return errorMessage */
	private String errorMessage;

	private String hotelTimezone;

	private String msgid;

	public enum SenderType {
		GUEST, HOTEL, SYSTEM;

		public static SenderType findByName(String name) {
			SenderType result = null;
		    for (SenderType senderType : values()) {
		        if (senderType.name().equalsIgnoreCase(name)) {
		            result = senderType;
		            break;
		        }
		    }
		    return result;
		}
	}

	public enum ContentType {
		TYPING, IMAGE;	// , VOICE, URL;

		public static ContentType findByName(String name) {
			ContentType result = null;
		    for (ContentType contentType : values()) {
		        if (contentType.name().equalsIgnoreCase(name)) {
		            result = contentType;
		            break;
		        }
		    }
		    return result;
		}
	}

	public enum MessageStatus {
		SENT, DELIVERED, SEEN;

		public static MessageStatus findByName(String name) {
			MessageStatus result = null;
		    for (MessageStatus messageStatus : values()) {
		        if (messageStatus.name().equalsIgnoreCase(name)) {
		            result = messageStatus;
		            break;
		        }
		    }
		    return result;
		}
	}

	public Message() {}

	/**
	 * Parses and validates a message received from a client.
	 * Never returns null, check getErrorMessage() before using it.
	 */
	public Message fillData(String msgStr, String contentStatus) {

		Message message = new Message();
		message.setErrorMessage(null);

		try {
			JSONObject json = new JSONObject(msgStr);

			/* Required fields */
			String[] required = { "chatSecurityToken", "customerId", "hotelId", "sender", "senderType", "content", "contentType", "roomNumber", "msgid" };
			for (String field : required) {
				if (!Utils.jsonContainsValue(json, field)) {
					message.setErrorMessage("Missing required fields.");
					return message;
				}
			}

			/* Enumeration checks
			 * System don't accept sederType = SYSTEM only HOTEL OR GUEST */
			SenderType senderType = SenderType.findByName(json.getString("senderType"));
			if (senderType == null || senderType == SenderType.SYSTEM) {
				message.setErrorMessage("Invalid senderType.");
				return message;
			}

			ContentType contentType = ContentType.findByName(json.getString("contentType"));
			if (contentType == null) {
				message.setErrorMessage("Invalid contentType.");
				return message;
			}

			if (MessageStatus.findByName(contentStatus) == null) {
				message.setErrorMessage("Invalid contentStatus.");
				return message;
			}

			/* Fill data */
			message.setChatSecurityToken(json.getString("chatSecurityToken"));
			message.setCustomerId(json.getString("customerId"));
			message.setHotelId(json.getString("hotelId"));
			message.setSender(json.getString("sender"));
			message.setSenderType(senderType.name());
			message.setReceiver(json.optString("receiver", null));
			message.setContent(json.getString("content"));
			message.setContentType(contentType.name());
			message.setRoomNumber(json.getString("roomNumber"));
			message.setMsgid(json.getString("msgid"));

			/* Not required fields*/
			message.setReservationNumber(trimToLength(json.optString("reservationNumber", null), MAX_NAME_LENGTH));
			message.setRoomTypeCode(trimToLength(json.optString("roomTypeCode", null), 32));

			message.setContentStatus(MessageStatus.findByName(contentStatus).name());
			message.setGuest(senderType == SenderType.GUEST);

			/* Size / format checks */
			if (message.getContent().trim().isEmpty() || message.getContent().length() > MAX_CONTENT_LENGTH) {
				message.setErrorMessage("Invalid content.");
				return message;
			}
			if (contentType == ContentType.IMAGE
					&& (message.getContent().length() > MAX_IMAGE_URL_LENGTH || !SAFE_IMAGE_URL.matcher(message.getContent()).matches())) {
				message.setErrorMessage("Invalid image.");
				return message;
			}
			if (!isValidName(message.getSender()) || !isValidName(message.getRoomNumber()) || !isValidName(message.getHotelId())) {
				message.setErrorMessage("Invalid message fields.");
				return message;
			}
			if (!MSG_ID.matcher(message.getMsgid()).matches()) {
				message.setErrorMessage("Invalid msgid.");
				return message;
			}

		} catch (Exception e) {
			message.setErrorMessage("Invalid message.");
		}

		return message;
	}

	public static boolean isValidName(String value) {
		if (value == null || value.trim().isEmpty() || value.length() > MAX_NAME_LENGTH)
			return false;
		for (int i = 0; i < value.length(); i++) {
			if (Character.isISOControl(value.charAt(i)))
				return false;
		}
		return true;
	}

	private static String trimToLength(String value, int max) {
		if (value == null)
			return null;
		return value.length() > max ? value.substring(0, max) : value;
	}

	public Message fillDateTime() {
		Date currentDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat),
				simpleTimeFormat = new SimpleDateFormat(Constants.TimeFormat);

		if (this.getHotelTimezone() != null && !this.getHotelTimezone().trim().isEmpty() && !"-1".equals(this.getHotelTimezone())) {
			TimeZone timeZone = TimeZone.getTimeZone(this.getHotelTimezone());
			simpleDateFormat.setTimeZone(timeZone);
			simpleTimeFormat.setTimeZone(timeZone);
		}

		this.setDate(simpleDateFormat.format(currentDate));
		this.setTime(simpleTimeFormat.format(currentDate));
		return this;
	}

	/** JSON published to the chat subscribers. Secrets (chatSecurityToken) are never included. */
	public String toClientJson() {
		JSONObject json = new JSONObject();
		json.put("customerId", customerId);
		json.put("hotelId", hotelId);
		json.put("sender", sender);
		json.put("senderType", senderType);
		json.put("receiver", receiver);
		json.put("content", content);
		json.put("contentType", contentType);
		json.put("contentStatus", contentStatus);
		json.put("date", date);
		json.put("time", time);
		json.put("roomNumber", roomNumber);
		json.put("roomTypeCode", roomTypeCode);
		json.put("reservationNumber", reservationNumber);
		json.put("guest", isGuest);
		json.put("chatIdentifier", chatIdentifier);
		json.put("errorMessage", errorMessage);
		json.put("hotelTimezone", hotelTimezone);
		json.put("msgid", msgid);
		return json.toString();
	}

	/** Topic identifier of a chat: customerId.hotelId.roomNumber.guestUsername */
	public static String buildChatIdentifier(String customerId, String hotelId, String roomNumber, String guestUsername) {
		return customerId + "." + hotelId + "." + roomNumber + "." + guestUsername;
	}


	/* Getters and Setters */

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public String getReservationNumber() {
		return reservationNumber;
	}

	public void setReservationNumber(String reservationNumber) {
		this.reservationNumber = reservationNumber;
	}

	public String getHotelId() {
		return hotelId;
	}

	public void setHotelId(String hotelId) {
		this.hotelId = hotelId;
	}

	public String getSenderType() {
		return senderType;
	}

	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}

	public String getChatSecurityToken() {
		return chatSecurityToken;
	}

	public void setChatSecurityToken(String chatSecurityToken) {
		this.chatSecurityToken = chatSecurityToken;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		if (errorMessage != null)
			this.setSenderType(SenderType.SYSTEM.name());
		this.errorMessage = errorMessage;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentStatus() {
		return contentStatus;
	}

	public void setContentStatus(String contentStatus) {
		this.contentStatus = contentStatus;
	}

	public String getChatIdentifier() {
		return chatIdentifier;
	}

	public void setChatIdentifier(String chatIdentifier) {
		this.chatIdentifier = chatIdentifier;
	}

	public boolean isGuest() {
		return isGuest;
	}

	public void setGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}

	public String getRoomTypeCode() {
		return roomTypeCode;
	}

	public void setRoomTypeCode(String roomTypeCode) {
		this.roomTypeCode = roomTypeCode;
	}

	public String getHotelTimezone() {
		return hotelTimezone;
	}

	public void setHotelTimezone(String hotelTimezone) {
		this.hotelTimezone = hotelTimezone;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}
}
