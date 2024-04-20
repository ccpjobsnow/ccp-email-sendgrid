package com.ccp.implementations.email.sendgrid;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.email.CcpEmailSender;

public class CcpSendGridEmailSender implements CcpInstanceProvider<CcpEmailSender> {
	
	public CcpEmailSender getInstance() {
		return new SendGridEmailSender();
	}

}
