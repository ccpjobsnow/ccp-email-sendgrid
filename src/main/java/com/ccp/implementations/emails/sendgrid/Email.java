package com.ccp.implementations.emails.sendgrid;

import com.ccp.dependency.injection.CcpInstanceProvider;

public class Email implements CcpInstanceProvider {

	@Override
	public Object getInstance() {
		return new EmailSenderSendGrid();
	}

}
