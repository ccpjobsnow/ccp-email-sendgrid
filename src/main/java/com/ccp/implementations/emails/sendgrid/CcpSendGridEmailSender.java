package com.ccp.implementations.emails.sendgrid;

import com.ccp.dependency.injection.CcpInstanceProvider;

public class CcpSendGridEmailSender implements CcpInstanceProvider {

	@Override
	public Object getInstance() {
		return new SendGridEmailSender();
	}

}
