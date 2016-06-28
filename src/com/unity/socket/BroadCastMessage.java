package com.unity.socket;

public class BroadCastMessage {
	
	private String message;
	private String clientIDs;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getClientIDs() {
		return clientIDs;
	}
	public void setClientIDs(String clientIDs) {
		this.clientIDs = clientIDs;
	}
}
