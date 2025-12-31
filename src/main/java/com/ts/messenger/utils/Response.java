package com.ts.messenger.utils;

public class Response {
	
	private boolean success;
	private int statusCode;
	private String message;
	private Object data;
	private String version;
	
	public Response(boolean success, int statusCode, String message, Object data) {
		this.success = success;
		this.statusCode = statusCode;
		this.message = message;
		this.data = data;
		version = Constants.BACKEND_VERSION;
	}
	
	public Response(boolean success, int statusCode, String message) {
		this.success = success;
		this.statusCode = statusCode;
		this.message = message;
		this.data = new Object();
		version = Constants.BACKEND_VERSION;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}


}
