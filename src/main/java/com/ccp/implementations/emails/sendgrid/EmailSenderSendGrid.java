package com.ccp.implementations.emails.sendgrid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpMapDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInject;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.exceptions.http.UnexpectedHttpStatus;

class EmailSenderSendGrid implements CcpEmailSender {

	@CcpDependencyInject
	private CcpHttpRequester ccpHttp;
	
	public void send(CcpMapDecorator emailParameters) {

		String emailTo = emailParameters.getAsString("email");

		boolean isInvalidEmail = new CcpStringDecorator(emailTo).email().isValid() == false;
		
		if (isInvalidEmail) {
			return;
		}
		
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

		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(202, this.ccpHttp);
		
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
			ccpHttpHandler.executeHttpRequest(sendgridApiUrl, "POST", headers, body, CcpHttpResponseType.string);
		} catch (UnexpectedHttpStatus e) {
			if(e.response.httpStatus < 500) {
				throw new ThereWasClientError(e.response);
			}

			if(e.response.httpStatus > 599) {
				throw new ThereWasClientError(e.response);
				
			}
			throw new EmailApiIsUnavailable();
		}
	}

	
	private List<CcpMapDecorator> getPersonalizations(CcpMapDecorator emailParameters) {
		
		//{"to":[{"email":"onias@noxxonsat.com.br"}]}
	
		List<String> emails = emailParameters.getAsStringList("emails", "email");
		
		
		List<Map<String, Object>> to = emails.stream().map(email -> new CcpMapDecorator().put("email",email).content).collect(Collectors.toList());
		List<CcpMapDecorator> asList = Arrays.asList( new CcpMapDecorator().put("to", to));
		return asList;
	}
	
}


