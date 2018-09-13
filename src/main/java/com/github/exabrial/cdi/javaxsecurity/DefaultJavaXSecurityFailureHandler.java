package com.github.exabrial.cdi.javaxsecurity;

import java.lang.reflect.Method;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

@ApplicationScoped
public class DefaultJavaXSecurityFailureHandler implements JavaXSecurityFailureHandler {
	@Inject
	private Instance<Logger> logInstance;
	@Inject
	private HttpServletRequest request;

	@Override
	public void authenticationFailure() {
		if (!logInstance.isUnsatisfied()) {
			String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			String decoded;
			try {
				final String base64Encoded = authHeader.substring(authHeader.indexOf(" ") + 1, authHeader.length());
				decoded = new String(Base64.getDecoder().decode(base64Encoded));
			} catch (Exception e) {
				decoded = null;
			}
			logInstance.get().warn("authenticationFailure() remoteAddr:{} authorization header:{}[{}]", request.getRemoteAddr(), authHeader,
					decoded);
		}
		throw new WebApplicationException(Response.Status.UNAUTHORIZED);
	}

	@Override
	public void authorizationFailure(Class<? extends Object> targetClass, Method targetMethod, String roleName) {
		if (!logInstance.isUnsatisfied()) {
			logInstance.get().warn("authorizationFailure() user:{} is authenticated, but doesn't have role:{} required for invoking:{}:{}",
					request.getRemoteUser(), roleName, targetClass.getName(), targetMethod.getName());
		}
		throw new WebApplicationException(Response.Status.UNAUTHORIZED);
	}
}
