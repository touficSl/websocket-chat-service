package com.ts.messenger.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Terminal {

	public static void print(String customerName, String args) {
		
		String ServerSignature = "[Messenger]";
		String DatabaseConnected = "-- " + customerName + " --";
		String ServerDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String tab = "\t";
		
		System.out.println(ServerDate + tab + ServerSignature + tab + DatabaseConnected + tab + args);
	}
	
}
