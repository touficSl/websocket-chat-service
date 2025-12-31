package com.ts.messenger.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name="hotel")
public class Hotel {
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@Column(name = "id")
	private String id;
	
	@Column(name = "hotel_code")
	private String hotelCode;

	@Column(name = "has_split_cache")
	private boolean hasSplitCache;
	
	@Column(name = "hotel_name")
	private String hotelName;

	@Column(name = "room_0_prefixed")
	private int room0Prefixed = 0;
	
	@Column(name = "hotel_id")
	private String hotelId;
	
	@Column(name = "hotel_city")
	private String city;
	
	@Column(name = "hotel_country")
	private String country;
	
	@Column(name = "hotel_image")
	private String hotel_image;

	@Column(name = "hotel_address")
	private String hotel_address;
	
	@Column(name = "hotel_tel")
	private String hotel_tel;
	
	@Column(name = "hotel_website")
	private String hotel_website;
	
	@Column(name = "explore_image")
	private String explore_image;
	
	@Column(name = "checkin_email")
	private String checkin_email;
	
	@Column(name = "room_reservations_image")
	private String room_reservations_image;
	
	@Column(name = "rooms_suites_image")
	private String rooms_suites_image;
	
	@Column(name = "restaurants_bars_image")
	private String restaurants_bars_image;
	
	@Column(name = "escape_image")
	private String escape_image;
	
	@Column(name = "city_guide_image")
	private String city_guide_image;
	
	@Column(name = "meeting_image")
	private String meeting_image;
	
	@Column(name = "offer_image")
	private String offer_image;
	
	@Column(name = "rewards_image")
	private String rewards_image;
	
	@Column(name = "mystay_image")
	private String myStay_image;
	
	@Column(name = "at_glance_image")
	private String at_glance_image;
	
	@Column(name = "destination_image")
	private String destination_image;
	
	@Column(name = "hotel_facilities_image")
	private String hotel_facilities_image;
	
	@Column(name = "recreation_image")
	private String recreation_image;
	
	@Column(name = "tripadvisorrate")
	private String tripadvisorRate;
	
	@Column(name = "starts")
	private String starts;
	
	@Column(name = "hotel_latitude")
	private String hotelLattitude;
	
	@Column(name = "hotel_longitude")
	private String hotelLongitute;
	
	@Column(name = "conf_nightly_rate")
	private String confNightlyRate;
	
	@Column(name = "conf_checkin_time_from")
	private String confCheckinTimeFrom;
	
	@Column(name = "conf_identification")
	private String confIdentification;
	
	@Column(name = "conf_checkout_time_to")
	private String confCheckoutTimeTo;
	
	@Column(name = "hotel_timezone_id")
	private String timezone;
	
	@Column(name = "currency")
	private String currency;
	
	@Column(name = "terms_conditions")
	private String termsConditions;
	
	@Column(name = "departure_time_policy")
	private String departureTimePolicy;
	
	@Column(name = "rate_policy")
	private String ratePolicy;
	
	@Column(name = "privacy_policy")
	private String privacyPolicy;
	
	@Column(name = "secondary_name")
	private String secondaryName;
	
	@Column(name = "gdpr")
	private String gdpr;
	
	@Column(name = "feature_using_reservtaion_number")
	private boolean featureUsingReservtaionNumber;
	
	@Column(name = "feature_check_performedonlinecheckin")
	private boolean featureCheckPerformedonlinecheckin;
	
	@Column(name = "arrival_time_policy")
	private String arrivalTimePolicy;
	
	/* Kiosk features for now */
	@Column(name = "feature_update_name_request")
	private boolean featureUpdateNameRequest;
	
	@Column(name = "feature_update_email")
	private boolean featureUpdateEmail;
	
	@Column(name = "feature_update_phone")
	private boolean featureUpdatePhone;
	
	@Column(name = "feature_insert_preference")
	private boolean featureInsertPreference;
	
	@Column(name = "feature_update_address")
	private boolean featureUpdateAddress;
	
