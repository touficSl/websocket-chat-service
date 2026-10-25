package com.ts.messenger.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ts.messenger.entities.MessageEntity;
import com.ts.messenger.repositories.MessageRepository;

@Service("messageService")
public class MessageServiceImpl implements MessageService {

	@Autowired
	private MessageRepository messageRepository;

	/** synchronized added to prevent deadlock problems **/
	@Override
	public synchronized MessageEntity save(MessageEntity message) {
		return messageRepository.save(message);
	}

	@Override
	public List<MessageEntity> chatMessages(String guestUserName, String hotelId, String roomNumber, int fromRow, int toRow) {

		return messageRepository.chatMessages(guestUserName, hotelId, roomNumber, PageRequest.of(fromRow, toRow));
	}

	@Override
	public long countChatMessages(String guestUserName, String hotelId, String roomNumber) {
		return messageRepository.countChatMessages(guestUserName, hotelId, roomNumber);
	}

	@Override
	public int updateMessageStatus(String roomNumber, String receiver, int guestRoomId, String status) {
		return messageRepository.updateMessageStatus(roomNumber, receiver, guestRoomId, status);
	}

}
