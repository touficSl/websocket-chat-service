package com.ts.messenger.multi.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.ts.messenger.authentication.Credentials;
 
public class CustomRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    public Object determineCurrentLookupKey() {
    	return RequestContext.getCustomerId() != null ? RequestContext.getCustomerId() : Credentials.getDefaultCustomerId();
    }
}