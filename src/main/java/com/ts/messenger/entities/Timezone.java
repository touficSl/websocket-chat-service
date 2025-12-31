package com.ts.messenger.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "time_zone")
public class Timezone {
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@Column(name = "id")
	private String id;

	@Column(name = "hotel_timezone")
	private String hotelTimezone;

	public Timezone() {
	}

	public Timezone(String hotelTimezone) {
		this.hotelTimezone = hotelTimezone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHotelTimezone() {
		return hotelTimezone;
	}

	public void setHotelTimezone(String hotelTimezone) {
		this.hotelTimezone = hotelTimezone;
	}

}
