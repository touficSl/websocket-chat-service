package com.ts.messenger.multi.tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ts.entities.NCICustomerEntity;
import com.ts.messenger.authentication.Credentials;
import com.ts.services.NCICustomerService;
import com.ts.utils.NCIResponse;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Builds one pooled data source per customer (tenant) registered in the common database.
 */
public final class TenantDataSource {

	private TenantDataSource() {}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getDataSourceHashMap() {
		NCIResponse nciResponse = NCICustomerService.getCustomers(Credentials.isDebugMode(), Credentials.getCommonDatabaseUrl(), Credentials.getCommonDatabaseUser(), Credentials.getCommonDatabaseDriver(), Credentials.getCommonDatabasePassword(), Credentials.getCommonDatabaseCatalog());
		if (nciResponse == null || !nciResponse.isSuccess() || !(nciResponse.getData() instanceof List))
			throw new IllegalStateException("Unable to load customers from the common database");

		List<NCICustomerEntity> clientDetails = (List<NCICustomerEntity>) nciResponse.getData();
		Map<Object, Object> result = new HashMap<>();
		for (NCICustomerEntity nciCustomerEntity : clientDetails) {

			/* Pool is created lazily on first use, no connection is opened at startup */
			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setPoolName("tenant-" + nciCustomerEntity.getCustomerId());
			if (nciCustomerEntity.getDatabaseDriver() != null && !nciCustomerEntity.getDatabaseDriver().trim().isEmpty())
				dataSource.setDriverClassName(nciCustomerEntity.getDatabaseDriver().trim());
			dataSource.setJdbcUrl(nciCustomerEntity.getDatabaseUrl());
			dataSource.setUsername(nciCustomerEntity.getDatabaseUser());
			dataSource.setPassword(nciCustomerEntity.getDatabasePassword());
			dataSource.setMaximumPoolSize(Credentials.getTenantPoolMaxSize());
			dataSource.setMinimumIdle(0);
			dataSource.setIdleTimeout(120_000);

			result.put(nciCustomerEntity.getCustomerId(), dataSource);
		}
		return result;
	}

}
