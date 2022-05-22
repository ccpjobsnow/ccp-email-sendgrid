package com.ccp.implementations.emails.sendgrid;

import java.io.IOException;

import com.ccp.decorators.CcpMapDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpImplementation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.utils.Utils;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@CcpImplementation
public class CcpEmailSenderSendGrid implements CcpEmailSender {

	@Override
	public void send(String subject, String emailTo, String message, String format) {
		if (new CcpStringDecorator(emailTo).email().isValid() == false) {
			return;
		}
		String sendgridSender = System.getenv("SENDGRID_SENDER");
		String sendgridApiKey = System.getenv("SENDGRID_KEY");

		Email to = new Email(emailTo);
		Email from = new Email(sendgridSender);
		Content content = new Content(format, message);
		Mail mail = new Mail(from, subject, to, content);

		// Instantiates SendGrid client.
		SendGrid sendgrid = new SendGrid(sendgridApiKey);

		// Instantiate SendGrid request.
		Request request = new Request();

		try {
			// Set request configuration.
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());

			// Use the client to send the API request.

			Response response;
			try {
				response = sendgrid.api(request);
			} catch (Exception e) {
				System.out.println("Erro de rede, repetindo novamente tentativa de enviar e-mail");
				Utils.sleep(1000);
				this.send(subject, emailTo, message, format);
				return;
			}

			if (response.getStatusCode() != 202) {
				System.out.println("Aconteceu algum erro, pois o sendGrid retornou " + response.getStatusCode());
				return;
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public String sendFailure(Throwable e) {
		String msg = new CcpMapDecorator(e).asPrettyJson();
		String hash = new CcpStringDecorator(msg).hash().asString("SHA1");
		String email = System.getenv("SUPPORT_EMAIL");
		this.send(email, "Erro: " + e.getMessage(), msg);
		return hash;
	}
	
}