	@Column(name = "feature_update_privacy_option")
	private boolean featureUpdatePrivacyOption;
	
	@Column(name = "feature_update_passport")
	private boolean featureUpdatePassport;
	
	@Column(name = "feature_modify_booking")
	private boolean featureModifyBooking;
	
	@Column(name = "feature_update_guest_card")
	private boolean featureUpdateGuestCard;
	
	@Column(name = "feature_update_comments")
	private boolean featureUpdateComments;
	
	@Column(name = "feature_custom_folio_email_subject")
	private boolean featureCustomFolioEmailSubject;

	@Column(name = "active")
	private boolean active;
	
	public Hotel() {
		
	}

	public Hotel(String hotelCode, String hotelCountry, String hotelName,int room0Prefixed) {
		super();
		this.hotelCode = hotelCode;
		this.hotelName = hotelName;
		this.room0Prefixed = room0Prefixed;
	}

	public Hotel(String kioskCode, String hotelCode, String name, String city, String country, String hotel_image, String user_id, String password)
	{
		this.hotelCode = hotelCode;
		this.hotelName = name;
		this.city = city;
		this.country = country;
		this.hotel_image = hotel_image;
	}

	public String getHotelCode() {
		return hotelCode;
	}

	public void setHotelCode(String hotelCode) {
		this.hotelCode = hotelCode;
	}

	public String getHotelName() {
		return hotelName;
	}

	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}

