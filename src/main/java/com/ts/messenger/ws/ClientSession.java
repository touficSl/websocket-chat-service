package com.ts.messenger.ws;

public class ClientSession {

	private String username;

	public ClientSession() {
		super();
	}

	public ClientSession(String username) {
		super();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
