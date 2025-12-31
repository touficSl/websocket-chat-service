package com.ts.messenger.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="room_type")
public class RoomEntity implements Comparable<RoomEntity> {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="hotel_id")
	private String hotelId;
	
	@Column(name="room_code")
	private String roomCode;
	
	@Column(name="room_title")
	private String roomTitle;
	
	@Column(name="room_summary")
	private String roomSummary;
	
	@Column(name="room_thumb")
	private String roomThumb;
	
	@Column(name="room_desc")
	private String roomDescription;
	
	@Column(name="room_amenities")
	private String roomAmenities;
	
	@Column(name="trust_code_1")
	private String trustCode1;
	
	@Column(name="trust_code_2")
	private String trustCode2;
	
	@Column(name="max_children_number")
	private int maxChildrenNumber;
	
	@Column(name="max_adults_number")
	private int maxAdultsNumber;
	
	public RoomEntity() {}

	public String getHotelId() {
		return hotelId;
	}

	public void setHotelId(String hotelId) {
		this.hotelId = hotelId;
	}

	public String getRoomCode() {
		return roomCode;
	}

	public void setRoomCode(String roomCode) {
		this.roomCode = roomCode;
	}

	public String getRoomTitle() {
		return roomTitle;
	}

	public void setRoomTitle(String roomTitle) {
		this.roomTitle = roomTitle;
	}

	public String getRoomSummary() {
		return roomSummary;
	}

	public void setRoomSummary(String roomSummary) {
		this.roomSummary = roomSummary;
	}

	public String getRoomThumb() {
		return roomThumb;
	}

	public void setRoomThumb(String roomThumb) {
		this.roomThumb = roomThumb;
	}

	public String getRoomDescription() {
		return roomDescription;
	}

	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}

	public String getRoomAmenities() {
		return roomAmenities;
	}

	public void setRoomAmenities(String roomAmenities) {
		this.roomAmenities = roomAmenities;
	}

	public String getTrustCode1() {
		return trustCode1;
	}

	public void setTrustCode1(String trustCode1) {
		this.trustCode1 = trustCode1;
	}

	public String getTrustCode2() {
		return trustCode2;
	}

	public void setTrustCode2(String trustCode2) {
		this.trustCode2 = trustCode2;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getMaxChildrenNumber() {
		return maxChildrenNumber;
	}

	public void setMaxChildrenNumber(int maxChildrenNumber) {
		this.maxChildrenNumber = maxChildrenNumber;
	}

	public int getMaxAdultsNumber() {
		return maxAdultsNumber;
	}

	public void setMaxAdultsNumber(int maxAdultsNumber) {
		this.maxAdultsNumber = maxAdultsNumber;
	}

	@Override
	public int compareTo(RoomEntity roomEntity) {
        return Integer.compare(getId(), roomEntity.getId());
	}
}
