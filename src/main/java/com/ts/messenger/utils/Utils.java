package com.ts.messenger.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ts.entities.NCICustomerEntity;
import com.ts.services.NCICustomerService;
import com.ts.utils.NCIResponse;
import com.ts.messenger.authentication.Credentials;

public class Utils {
	
	// SUCC_MSG; contains success messages for the message field returned
	public static Map<Integer, String> SUCC_MSG = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
		}
	};
	// ERR_MSG; contains error messages for the message field returned
	public static Map<Integer, String> ERR_MSG = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
		}
	};

	// If an unclear message returned by third party service, we can change it from
	// here
	private static Map<String, String> ErrorMessages = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
		}
	};


	private static Map<Integer, String> Messages = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(  Constants.SUCCESS, "" );
			put(  Constants.ERROR_GENERATING_TOKEN_812, Constants.ERROR_GENERATING_TOKEN );
			put(  Constants.REQUIRED_HEADER_805, Constants.REQUIRED_HEADER );
			put(  Constants.INVALID_HEADER_806, Constants.INVALID_HEADER );
			put(  Constants.REQUIRED_CUSTOMER_807, Constants.REQUIRED_CUSTOMER );
			put(  Constants.INVALID_CUSTOMER_808, Constants.INVALID_CUSTOMER );
			put(  Constants.INVALID_TOKEN_CUSTOMER_809, Constants.INVALID_TOKEN_CUSTOMER );
			put(  Constants.REQUIRED_DEVICE_TYPE_ID_117, Constants.REQUIRED_DEVICE_TYPE_ID );
			put(  Constants.INVALID_DEVICE_TYPE_ID_115, Constants.INVALID_DEVICE_TYPE_ID ); 
			put(  Constants.INVALID_CREDENTIALS_120, Constants.INVALID_CREDENTIALS ); 
			put(  Constants.INVALID_HOTEL_ID_813, Constants.INVALID_HOTEL_ID ); 
		}
	};

	public static void printLogMessage(String className, String message) {
		if(Credentials.isDebugMode()) { 
			System.out.println("\n SYSTEM_LOGS >>> " + className + " - " + new SimpleDateFormat(Constants.DateTimeFormat).format(new Date()) + " - " + message + "\n");
		}
	}

	public static NCICustomerEntity getCustomerById(String customerId)
	{
		NCIResponse nciresponse = NCICustomerService.getCustomerByCustomerId(customerId, Credentials.isDebugMode(), Credentials.getCommonDatabaseUrl(), Credentials.getCommonDatabaseUser(), Credentials.getCommonDatabaseDriver(), Credentials.getCommonDatabasePassword(), Credentials.getCommonDatabaseCatalog());
		NCICustomerEntity customer = (NCICustomerEntity) nciresponse.getData();
		return customer;
	}

	public static Date getDateByCustomerTimezone(String timezone)
    {
    	Date dateByCust = null;
    	try {
    		Date serverDate = new Date();
    		if(timezone != null && !timezone.isEmpty())
    		{
    			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			df.setTimeZone(TimeZone.getTimeZone(timezone));//can be something like "GMT+1" or "Asia/Singapore"
    			String dateByCustStr = df.format(serverDate);
    			dateByCust = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateByCustStr);
    		}
    		else dateByCust = serverDate;
    		return dateByCust;
    	} catch (Exception e) {
    		if(Credentials.isDebugMode()) e.printStackTrace();
    		return new Date();
    	}
    }

	public static double roundNumber(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static boolean jsonContainsValue(JSONObject jsonObject, String value) {
		try {
			return jsonObject.has(value) && jsonObject.getString(value) != null
					&& jsonObject.getString(value).length() > 0 && !jsonObject.getString(value).equalsIgnoreCase("null")
							? true
							: false;
		} catch (JSONException e) {
			return false;
		}
	}

	public static String returnEmptyStringIfNull(String value) {
		return value == null ? "" : value;
	}

	public static String returnEncryptedString(String value) {
		try {
			return value == null ? "" : AESencrp.encrypt(value);
		} catch (Exception e) {
			return "";
		}
	}

	public static void executeAsPDF(String path, HttpServletResponse httpResponse) throws Exception {
		
		httpResponse.setContentType("application/pdf");
		String PdfFilePath = path;
		httpResponse.setHeader("Content-Disposition", "inline; filename=" + "E Registration Card.pdf");
		File pdfFile = new File(PdfFilePath);

		httpResponse.setContentLength((int) pdfFile.length());

		FileInputStream fileInputStream = new FileInputStream(pdfFile);
		OutputStream responseOutputStream = httpResponse.getOutputStream();
		int bytes;
		while ((bytes = fileInputStream.read()) != -1) {
			responseOutputStream.write(bytes);
		}
	}

	public static String GetJsonFormat(int status, String message, String data, int msgId) throws JSONException {

		JSONObject jsonObj = new JSONObject();
		if (status != 0) {
			if (message == null || message.isEmpty())
				if (!Messages.containsKey(status))
					message = Messages.get(-1);
				else
					message = Messages.get(status);

			JSONArray jsonArray = new JSONArray();
			JSONObject obj = new JSONObject();
			obj.put("errorMessage", message);
			obj.put("userMessage", message);
			obj.put("errorCode", status);
			jsonArray.put(obj);
			jsonObj.put("errors", jsonArray);
			jsonObj.put("statusCode", Constants.SERVER_ERROR);
		}

		boolean isSuccess = false;

		jsonObj.put("apiVersion", Constants.BACKEND_VERSION);

		if (data == null) {
			jsonObj.put("data", new JSONObject());
			if (status == 0)
				isSuccess = true;
		} else if (data.charAt(0) == '[') {
			isSuccess = true;
			JSONArray array = new JSONArray(data);
			JSONArray resArray = new JSONArray();
			for (int i = 0; i < array.length(); i++) {
				Object obj = array.get(i);
				resArray.put(new JSONObject(obj.toString()));
			}
			jsonObj.put("data", resArray);

			jsonObj.put("errors", new JSONArray());
		} else {
			isSuccess = true;
			JSONObject response = new JSONObject(data);
			JSONArray jsonArray = new JSONArray();

			if (response.has("errors")) {
				jsonArray = AddUserErrors(response.getJSONArray("errors"));
				response = new JSONObject();
				isSuccess = false;
				jsonObj.put("statusCode", Constants.THIRDPARTY_SERVER_ERROR);
			}

			jsonObj.put("errors", jsonArray);
			jsonObj.put("data", response);
		}

		jsonObj.put("success", isSuccess);
		if (isSuccess)
			jsonObj.put("statusCode", Constants.SERVER_OK);

		if (msgId != 0)
			if (isSuccess)
				message = SUCC_MSG.get(msgId);
			else
				message = ERR_MSG.get(msgId);
		if (message == null)
			message = "";
		jsonObj.put("message", message);

		return jsonObj.toString();
	}

	private static JSONArray AddUserErrors(JSONArray jsonArray) throws JSONException {
		JSONArray newJsonArray = new JSONArray();
		for (int i = 0; i < jsonArray.length(); i++) {
			Object obj = jsonArray.get(i);
			if (obj instanceof JSONObject) {
				JSONObject jsonObj = (JSONObject) obj;
				if (ErrorMessages.get(jsonObj.getString("errorCode")) == null)
					jsonObj.put("userMessage", jsonObj.getString("errorMessage"));
				else
					jsonObj.put("userMessage", ErrorMessages.get(jsonObj.getString("errorCode")));

				newJsonArray.put(jsonObj);
			}
		}
		return newJsonArray;
	}
	
	public static Date convertDateStringFormatToDate(String dateStr, String dateformat) {
	    try {
			return new SimpleDateFormat(dateformat).parse(dateStr);
		} catch (Exception e) {
			return null;
		}  
	}
	
	public static Timestamp getHotelTimezoneDateByHotelId(String timezone) {
    	try {
    		Date serverDate = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		if(timezone != null && !timezone.isEmpty()) 
    			df.setTimeZone(TimeZone.getTimeZone(timezone));
    		
    		return Timestamp.valueOf(df.format(serverDate));
    	} catch (Exception e) {
    		e.printStackTrace();
    		return new Timestamp(new Date().getTime());
    	}
	}
	
	public static int convertStringToInt(String intStrValue) {
		try {
			return Integer.valueOf(intStrValue);
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static BufferedImage resizeImage(Integer width, Integer height, BufferedImage originalImage) {
		try {
			int originalWidth = originalImage.getWidth();
			int originalHeight = originalImage.getHeight();
			int convertedWidth;
			int convertedHeight;
			float original_rapport = ((float) originalHeight) / originalWidth;
			float targeted_rapport = ((float) height) / width;

			if (originalWidth <= width && originalHeight <= height)
				return originalImage;

			if (original_rapport > targeted_rapport) {
				convertedHeight = height;
				convertedWidth = (int) (convertedHeight / original_rapport);
			} else {
				convertedWidth = width;
				convertedHeight = (int) (convertedWidth * original_rapport);
			}

			int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
			BufferedImage resizedImage = ResizeImageUtils.resizeImageWithHint(originalImage, type, convertedWidth,
					convertedHeight);

			return resizedImage;

		} catch (Exception e) {
			return null;
		}
	}
}
