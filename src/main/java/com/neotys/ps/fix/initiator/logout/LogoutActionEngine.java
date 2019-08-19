package com.neotys.ps.fix.initiator.logout;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.ps.fix.common.FIXLogger;
import com.neotys.ps.fix.common.NeoLoadFIXHandler;
import com.neotys.ps.fix.common.NeoLoadUtils;
import quickfix.ConfigError;

import java.io.FileNotFoundException;
import java.util.List;

import static com.neotys.ps.fix.common.NeoLoadUtils.appendLineToStringBuilder;

public final class LogoutActionEngine implements ActionEngine {

	private String sessionName;			//Name of the FIX session

	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {

		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();

		//Get the session name
		parseParameters(context,parameters);

		//Get the application
		NeoLoadFIXHandler neoLoadFIXHandler = (NeoLoadFIXHandler) context.getCurrentVirtualUser().get(sessionName);

		appendLineToStringBuilder(requestBuilder, "---------------Parameters---------------");
		appendLineToStringBuilder(requestBuilder, "Session: " + sessionName);
		appendLineToStringBuilder(requestBuilder, "-----------Session Settings----------");
		appendLineToStringBuilder(requestBuilder, neoLoadFIXHandler.getConnector().getSessions().get(0).toString());
		sampleResult.setRequestContent(requestBuilder.toString());

		//Logout
		try {
			sampleResult.setDuration(neoLoadFIXHandler.logout(sampleResult));
		} catch (InterruptedException e) {
			NeoLoadUtils.throwNeoLoadError(neoLoadFIXHandler.getFixApplication().getLogger(),sampleResult,"FIX-LOGOUT","Couldn't logout",e);
		}

		appendLineToStringBuilder(responseBuilder, "Session logged out");

		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	@Override
	public void stopExecute() {
		// TODO add code executed when the test have to stop.
	}

	private void parseParameters(Context context, List<ActionParameter> parameters) {

		for (ActionParameter temp : parameters) {
			switch (temp.getName().toLowerCase()) {
				case "sessionname":
					sessionName = temp.getValue();
					break;
				default:
					break;
			}
		}
	}

}
