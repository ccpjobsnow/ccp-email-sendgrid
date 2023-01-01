package com.ccp.implementations.emails.sendgrid;

import java.util.List;
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
		
		String sendgridUserAgent =  emailParameters.getAsString("apiUserAgent");
		String sendgridApiMethod =  emailParameters.getAsString("apiMethod");
		String sendgridApiKey =  emailParameters.getAsString("apiEmailKey");
		String sendgridApiUrl =  emailParameters.getAsString("apiUrl");

		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(202, this.ccpHttp);
		
		CcpMapDecorator headers = new CcpMapDecorator()
				.put("Authorization", sendgridApiKey)
				.put("User-agent", sendgridUserAgent)
				.put("Accept", "application/json")
				
		;
		
		CcpMapDecorator personalizations = this.getPersonalizations(emailParameters);
		
		CcpMapDecorator body = new CcpMapDecorator()
				.addToItem("from", "email", sendgridSender)
				.put("subject", subject)
				.put("personalizations", personalizations)
				.addToItem("content", "type", format)
				.addToItem("content", "value", message)
				;
		
		try {
			ccpHttpHandler.executeHttpRequest(sendgridApiUrl, sendgridApiMethod, headers, body, CcpHttpResponseType.string);
		} catch (UnexpectedHttpStatus e) {
			throw new EmailWasNotSent(e.response.httpResponse);
		}
	}

	
	private CcpMapDecorator getPersonalizations(CcpMapDecorator emailParameters) {
		List<String> emails = emailParameters.getAsStringList("emails");
		List<CcpMapDecorator> to = emails.stream().map(email -> new CcpMapDecorator().put("email",email)).collect(Collectors.toList());
		return new CcpMapDecorator().addToList("to", to);
	}
	
}


