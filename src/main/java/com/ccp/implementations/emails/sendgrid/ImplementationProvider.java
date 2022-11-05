package com.ccp.implementations.emails.sendgrid;

import com.ccp.dependency.injection.CcpImplementationProvider;

public class ImplementationProvider implements CcpImplementationProvider {

	@Override
	public Object getImplementation() {
		return new EmailSenderSendGrid();
	}

}
