package com.ts.messenger.multi.tenant;

import org.springframework.stereotype.Component;

@Component
public class RequestContext {

	private static ThreadLocal<String> currentCustomerId = new ThreadLocal<>(); 

	public static String getCustomerId() {
		return currentCustomerId.get();
	}

	public static void setCustomerId(String customerId) {
		currentCustomerId.set(customerId);
	} 
 
	public static void clear(){
		currentCustomerId.remove();
	}
}