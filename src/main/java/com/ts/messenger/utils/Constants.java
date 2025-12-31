package com.ts.messenger.utils;

import java.util.ArrayList;
import java.util.List;

public final class Constants {

	public static final String BACKEND_VERSION = "1.0.2";

	public static final String SERVICE_NAME = "messenger"; 
	public static final boolean DEBUG = true;
	
	/* List of APIs (url) that don't need authentication */
	public static List<String> NO_AUTH_API_LIST = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("/error");	// first-time-run 

			/* Swagger */
			add("/swagger"); 
			add("v3/api-docs");
			add("/content/styles/site.css");
			add("/scripts/chatroom.js");
		}
	};

	/* API call Constants */  
	public static final String AUTHORIZATION = "Authorization";
	public static final String CUSTOMERID = "customerId";
	public static final String hotelId = "hotelId";
	public static final String deviceTypeId = "deviceTypeId";
	
	public static final String LANG = "E";
	
	public static final String defaultLogsTimeZone = "Asia/Beirut";
	
	public static final String DateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DateFormat = "yyyy-MM-dd";
	public static final String CCDateFormat = "yyyy-MM";
	public static final String TimeFormat = "HH:mm:ss";

	public static final int SUCCESS = 0;
	public static final int WARNING = -1;
	public static final int ERROR = -2;
	public static final int SERVER_OK = 200;
	public static final int SERVER_ERROR = 401;
	public static final int THIRDPARTY_SERVER_ERROR = 402;
	
	public static final int REQUIRED_HEADER_805 = 805;
	public static final int INVALID_HEADER_806 = 806;
	public static final int REQUIRED_CUSTOMER_807 = 807;
	public static final int INVALID_CUSTOMER_808 = 808;
	public static final int INVALID_TOKEN_CUSTOMER_809 = 809;
	public static final int HOTEL_SERVICE_AUTH_810 = 810;
	public static final int HOTEL_SERVICE_AUTH_811 = 811;
	public static final int ERROR_GENERATING_TOKEN_812 = 812; 
	public static final int INVALID_HOTEL_ID_813 = 813; 
	public static final int ERROR_GENERATING_TOKEN_814 = 814; 

	public static final int INVALID_DEVICE_TYPE_ID_115 = 115;
	public static final int REQUIRED_DEVICE_TYPE_ID_117 = 117;
	public static final int INVALID_CREDENTIALS_120 = 120;
	
	public static final int INTERNAL_ERROR_203 = 203;
	

	public static final String ERROR_GENERATING_TOKEN = "Error while generating token, please try again.";
	public static final String REQUIRED_HEADER = "Required authorization.";
	public static final String INVALID_HEADER = "Invalid header.";
	public static final String REQUIRED_CUSTOMER = "Missing request header 'customerId' for method parameter of type String.";
	public static final String INVALID_CUSTOMER = "Invalid customerId";
	public static final String INVALID_TOKEN_CUSTOMER = "Invalid token customerId.";
	public static final String REQUIRED_DEVICE_TYPE_ID = "Missing request header 'deviceTypeId' for method parameter of type String.";
	public static final String INVALID_DEVICE_TYPE_ID = "Invalid deviceTypeId.";
	public static final String SERVER_ERROR_MSG = "Error";
	public static final String SERVER_SUCCESS_MSG = "Success";
	public static final String REQUIRED_HOTELCODE = "hotelCode is required.";
	public static final String REQUIRED_LOGIN = "login is required.";
	public static final String REQUIRED_PASSWORD = "password is required.";
	public static final String INVALID_CREDENTIALS = "Invalid credentials.";
	public static final String INVALID_HOTEL_ID = "Invalid hotelId";
	
	
	public final static int UNKNOWN_ERROR = 2;
	public final static int MISSING_PARAMETERS = 3;
	public final static int SYNTAX_ERROR = 4;
	public final static int INVALID_PATH = 5;
	
	/****************************************************/
	/*******************   USER  ************************/
	/****************************************************/
	
	public final static int USER_INVALID_USERNAME = 100;
	public final static int USER_INVALID_PASSWORD = 101;
	
	/****************************************************/
	/*******************   UTIL  ************************/
	/****************************************************/
	
	public final static int APP_CONFIG_NOT_FOUND = 500;

}
