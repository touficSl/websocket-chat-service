package com.ts.messenger.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

	@Bean
	public Docket postsApi() {
		
		return new Docket(DocumentationType.SWAGGER_2)
	            .select()
	            .apis(RequestHandlerSelectors.basePackage("com.ts.messenger.controller"))
	            .paths(PathSelectors.any())
	            .build()
	            .apiInfo(apiInfo());
	}
	
//	private Predicate<String> publicAPIs() {
//		return or(regex("/public/.*"), regex("/common/.*"));
//	}

//	private Predicate<String> postPaths() {
////		return or(regex("/api/posts.*"), regex("/api/javainuse.*"));
//		return regex("/\\{customer\\}/.*");
//	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Messenger")
				.description("Messenger Apis")
				.contact(new Contact("TS Support", "", "ts.support@gmail.com"))
//				.license("License")
//				.licenseUrl("License")
				.version("1.0").build();
	}
	
}
