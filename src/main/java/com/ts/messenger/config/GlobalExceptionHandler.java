package com.ts.messenger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.ts.messenger.utils.Constants;
import com.ts.messenger.utils.Response;

/**
 * Keeps the usual Response envelope for errors and never exposes internal details to clients.
 */
@RestControllerAdvice(basePackages = "com.ts.messenger.controller")
public class GlobalExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler({ ServletRequestBindingException.class, HttpMediaTypeException.class, HttpRequestMethodNotSupportedException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Response badRequest(Exception e) {
		return new Response(false, Constants.ERROR, "Invalid request.");
	}

	@ExceptionHandler({ MaxUploadSizeExceededException.class, MultipartException.class })
	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	public Response uploadError(Exception e) {
		return new Response(false, Constants.ERROR, "Images should be maximum 5 MB each.");
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Response unexpected(Exception e) {
		LOG.error("Unexpected API error", e);
		return new Response(false, Constants.INTERNAL_ERROR_203, "Internal error, please try again.");
	}
}
