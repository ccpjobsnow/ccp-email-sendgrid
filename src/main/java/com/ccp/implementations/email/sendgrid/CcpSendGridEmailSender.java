package com.ccp.implementations.email.sendgrid;

import com.ccp.dependency.injection.CcpInstanceProvider;

public class CcpSendGridEmailSender implements CcpInstanceProvider {

	
	public Object getInstance() {
		return new SendGridEmailSender();
	}

}
