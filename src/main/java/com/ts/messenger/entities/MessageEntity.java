package com.ts.messenger.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.ts.messenger.communication.Message;
import com.ts.messenger.utils.AESencrp;

@Entity
@Table(name="messenger_message")
public class MessageEntity  implements Comparable{

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="content")
	private String content;
	
	@Column(name="content_type")
	private String contentType;
	
	@Column(name="sender")
	private String sender; 
	
	@ManyToOne
	@JoinColumn(name = "guest_room_id", nullable = false)
	private GuestRoomEntity guestRoom;
	
	@Column(name="receiver")
	private String receiver;
	
	@Column(name="status")
	private String status;
	
	@Column(name="room_number")
	private String roomNumber;
	
	@Column(name="reservation_number")
	private String reservationNumber;
	
	@Column(name="created_date")
	private Date createdDate;
	
	public MessageEntity() {
		
	}
	
	public MessageEntity(Message msg, GuestRoomEntity guestRoom) {
		try {
			this.setContent(AESencrp.encrypt(msg.getContent()));
		} catch (Exception e) {
			e.printStackTrace();
			this.setContent(msg.getContent());
		}
		this.setContentType(msg.getContentType());
		this.setSender(msg.getSender());
		this.setReceiver(msg.getReceiver());
		this.setGuestRoom(guestRoom);
		this.setRoomNumber(msg.getRoomNumber());
		this.setReservationNumber(msg.getReservationNumber());
		this.setCreatedDate(new Date());
		this.setStatus(msg.getContentStatus());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public GuestRoomEntity getGuestRoom() {
		return guestRoom;
	}

	public void setGuestRoom(GuestRoomEntity guestRoom) {
		this.guestRoom = guestRoom;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getContent() {
		try {
			return AESencrp.decrypt(content);
		} catch (Exception e) {
			return content;
		}
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/* ORDER BY createdDate ASC */
	@Override
	public int compareTo(Object messageObj) {
		MessageEntity message = (MessageEntity) messageObj;
		return this.createdDate.compareTo(message.getCreatedDate());
	}
}