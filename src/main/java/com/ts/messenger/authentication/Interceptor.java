package com.ts.messenger.authentication;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.HotelServices;
import com.ts.messenger.multi.tenant.RequestContext;
import com.ts.messenger.services.HotelService;
import com.ts.messenger.utils.Constants;

@Component
public class Interceptor { 

	@Autowired
	HotelService hotelService;
	
    /**
     * Executed before actual handler is executed
     **/
    public int preHandle(String authorization, String customerId, String hotelId) throws Exception {  
    	
    	/* if no authorization in the header */
        if (authorization == null || authorization.trim().equals("")) {
    		return Constants.REQUIRED_HEADER_805;
        }

    	/* if no customerId in the header */
        if (customerId == null || customerId.trim().equals("")) {
    		return Constants.REQUIRED_CUSTOMER_807;
        } 
    
        String token = "";
        final String token_type = Credentials.getAuthTokenType();
        final String url = Credentials.getAuthUrl();  

        /* check if token in the header exist */
        if (!authorization.startsWith(token_type)) {
    		return Constants.INVALID_HEADER_806;
        } 

        token = authorization.replace(token_type, "");
        
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders service_headers = new HttpHeaders();
        String result = "";
        final JSONObject jsonObj2 = new JSONObject();
        jsonObj2.put("token", (Object)token);
        
        service_headers.set("Content-Type", "application/json");
        final HttpEntity<?> entity = (HttpEntity<?>)new HttpEntity<Object>((Object)jsonObj2.toString(), (MultiValueMap<String, String>)service_headers);
        try {
        	result = (String)restTemplate.postForObject(url, (Object)entity, (Class<?>)String.class, new Object[0]);
        } catch (Exception e) {
    		return Constants.ERROR_GENERATING_TOKEN_812;
		}
        
        JSONObject json = new JSONObject(result);
        
        if(!json.has("success") || !json.getBoolean("success")) {
        	try {
	        	JSONObject resJson = new JSONObject(json.toString());
	        	JSONArray errors = resJson.getJSONArray("errors");
	        	JSONObject error = errors.getJSONObject(0);
	    		return error.getInt("errorCode");
        	} catch (Exception e) {
        		return Constants.ERROR_GENERATING_TOKEN_814;
			}
        } 
        else {
        	JSONObject data = json.getJSONObject("data");
        	String tokenCustomerId = data.getString("username");
        	if(!customerId.equals(tokenCustomerId)) {
	    		return Constants.INVALID_TOKEN_CUSTOMER_809;
	        } 
        } 

        RequestContext.setCustomerId(customerId);

        /* Check hotelId, if can use the service and the request */
        if (hotelId != null) {  
        	if(hotelId.trim().equals("")) {
	    		return Constants.INVALID_HOTEL_ID_813;
	        } 
        	int checkHotelAuthorizationRes = checkHotelAuthorization(hotelId);
			if (checkHotelAuthorizationRes != Constants.SUCCESS) /* Only if returned false */
				return checkHotelAuthorizationRes;
        }
    	
    	return Constants.SUCCESS;
    }
    
    public int checkHotelAuthorization(String hotelId) throws JsonProcessingException, IOException, JSONException {  
				
		/* Check if hotelId exists in hotel table */
    	try {
			Hotel hotel = hotelService.getOne(hotelId);
			if(hotel == null) {
	    		return Constants.HOTEL_SERVICE_AUTH_810;
			}
    	} catch (Exception e) {
    		return Constants.HOTEL_SERVICE_AUTH_810;
		}
			
		/* Check if hotelId exists in hotel_services table and if authorized */
    	try {
			HotelServices hotelServices = hotelService.findHotelServiceByHotelId(hotelId);
			if (hotelServices == null) {
	    		return Constants.HOTEL_SERVICE_AUTH_811;
			}
    	} catch (Exception e) {
    		return Constants.HOTEL_SERVICE_AUTH_811;
		}
    	
    	return Constants.SUCCESS;
	}
     
}
