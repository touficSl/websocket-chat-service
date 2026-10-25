package com.ts.messenger.authentication;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ts.messenger.utils.PropertiesManager;

/**
 * Static access to application.properties for code that runs outside of the Spring context
 * (tenant data source bootstrap, entities, utils).
 * Values can reference environment variables / system properties: ${ENV_VAR:default}
 */
public class Credentials {

	static Properties properties = PropertiesManager.load("/application.properties");

	private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z0-9_.\\-]+)(?::([^}]*))?\\}");

	static String get(String key) {
		String raw = properties.getProperty(key);
		if (raw == null)
			return null;

		Matcher matcher = PLACEHOLDER.matcher(raw);
		StringBuffer resolved = new StringBuffer();
		while (matcher.find()) {
			String name = matcher.group(1);
			String value = System.getProperty(name);
			if (value == null)
				value = System.getenv(name);
			if (value == null)
				value = matcher.group(2) != null ? matcher.group(2) : "";
			matcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(resolved);
		return resolved.toString().trim();
	}

	/*	Common DB section */

	public static String getCommonDatabaseUrl() {
		return get("common.database.url");
	}

	public static String getCommonDatabaseCatalog() {
		return get("common.database.catalog");
	}

	public static String getCommonDatabaseUser() {
		return get("common.database.user");
	}

	public static String getCommonDatabasePassword() {
		return get("common.database.password");
	}

	public static String getCommonDatabaseDriver() {
		return get("common.database.driver");
	}

	public static int getTenantPoolMaxSize() {
		try {
			return Math.max(1, Integer.parseInt(get("tenant.pool.max-size")));
		} catch (Exception e) {
			return 5;
		}
	}

	/*	Others */
	public static String getDefaultCustomerId() {
		String customerId = get("customer.id");
		return customerId == null || customerId.isEmpty() ? null : customerId;
	}

	public static boolean isDebugMode() {
		return "true".equalsIgnoreCase(get("is.debug.mode"));
	}

	/*	Auth section */
	public static String getAuthTokenType() {
		return get("auth.token.type") + " ";
	}

	public static String getAuthUrl() {
		return get("auth.url");
	}

	public static int getAuthConnectTimeoutMs() {
		return parseInt(get("auth.connect-timeout-ms"), 5000);
	}

	public static int getAuthReadTimeoutMs() {
		return parseInt(get("auth.read-timeout-ms"), 10000);
	}

	/*	Encryption section */
	public static String getEncryptionKey() {
		return get("messenger.encryption.key");
	}

	/*	Upload section */
	public static String getUploadImagesPath(String customerName) {
		return get("upload.images.path").replace("#customerName#", customerName);
	}

	private static int parseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
