package com.ts.messenger.utils;

import org.json.JSONObject;


public class GlobalCheckFields { 
	
//	private String hotelId; 
//
//	private HttpServletRequest req;
//	
//	boolean isCheckAuth; 
//	
//	private boolean isCheckHotelAuthorization;  
	
	private JSONObject jsonBody;

//	private DeviceTypeService deviceTypeService;
//	
//	private String deviceTypeId;
//	
//	private HotelService hotelService;
//	
//	private String actionPath;
	
//	public HotelService getHotelService() {
//		return hotelService;
//	}
//
//	public void setHotelService(HotelService hotelService) {
//		this.hotelService = hotelService;
//	}

	public GlobalCheckFields() {
		super();
	} 
	
	public GlobalCheckFields(
//			HotelService hotelService, String hotelId, HttpServletRequest req,
//			boolean isCheckAuth, boolean isCheckHotelAuthorization, 
			JSONObject jsonBody
//			, String actionPath
			) {
		super();
//		this.hotelService = hotelService;
//		this.hotelId = hotelId;
//		this.req = req;
//		this.isCheckAuth = isCheckAuth;
//		this.isCheckHotelAuthorization = isCheckHotelAuthorization;
		this.jsonBody = jsonBody;
//		this.actionPath = actionPath;
	}


//	public boolean isCheckAuth() {
//		return isCheckAuth;
//	}

//	public void setCheckAuth(boolean isCheckAuth) {
//		this.isCheckAuth = isCheckAuth;
//	}   

//	public String getHotelId() {
//		return hotelId;
//	}
//
//	public void setHotelId(String hotelId) {
//		this.hotelId = hotelId;
//	}

//	public HttpServletRequest getReq() {
//		return req;
//	}
//
//	public void setReq(HttpServletRequest req) {
//		this.req = req;
//	}
//
//	public boolean isCheckHotelAuthorization() {
//		return isCheckHotelAuthorization;
//	}
//
//	public void setCheckHotelAuthorization(boolean isCheckHotelAuthorization) {
//		this.isCheckHotelAuthorization = isCheckHotelAuthorization;
//	}

	public JSONObject getJsonBody() {
		return jsonBody;
	}

	public void setJsonBody(JSONObject jsonBody) {
		this.jsonBody = jsonBody;
	}

//	public DeviceTypeService getDeviceTypeService() {
//		return deviceTypeService;
//	}
//
//	public void setDeviceTypeService(DeviceTypeService deviceTypeService) {
//		this.deviceTypeService = deviceTypeService;
//	}
//
//	public String getDeviceTypeId() {
//		return deviceTypeId;
//	}
//
//	public void setDeviceTypeId(String deviceTypeId) {
//		this.deviceTypeId = deviceTypeId;
//	}
//
//	public String getActionPath() {
//		return actionPath;
//	}
//
//	public void setActionPath(String actionPath) {
//		this.actionPath = actionPath;
//	}

}