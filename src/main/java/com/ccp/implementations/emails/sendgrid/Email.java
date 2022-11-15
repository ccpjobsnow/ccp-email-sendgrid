package com.ccp.implementations.emails.sendgrid;

import com.ccp.dependency.injection.CcpModuleExporter;

public class Email implements CcpModuleExporter {

	@Override
	public Object export() {
		return new EmailSenderSendGrid();
	}

}
