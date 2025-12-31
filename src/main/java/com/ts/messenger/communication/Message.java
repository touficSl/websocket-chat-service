package com.ts.messenger.communication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;

import com.ts.services.NCICustomerService;
import com.ts.utils.NCIResponse;
import com.ts.messenger.authentication.Credentials;
import com.ts.messenger.utils.Constants;
import com.ts.messenger.utils.Utils;

public class Message {

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
	
	public Message fillData(String msgStr, String contentStatus) {

		try {
			Message message = new Message();	
			message.setErrorMessage(null);
			JSONObject json = new JSONObject(msgStr);
			
			/* Required fields */
			if (
					!json.has("chatSecurityToken") || 
					!json.has("customerId") ||
					!json.has("hotelId") ||
					!json.has("sender") ||
					!json.has("senderType") ||
					!json.has("receiver") ||
					!json.has("content") ||
					!json.has("contentType") ||
					!json.has("roomNumber") ||
					!json.has("hotelTimezone") ||
					!json.has("msgid")
				) {
				message.setErrorMessage("Missing required fields.");
				return message;
			}
			
			/* Enumeration checks 
			 * System don't accept sederType = SYSTEM only HOTEL OR GUEST */
			if ( SenderType.findByName(json.getString("senderType")) == null || json.getString("senderType").equalsIgnoreCase(SenderType.SYSTEM.name()) ) {
				message.setErrorMessage("Invalid sendetType.");
				return message;
			}
			
			if ( ContentType.findByName(json.getString("contentType")) == null ) {
				message.setErrorMessage("Invalid contentType.");
				return message;
			}
			
			if ( MessageStatus.findByName(contentStatus) == null ) {
				message.setErrorMessage("Invalid contentStatus.");
				return message;
			}

			/* Fill data */
			message.setChatSecurityToken(json.getString("chatSecurityToken"));
			message.setCustomerId(json.getString("customerId"));
			message.setHotelId(json.getString("hotelId"));
			message.setSender(json.getString("sender"));
			message.setSenderType(json.getString("senderType"));
			message.setReceiver(json.getString("receiver"));
			message.setContent(json.getString("content"));
			message.setContentType(json.getString("contentType"));
			message.setRoomNumber(json.getString("roomNumber"));
			message.setHotelTimezone(json.getString("hotelTimezone"));
			message.setMsgid(json.getString("msgid"));

			/* Not required fields*/
			if (json.has("reservationNumber"))
				message.setReservationNumber(json.getString("reservationNumber"));
			if (json.has("roomTypeCode"))
				message.setRoomTypeCode(json.getString("roomTypeCode"));
			
			message.setContentStatus(contentStatus);
			
			/* Create chat identifier */

			message.setGuest(message.getSenderType().equals(SenderType.GUEST.name()) ? true : false);
			
			String userChatId = message.isGuest() ? message.getSender() : message.getReceiver();
			message.setChatIdentifier(message.getCustomerId() + "." +  message.getHotelId() + "." + message.getRoomNumber() + "." + userChatId);
			
			/* customerId check */
			NCIResponse nciResponse = NCICustomerService.getCustomerByCustomerId(message.getCustomerId(), Credentials.isDebugMode(), Credentials.getCommonDatabaseUrl(), Credentials.getCommonDatabaseUser(), Credentials.getCommonDatabaseDriver(), Credentials.getCommonDatabasePassword(), Credentials.getCommonDatabaseCatalog());
			if ( !nciResponse.isSuccess() ) {
				message.setErrorMessage("Invalid customerId.");
				return message;
			}
			
			return message;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Message fillDateTime() {
		Date currentDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DateFormat),
				simpleTimeFormat = new SimpleDateFormat(Constants.TimeFormat);
		String date, time;
    	try {
			if (this.getHotelTimezone() != null && !this.getHotelTimezone().trim().equalsIgnoreCase(""))
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone(this.getHotelTimezone()));
			
			if (this.getHotelTimezone() != null && !this.getHotelTimezone().trim().equalsIgnoreCase(""))
				simpleTimeFormat.setTimeZone(TimeZone.getTimeZone(this.getHotelTimezone()));
			
			date = simpleDateFormat.format(currentDate);
			time = simpleTimeFormat.format(currentDate);
			
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printLogMessage("Message.fillDateTime", "Timezone error");
			date = simpleDateFormat.format(currentDate);
			time = simpleTimeFormat.format(currentDate);
		}
		this.setDate(date);
		this.setTime(time);
		return this;
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