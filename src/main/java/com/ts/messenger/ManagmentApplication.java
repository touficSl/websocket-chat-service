package com.ts.messenger;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ts.messenger.authentication.Interceptor;
import com.ts.messenger.multi.tenant.CustomRoutingDataSource;
import com.ts.messenger.multi.tenant.TenantDataSource;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableJpaAuditing
@EnableScheduling 
@ComponentScan(basePackages = "com.ts.messenger")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.ts.messenger")
@EnableAsync
public class ManagmentApplication extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ManagmentApplication.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(ManagmentApplication.class, args);
	} 

	@Bean
    public DataSource dataSource(){
        CustomRoutingDataSource customDataSource = new CustomRoutingDataSource();
        customDataSource.setTargetDataSources(TenantDataSource.getDataSourceHashMap());
        return customDataSource;
    } 

	@Bean
	public Interceptor Interceptor() {
	       return new Interceptor();
	}
}