//	public String getHotelVersion() {
//		return hotelVersion;
//	}
//
//	public void setHotelVersion(String hotelVersion) {
//		this.hotelVersion = hotelVersion;
//	}

	public int getRoom0Prefixed() {
		return room0Prefixed;
	}

	public void setRoom0Prefixed(int room0Prefixed) {
		this.room0Prefixed = room0Prefixed;
	}

	public String gethotelId() {
		return hotelId;
	}

	public void sethotelId(String hotelId) {
		this.hotelId = hotelId;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getHotel_image() {
		return hotel_image;
	}

	public void setHotel_image(String hotel_image) {
		this.hotel_image = hotel_image;
	}

	public String getHotel_address() {
		return hotel_address;
	}

	public void setHotel_address(String hotel_address) {
		this.hotel_address = hotel_address;
	}

	public String getHotel_tel() {
		return hotel_tel;
	}

	public void setHotel_tel(String hotel_tel) {
		this.hotel_tel = hotel_tel;
	}

	public String getHotel_website() {
		return hotel_website;
	}

	public void setHotel_website(String hotel_website) {
		this.hotel_website = hotel_website;
	}

	public String getExplore_image() {
		return explore_image;
	}

	public void setExplore_image(String explore_image) {
		this.explore_image = explore_image;
	}

	public String getCheckin_email() {
		return checkin_email;
	}

	public void setCheckin_email(String checkin_email) {
		this.checkin_email = checkin_email;
	}

	public String getRoom_reservations_image() {
		return room_reservations_image;
	}

	public void setRoom_reservations_image(String room_reservations_image) {
		this.room_reservations_image = room_reservations_image;
	}

	public String getRooms_suites_image() {
		return rooms_suites_image;
	}

	public void setRooms_suites_image(String rooms_suites_image) {
		this.rooms_suites_image = rooms_suites_image;
	}

	public String getRestaurants_bars_image() {
		return restaurants_bars_image;
	}

	public void setRestaurants_bars_image(String restaurants_bars_image) {
		this.restaurants_bars_image = restaurants_bars_image;
	}

	public String getEscape_image() {
		return escape_image;
	}

	public void setEscape_image(String escape_image) {
		this.escape_image = escape_image;
	}

	public String getCity_guide_image() {
		return city_guide_image;
	}

	public void setCity_guide_image(String city_guide_image) {
		this.city_guide_image = city_guide_image;
	}

	public String getMeeting_image() {
		return meeting_image;
	}

	public void setMeeting_image(String meeting_image) {
		this.meeting_image = meeting_image;
	}

	public String getOffer_image() {
		return offer_image;
	}

	public void setOffer_image(String offer_image) {
		this.offer_image = offer_image;
	}

	public String getRewards_image() {
		return rewards_image;
	}

	public void setRewards_image(String rewards_image) {
		this.rewards_image = rewards_image;
	}

	public String getMyStay_image() {
		return myStay_image;
	}

	public void setMyStay_image(String myStay_image) {
		this.myStay_image = myStay_image;
	}

	public String getAt_glance_image() {
		return at_glance_image;
	}

	public void setAt_glance_image(String at_glance_image) {
		this.at_glance_image = at_glance_image;
	}

	public String getDestination_image() {
		return destination_image;
	}

	public void setDestination_image(String destination_image) {
		this.destination_image = destination_image;
	}

	public String getHotel_facilities_image() {
		return hotel_facilities_image;
	}

	public void setHotel_facilities_image(String hotel_facilities_image) {
		this.hotel_facilities_image = hotel_facilities_image;
	}

	public String getRecreation_image() {
		return recreation_image;
	}

	public void setRecreation_image(String recreation_image) {
		this.recreation_image = recreation_image;
	}

	public String getTripadvisorRate() {
		return tripadvisorRate;
	}

	public void setTripadvisorRate(String tripadvisorRate) {
		this.tripadvisorRate = tripadvisorRate;
	}

	public String getStarts() {
		return starts;
	}

	public void setStarts(String starts) {
		this.starts = starts;
	}

	public String getHotelLattitude() {
		return hotelLattitude;
	}

	public void setHotelLattitude(String hotelLattitude) {
		this.hotelLattitude = hotelLattitude;
	}

	public String getHotelLongitute() {
		return hotelLongitute;
	}

	public void setHotelLongitute(String hotelLongitute) {
		this.hotelLongitute = hotelLongitute;
	}

//	@JSON(include = false)
//	public String getUser_id() {
//		return user_id;
//	}
//
//	public void setUser_id(String user_id) {
//		this.user_id = user_id;
//	}
//
//	@JSON(include = false)
//	public String getPassword() {
//		return password;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//	
	public String getConfNightlyRate() {
		return confNightlyRate;
	}

	public void setConfNightlyRate(String confNightlyRate) {
		this.confNightlyRate = confNightlyRate;
	}

	public String getConfCheckinTimeFrom() {
		return confCheckinTimeFrom;
	}

	public void setConfCheckinTimeFrom(String confCheckinTimeFrom) {
		this.confCheckinTimeFrom = confCheckinTimeFrom;
	}

	public String getConfIdentification() {
		return confIdentification;
	}

	public void setConfIdentification(String confIdentification) {
		this.confIdentification = confIdentification;
	}

	public String getConfCheckoutTimeTo() {
		return confCheckoutTimeTo;
	}

	public void setConfCheckoutTimeTo(String confCheckoutTimeTo) {
		this.confCheckoutTimeTo = confCheckoutTimeTo;
	}

//	public ArrayList<PayMethod> getPay_method_list() {
//		return pay_method_list;
//	}
//
//	public void setPay_method_list(ArrayList<PayMethod> pay_method_list) {
//		this.pay_method_list = pay_method_list;
//	}

	public String getTermsConditions() {
		return termsConditions;
	}

	public void setTermsConditions(String termsConditions) {
		this.termsConditions = termsConditions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean getHasSplitCache() {
		return hasSplitCache;
	}

	public void setHasSplitCache(boolean hasSplitCache) {
		this.hasSplitCache = hasSplitCache;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDepartureTimePolicy() {
		return departureTimePolicy;
	}

	public void setDepartureTimePolicy(String departureTimePolicy) {
		this.departureTimePolicy = departureTimePolicy;
	}

	public String getRatePolicy() {
		return ratePolicy;
	}

	public void setRatePolicy(String ratePolicy) {
		this.ratePolicy = ratePolicy;
	}

	public String getPrivacyPolicy() {
		return privacyPolicy;
	}

	public void setPrivacyPolicy(String privacyPolicy) {
		this.privacyPolicy = privacyPolicy;
	}

	public String getSecondaryName() {
		return secondaryName;
	}

	public void setSecondaryName(String secondaryName) {
		this.secondaryName = secondaryName;
	}

	public String getGdpr() {
		return gdpr;
	}

	public void setGdpr(String gdpr) {
		this.gdpr = gdpr;
	}

	public boolean isFeatureUpdateNameRequest() {
		return featureUpdateNameRequest;
	}

	public void setFeatureUpdateNameRequest(boolean featureUpdateNameRequest) {
		this.featureUpdateNameRequest = featureUpdateNameRequest;
	}

	public boolean isFeatureUsingReservtaionNumber() {
		return featureUsingReservtaionNumber;
	}
	
	public void setFeatureUsingReservtaionNumber(boolean featureUsingReservtaionNumber) {
		this.featureUsingReservtaionNumber = featureUsingReservtaionNumber;
	}
	
	public boolean isFeatureCheckPerformedonlinecheckin() {
		return featureCheckPerformedonlinecheckin;
	}
	
	public void setFeatureCheckPerformedonlinecheckin(boolean featureCheckPerformedonlinecheckin) {
		this.featureCheckPerformedonlinecheckin = featureCheckPerformedonlinecheckin;
	}

	public String getArrivalTimePolicy() {
		return arrivalTimePolicy;
	}

	public void setArrivalTimePolicy(String arrivalTimePolicy) {
		this.arrivalTimePolicy = arrivalTimePolicy;
	}

	public boolean isFeatureUpdateEmail() {
		return featureUpdateEmail;
	}

	public void setFeatureUpdateEmail(boolean featureUpdateEmail) {
		this.featureUpdateEmail = featureUpdateEmail;
	}

	public boolean isFeatureUpdatePhone() {
		return featureUpdatePhone;
	}

	public void setFeatureUpdatePhone(boolean featureUpdatePhone) {
		this.featureUpdatePhone = featureUpdatePhone;
	}

	public boolean isFeatureInsertPreference() {
		return featureInsertPreference;
	}

	public void setFeatureInsertPreference(boolean featureInsertPreference) {
		this.featureInsertPreference = featureInsertPreference;
	}

	public boolean isFeatureUpdateAddress() {
		return featureUpdateAddress;
	}

	public void setFeatureUpdateAddress(boolean featureUpdateAddress) {
		this.featureUpdateAddress = featureUpdateAddress;
	}

	public boolean isFeatureUpdatePrivacyOption() {
		return featureUpdatePrivacyOption;
	}

	public void setFeatureUpdatePrivacyOption(boolean featureUpdatePrivacyOption) {
		this.featureUpdatePrivacyOption = featureUpdatePrivacyOption;
	}

	public boolean isFeatureUpdatePassport() {
		return featureUpdatePassport;
	}

	public void setFeatureUpdatePassport(boolean featureUpdatePassport) {
		this.featureUpdatePassport = featureUpdatePassport;
	}

	public boolean isFeatureModifyBooking() {
		return featureModifyBooking;
	}

	public void setFeatureModifyBooking(boolean featureModifyBooking) {
		this.featureModifyBooking = featureModifyBooking;
	}

	public boolean isFeatureUpdateGuestCard() {
		return featureUpdateGuestCard;
	}

	public void setFeatureUpdateGuestCard(boolean featureUpdateGuestCard) {
		this.featureUpdateGuestCard = featureUpdateGuestCard;
	}

	public boolean isFeatureUpdateComments() {
		return featureUpdateComments;
	}

	public void setFeatureUpdateComments(boolean featureUpdateComments) {
		this.featureUpdateComments = featureUpdateComments;
	}
	
	public boolean isFeatureCustomFolioEmailSubject() {
		return featureCustomFolioEmailSubject;
	}
	
	public void setFeatureCustomFolioEmailSubject(boolean featureCustomFolioEmailSubject) {
		this.featureCustomFolioEmailSubject = featureCustomFolioEmailSubject;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
