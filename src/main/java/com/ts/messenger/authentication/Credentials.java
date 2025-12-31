package com.ts.messenger.authentication;

import java.util.Properties;

import com.ts.messenger.utils.PropertiesManager;


public class Credentials {
	
	static Properties properties = PropertiesManager.load("/application.properties");

	/*	Common DB section */

	public static String getCommonDatabaseUrl() {
		return properties.getProperty("common.database.url");
	}

	public static String getCommonDatabaseCatalog() {
		return properties.getProperty("common.database.catalog");
	}

	public static String getCommonDatabaseUser() {
		return properties.getProperty("common.database.user");
	}

	public static String getCommonDatabasePassword() {
		return properties.getProperty("common.database.password");
	}
	
	public static String getCommonDatabaseDriver() {
		return properties.getProperty("common.database.driver");
	}

	/*	Others */
	public static String getDefaultCustomerId() {
		return properties.getProperty("customer.id");
	}

	public static boolean isDebugMode() {
		return properties.getProperty("is.debug.mode").equals("true") ? true : false;
	}
	
	/*	Auth section */
	public static String getAuthTokenType() {
		return properties.getProperty("auth.token.type") + " ";
	}
	
	public static String getAuthUrl() {
		return properties.getProperty("auth.url");
	}
	
	public static String getUploadImagesPath(String customerName) {
		return properties.getProperty("upload.images.path").replace("#customerName#", customerName);
	}
	
}


