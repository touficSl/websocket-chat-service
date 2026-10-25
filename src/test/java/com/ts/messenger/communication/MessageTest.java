package com.ts.messenger.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class MessageTest {

	private static JSONObject valid() {
		JSONObject json = new JSONObject();
		json.put("chatSecurityToken", "token-123");
		json.put("customerId", "cust");
		json.put("hotelId", "H1");
		json.put("sender", "john");
		json.put("senderType", "GUEST");
		json.put("receiver", "Hotel Staff");
		json.put("content", "Need extra towels");
		json.put("contentType", "TYPING");
		json.put("roomNumber", "101");
		json.put("hotelTimezone", "Asia/Dubai");
		json.put("msgid", "msgid.Ab12Cd34Ef");
		return json;
	}

	private static Message parse(JSONObject json) {
		return new Message().fillData(json.toString(), "SENT");
	}

	@Test
	void validMessageIsAccepted() {
		Message message = parse(valid());
		assertNull(message.getErrorMessage());
		assertTrue(message.isGuest());
		assertEquals("GUEST", message.getSenderType());
	}

	@Test
	void invalidJsonNeverReturnsNull() {
		Message message = new Message().fillData("{not json", "SENT");
		assertNotNull(message);
		assertNotNull(message.getErrorMessage());
	}

	@Test
	void missingFieldIsRejected() {
		JSONObject json = valid();
		json.remove("chatSecurityToken");
		assertEquals("Missing required fields.", parse(json).getErrorMessage());
	}

	@Test
	void systemSenderIsRejected() {
		JSONObject json = valid();
		json.put("senderType", "SYSTEM");
		assertNotNull(parse(json).getErrorMessage());
	}

	@Test
	void unsafeImageUrlsAreRejected() {
		for (String url : new String[] { "javascript:alert(1)", "https://x/a.png') ; background:url('//evil", "data:image/png;base64,AAA", "https://x/<img>" }) {
			JSONObject json = valid();
			json.put("contentType", "IMAGE");
			json.put("content", url);
			assertNotNull(parse(json).getErrorMessage(), url);
		}

		JSONObject json = valid();
		json.put("contentType", "IMAGE");
		json.put("content", "https://hotel.example.com/portal/demo/img/messenger/messengerimg_1.png");
		assertNull(parse(json).getErrorMessage());
	}

	@Test
	void oversizedContentIsRejected() {
		JSONObject json = valid();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= Message.MAX_CONTENT_LENGTH; i++) sb.append('a');
		json.put("content", sb.toString());
		assertNotNull(parse(json).getErrorMessage());
	}

	@Test
	void controlCharactersInNamesAreRejected() {
		JSONObject json = valid();
		json.put("sender", "john\u0000");
		assertNotNull(parse(json).getErrorMessage());
		assertFalse(Message.isValidName(""));
	}

	@Test
	void clientJsonNeverContainsTheChatToken() {
		Message message = parse(valid());
		JSONObject json = new JSONObject(message.toClientJson());
		assertFalse(json.has("chatSecurityToken"));
		assertEquals("msgid.Ab12Cd34Ef", json.getString("msgid"));
	}
}
