package com.ts.messenger.utils;

public class Error {

	public final static String UNKNOWN_ERROR = "Unknown Error!";
	public final static String INVALID_PATH = "Invalid Path or Parameters. Redirected to /error";
	
	public final static String INVALID_EMAIL = "Invalid Email!";
	
	public static String missingStringParam(String param) {
		return "Required String parameter '" + param + "' is not present";
	}
	
	public static String missingIntParam(String param) {
		return "Required Integer parameter '" + param + "' is not present";
	}
	
}
