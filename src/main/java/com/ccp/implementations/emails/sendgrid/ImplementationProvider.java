package com.ccp.implementations.emails.sendgrid;

import com.ccp.dependency.injection.CcpEspecification.DefaultImplementationProvider;

public class ImplementationProvider extends DefaultImplementationProvider {

	@Override
	public Object getImplementation() {
		return new EmailSenderSendGrid();
	}

}
