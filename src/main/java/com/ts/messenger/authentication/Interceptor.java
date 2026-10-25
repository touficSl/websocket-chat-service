package com.ts.messenger.authentication;

import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ts.messenger.entities.Hotel;
import com.ts.messenger.entities.HotelServices;
import com.ts.messenger.multi.tenant.RequestContext;
import com.ts.messenger.services.HotelService;
import com.ts.messenger.utils.Constants;

@Component
public class Interceptor {

	private static final Logger LOG = LoggerFactory.getLogger(Interceptor.class);

	@Autowired
	HotelService hotelService;

	private final RestTemplate restTemplate;

	public Interceptor() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Credentials.getAuthConnectTimeoutMs());
		requestFactory.setReadTimeout(Credentials.getAuthReadTimeoutMs());
		this.restTemplate = new RestTemplate(requestFactory);
	}

    /**
     * Validates the bearer token against the authentication service, binds the request to the
     * customer (tenant) database and, when hotelId is given, checks the hotel can use the service.
     **/
    public int preHandle(String authorization, String customerId, String hotelId) {

    	int customerAuth = authenticateCustomer(authorization, customerId);
    	if (customerAuth != Constants.SUCCESS)
    		return customerAuth;

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

    /**
     * Only validates the token and that it was issued for customerId, no database access.
     */
    public int authenticateCustomer(String authorization, String customerId) {

    	/* if no authorization in the header */
        if (authorization == null || authorization.trim().equals("")) {
    		return Constants.REQUIRED_HEADER_805;
        }

    	/* if no customerId in the header */
        if (customerId == null || customerId.trim().equals("")) {
    		return Constants.REQUIRED_CUSTOMER_807;
        }

        final String tokenType = Credentials.getAuthTokenType();

        /* check if token in the header exist */
        if (!authorization.startsWith(tokenType)) {
    		return Constants.INVALID_HEADER_806;
        }

        String token = authorization.substring(tokenType.length()).trim();
        if (token.isEmpty())
        	return Constants.INVALID_HEADER_806;

        JSONObject body = new JSONObject();
        body.put("token", token);

        HttpHeaders serviceHeaders = new HttpHeaders();
        serviceHeaders.setContentType(MediaType.APPLICATION_JSON);
        serviceHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String result;
        try {
        	result = restTemplate.postForObject(Credentials.getAuthUrl(), new HttpEntity<>(body.toString(), serviceHeaders), String.class);
        } catch (Exception e) {
        	LOG.warn("Authentication service call failed: {}", e.getMessage());
    		return Constants.ERROR_GENERATING_TOKEN_812;
		}

        try {
	        JSONObject json = new JSONObject(result);

	        if (!json.optBoolean("success", false)) {
	        	JSONArray errors = json.optJSONArray("errors");
	        	if (errors != null && errors.length() > 0 && errors.optJSONObject(0) != null && errors.getJSONObject(0).has("errorCode"))
	        		return errors.getJSONObject(0).getInt("errorCode");
	        	return Constants.ERROR_GENERATING_TOKEN_814;
	        }

	        String tokenCustomerId = json.getJSONObject("data").getString("username");
	        if (!customerId.equals(tokenCustomerId)) {
	    		return Constants.INVALID_TOKEN_CUSTOMER_809;
	        }
        } catch (Exception e) {
        	LOG.warn("Unexpected authentication service response");
        	return Constants.ERROR_GENERATING_TOKEN_814;
        }

        return Constants.SUCCESS;
    }

    public int checkHotelAuthorization(String hotelId) {

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
