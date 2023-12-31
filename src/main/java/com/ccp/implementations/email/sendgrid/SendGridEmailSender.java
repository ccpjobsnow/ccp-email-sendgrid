package com.ccp.implementations.email.sendgrid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpConstants;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.exceptions.http.CcpHttpError;

class SendGridEmailSender implements CcpEmailSender {

	public CcpJsonRepresentation send(CcpJsonRepresentation emailApiParameters) {
		String apiTokenKeyName = emailApiParameters.getAsString("token");

		String apiUrlKeyName = emailApiParameters.getAsString("url");

		String message = emailApiParameters.getAsString("message");

		String subject = emailApiParameters.getAsString("subject");

		String sender = emailApiParameters.getAsString("sender");
		
		String format = emailApiParameters.getAsString("format");

		String method = emailApiParameters.getAsString("method");

		List<String> recipients = emailApiParameters.getAsStringList("emails", "email");

		if(format.trim().isEmpty()) {
			format = "text/html";
		}
		
		CcpJsonRepresentation systemProperties = new CcpStringDecorator("application.properties").propertiesFrom().environmentVariablesOrClassLoaderOrFile();
		
		String sendgridApiKey =  systemProperties.getAsString(apiTokenKeyName);
		String sendgridApiUrl =  systemProperties.getAsString(apiUrlKeyName);

		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(202);
		
		CcpJsonRepresentation headers = CcpConstants.EMPTY_JSON
				.put("Authorization", "Bearer " + sendgridApiKey)
				.put("User-agent", "sendgrid/3.0.0;java")
				.put("Accept", "application/json")
		;
		
		String[] emails = recipients.toArray(new String[recipients.size()]);

		List<CcpJsonRepresentation> personalizations = this.getPersonalizations(emails);
		
		CcpJsonRepresentation body = CcpConstants.EMPTY_JSON
				.addToItem("from", "email", sender)
				.put("subject", subject)
				.put("personalizations", personalizations)
				.addToList("content", CcpConstants.EMPTY_JSON.put("type", format).put("value", message))
				;
		
//		this.throwFakeServerErrorToTestingProcessFlow();
		ccpHttpHandler.executeHttpRequest(sendgridApiUrl, method, headers, body, CcpHttpResponseType.singleRecord);
		return CcpConstants.EMPTY_JSON;
	}

	void throwFakeServerErrorToTestingProcessFlow() {
		throw new CcpHttpError("url", "POST", CcpConstants.EMPTY_JSON, "", 500, "", new HashSet<>());
	}

	private List<CcpJsonRepresentation> getPersonalizations(String... emails) {
		
		List<String> list = Arrays.asList(emails);
		List<CcpEmailDecorator> invalidEmails = list.stream().map(email -> new CcpStringDecorator(email).email()).filter(x -> x.isValid() == false).collect(Collectors.toList());
		boolean hasInvalidEmails = invalidEmails.isEmpty() == false;
		if(hasInvalidEmails) {
			throw new RuntimeException("some mail addresses are not valids: " + invalidEmails);
		}
		
		List<Map<String, Object>> to = list.stream().map(email -> CcpConstants.EMPTY_JSON.put("email",email).content).collect(Collectors.toList());
		List<CcpJsonRepresentation> asList = Arrays.asList( CcpConstants.EMPTY_JSON.put("to", to));
		return asList;
	}
	
}


