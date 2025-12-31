package com.ts.messenger.ws;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class WSHandshakeInterceptor implements HandshakeInterceptor {
	
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
    	System.out.println("beforeHandshake");
    	return true;
    }


    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
    	System.out.println("afterHandshake");
    	try {
	    	HttpHeaders header = serverHttpRequest.getHeaders();
//	        String client = header.get("client-id").get(0);
	        String sessionId = null;
	        if (serverHttpRequest instanceof ServletServerHttpRequest) {
	            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) serverHttpRequest;
	            HttpSession session = servletRequest.getServletRequest().getSession();
	            sessionId = session.getId();
	            System.out.println("Session ID + "+sessionId);
//	            System.out.println("CLIENT ID " + client);
	//            ClassChangeNotificationServiceImpl.clientSessionMap.put(client, sessionId);
	        }
    	} catch(Exception xx) {
    		xx.printStackTrace();
    	}
    }
}