package com.ccp.implementations.emails.sendgrid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInject;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.exceptions.http.CcpHttpClientError;
import com.ccp.exceptions.http.CcpHttpError;
import com.ccp.exceptions.http.CcpHttpUnexpectedStatus;
import com.ccp.process.ThrowException;

class EmailSenderSendGrid implements CcpEmailSender {

	@CcpDependencyInject
	private CcpHttpRequester ccpHttp;
	
	public CcpMapDecorator send(CcpMapDecorator emailParameters) {

		
		String sendgridSender = emailParameters.getAsString("sender");
		String format = emailParameters.getAsString("format");
		
		if(format.trim().isEmpty()) {
			format = "text/html";
		}
		
		String message = emailParameters.getAsString("message");
		String subject = emailParameters.getAsString("subject");
		CcpMapDecorator systemProperties = new CcpStringDecorator("application.properties").propertiesFileFromClassLoader();
		
		String sendgridApiKey =  systemProperties.getAsString("sendgridApiKey");
		String sendgridApiUrl =  systemProperties.getAsString("sendGridSendEmailUrl");

		CcpMapDecorator handlers = new CcpMapDecorator()
				.put("401", new ThrowException(new RuntimeException("The api key '" + sendgridApiKey + "' is invalid in the SendGrid API")))
				.put("404", new ThrowException(new RuntimeException("The url '" + sendgridApiUrl + "' doesn't exist in the SendGrid API" )))
				.put("202", CcpConstants.DO_NOTHING)
				;

		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(handlers, this.ccpHttp);
		
		CcpMapDecorator headers = new CcpMapDecorator()
				.put("Authorization", "Bearer " + sendgridApiKey)
				.put("User-agent", "sendgrid/3.0.0;java")
				.put("Accept", "application/json")
		;
		
		List<CcpMapDecorator> personalizations = this.getPersonalizations(emailParameters);
		
		CcpMapDecorator body = new CcpMapDecorator()
				.addToItem("from", "email", sendgridSender)
				.put("subject", subject)
				.put("personalizations", personalizations)
				.addToList("content", new CcpMapDecorator().put("type", format).put("value", message))
				;
		
		try {
//			this.throwErrorTest(sendgridApiKey, sendgridApiUrl, headers, body);
			ccpHttpHandler.executeHttpRequest(sendgridApiUrl, "POST", headers, body, CcpHttpResponseType.singleRecord);
			CcpHttpClientError data = this.getData(sendgridApiKey, sendgridApiUrl, headers, body);
			CcpMapDecorator asEntity = data.entity;
			return asEntity;
		} catch (CcpHttpUnexpectedStatus e) {
			
			if(e.response.httpStatus < 500) {
				throw this.throwException(sendgridApiKey, sendgridApiUrl, headers, body, e);

			}

			if(e.response.httpStatus > 599) {
				throw this.throwException(sendgridApiKey, sendgridApiUrl, headers, body, e);
			}
			throw this.throwException(sendgridApiKey, sendgridApiUrl, headers, body, e);
		}

	}


	private CcpHttpError throwException(String sendgridApiKey, String sendgridApiUrl, CcpMapDecorator headers,
			CcpMapDecorator body, CcpHttpUnexpectedStatus e) {
		CcpHttpError addDetail2 = this.getException(sendgridApiKey, sendgridApiUrl, headers, body);
		CcpHttpError addDetail = addDetail2
		.addDetail("httpResponse", e.response.httpResponse)
		.addDetail("httpStatus", e.response.httpStatus);
		
		return addDetail;
	}


	private CcpHttpError getException(String sendgridApiKey, String sendgridApiUrl, CcpMapDecorator headers,
			CcpMapDecorator body) {
		CcpHttpError addDetail2 = new CcpHttpClientError()
				.addDetail("sendgridApiUrl", sendgridApiUrl)
				.addDetail("sendgridApiKey", sendgridApiKey)
				.addDetail("headers", headers)
				.addDetail("body", body);
		return addDetail2;
	}
	

	void throwErrorTest(String sendgridApiKey, String sendgridApiUrl, CcpMapDecorator headers, CcpMapDecorator body) {
		CcpHttpClientError ex = this.getData(sendgridApiKey, sendgridApiUrl, headers, body);
		throw ex;
	}


	private CcpHttpClientError getData(String sendgridApiKey, String sendgridApiUrl, CcpMapDecorator headers, CcpMapDecorator body) {
		throw this.getException(sendgridApiKey, sendgridApiUrl, headers, body);
	}

	
	private List<CcpMapDecorator> getPersonalizations(CcpMapDecorator emailParameters) {
		
		List<String> emails = emailParameters.getAsStringList("emails", "email");
		List<CcpEmailDecorator> invalidEmails = emails.stream().map(email -> new CcpStringDecorator(email).email()).filter(x -> x.isValid() == false).collect(Collectors.toList());
		boolean hasInvalidEmails = invalidEmails.isEmpty() == false;
		if(hasInvalidEmails) {
			throw new RuntimeException("some mail address are not valids: " + invalidEmails);
		}
		
		List<Map<String, Object>> to = emails.stream().map(email -> new CcpMapDecorator().put("email",email).content).collect(Collectors.toList());
		List<CcpMapDecorator> asList = Arrays.asList( new CcpMapDecorator().put("to", to));
		return asList;
	}
	
}


