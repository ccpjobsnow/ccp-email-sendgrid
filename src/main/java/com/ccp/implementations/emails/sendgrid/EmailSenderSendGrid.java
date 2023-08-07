package com.ccp.implementations.emails.sendgrid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpMapDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInject;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.exceptions.http.CcpHttpError;
enum X{
	email
}
class EmailSenderSendGrid implements CcpEmailSender {

	@CcpDependencyInject
	private CcpHttpRequester ccpHttp;
	
	public CcpMapDecorator send(CcpMapDecorator emailApiParameters) {
		String apiTokenKeyName = emailApiParameters.getAsString("apiTokenKeyName");

		String apiUrlKeyName = emailApiParameters.getAsString("apiUrlKeyName");

		String message = emailApiParameters.getAsString("emailMessage");

		String subject = emailApiParameters.getAsString("subject");

		String sender = emailApiParameters.getAsString("sender");
		
		String format = emailApiParameters.getAsString("format");

		List<String> recipients = emailApiParameters.getAsStringList("emails", "email");

		if(format.trim().isEmpty()) {
			format = "text/html";
		}
		
		CcpMapDecorator systemProperties = new CcpStringDecorator("application.properties").propertiesFileFromClassLoader();
		
		String sendgridApiKey =  systemProperties.getAsString(apiTokenKeyName);
		String sendgridApiUrl =  systemProperties.getAsString(apiUrlKeyName);


		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(202, this.ccpHttp);
		
		CcpMapDecorator headers = new CcpMapDecorator()
				.put("Authorization", "Bearer " + sendgridApiKey)
				.put("User-agent", "sendgrid/3.0.0;java")
				.put("Accept", "application/json")
		;
		
		String[] emails = recipients.toArray(new String[recipients.size()]);

		List<CcpMapDecorator> personalizations = this.getPersonalizations(emails);
		
		CcpMapDecorator body = new CcpMapDecorator()
				.addToItem("from", "email", sender)
				.put("subject", subject)
				.put("personalizations", personalizations)
				.addToList("content", new CcpMapDecorator().put("type", format).put("value", message))
				;
		
//		this.throwFakeServerErrorToTestingProcessFlow();
		ccpHttpHandler.executeHttpRequest(sendgridApiUrl, "POST", headers, body, CcpHttpResponseType.singleRecord, X.email);
		return new CcpMapDecorator();
	}

	void throwFakeServerErrorToTestingProcessFlow() {
		throw new CcpHttpError("url", "POST", new CcpMapDecorator(), "", X.email, 500, "", new HashSet<>());
	}

	private List<CcpMapDecorator> getPersonalizations(String... emails) {
		
		List<String> list = Arrays.asList(emails);
		List<CcpEmailDecorator> invalidEmails = list.stream().map(email -> new CcpStringDecorator(email).email()).filter(x -> x.isValid() == false).collect(Collectors.toList());
		boolean hasInvalidEmails = invalidEmails.isEmpty() == false;
		if(hasInvalidEmails) {
			throw new RuntimeException("some mail address are not valids: " + invalidEmails);
		}
		
		List<Map<String, Object>> to = list.stream().map(email -> new CcpMapDecorator().put("email",email).content).collect(Collectors.toList());
		List<CcpMapDecorator> asList = Arrays.asList( new CcpMapDecorator().put("to", to));
		return asList;
	}
	
}


