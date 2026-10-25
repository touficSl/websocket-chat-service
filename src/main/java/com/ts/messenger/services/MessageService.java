package com.ts.messenger.services;

import java.util.List;

import com.ts.messenger.entities.MessageEntity;

public interface MessageService {

	public MessageEntity save(MessageEntity message);

	public List<MessageEntity> chatMessages(String guestUserName, String hotelId, String roomNumber, int fromRow, int toRow);

	public long countChatMessages(String guestUserName, String hotelId, String roomNumber);

	public int updateMessageStatus(String roomNumber, String receiver, int guestRoomId, String status);
}
