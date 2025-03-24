package com.ccp.implementations.email.sendgrid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.constantes.CcpStringConstants;
import com.ccp.decorators.CcpEmailDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.exceptions.email.CcpInvalidEmailAdresses;
import com.ccp.http.CcpHttpMethods;

class SendGridEmailSender implements CcpEmailSender {

	public CcpJsonRepresentation send(CcpJsonRepresentation emailApiParameters) {
		String apiTokenKeyName = emailApiParameters.getAsString("token");

		String apiUrlKeyName = emailApiParameters.getAsString("url");

		String message = emailApiParameters.getAsString("message");

		String subject = emailApiParameters.getAsString("subject");

		String sender = emailApiParameters.getAsString("sender");
		
		String format = emailApiParameters.getAsString("format");

		CcpHttpMethods method = CcpHttpMethods.valueOf(emailApiParameters.getAsString("method"));

		List<String> recipients = emailApiParameters.getAsStringList("emails", CcpStringConstants.EMAIL.value);

		if(format.trim().isEmpty()) {
			format = "text/html";
		}
		
		CcpJsonRepresentation systemProperties = new CcpStringDecorator("application_properties").propertiesFrom().environmentVariablesOrClassLoaderOrFile();
		
		String sendgridApiKey =  systemProperties.getAsString(apiTokenKeyName);
		String sendgridApiUrl =  systemProperties.getAsString(apiUrlKeyName);

		CcpHttpHandler ccpHttpHandler = new CcpHttpHandler(202);
		
		CcpJsonRepresentation headers = CcpOtherConstants.EMPTY_JSON
				.put("Authorization", "Bearer " + sendgridApiKey)
				.put("User-agent", "sendgrid/3.0.0;java")
				.put("Accept", "application/json")
		;
		
		String[] emails = recipients.toArray(new String[recipients.size()]);

		List<CcpJsonRepresentation> personalizations = this.getPersonalizations(emails);
		
		CcpJsonRepresentation body = CcpOtherConstants.EMPTY_JSON
				.addToItem("from", CcpStringConstants.EMAIL.value, sender)
				.put("subject", subject)
				.put("personalizations", personalizations)
				.addToList("content", CcpOtherConstants.EMPTY_JSON.put("type", format).put("value", message))
				;
		
//		this.throwFakeServerErrorToTestingProcessFlow();
		ccpHttpHandler.executeHttpRequest("sendEmail", sendgridApiUrl, method, headers, body, CcpHttpResponseType.singleRecord);
		return CcpOtherConstants.EMPTY_JSON;
	}

	private List<CcpJsonRepresentation> getPersonalizations(String... emails) {
		
		List<String> list = Arrays.asList(emails);
		List<CcpEmailDecorator> invalidEmails = list.stream().map(email -> new CcpStringDecorator(email).email()).filter(x -> x.isValid() == false).collect(Collectors.toList());
		boolean hasInvalidEmails = invalidEmails.isEmpty() == false;
		
		if(hasInvalidEmails) {
			throw new CcpInvalidEmailAdresses(invalidEmails);
		}
		
		List<Map<String, Object>> to = list.stream().map(email -> CcpOtherConstants.EMPTY_JSON.put(CcpStringConstants.EMAIL.value,email).content).collect(Collectors.toList());
		List<CcpJsonRepresentation> asList = Arrays.asList( CcpOtherConstants.EMPTY_JSON.put("to", to));
		return asList;
	}
	
}


