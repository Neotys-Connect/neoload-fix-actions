package com.neotys.ps.fix.acceptor.logon;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.ps.fix.common.FIXLogger;
import com.neotys.ps.fix.common.NeoLoadFIXHandler;
import com.neotys.ps.fix.common.NeoLoadUtils;
import quickfix.ConfigError;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.neotys.ps.fix.common.NeoLoadUtils.appendLineToStringBuilder;

public final class AcceptorLogonActionEngine implements ActionEngine {

	private String sessionFilePath;		//Configuration file path

	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {

		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		FIXLogger logger = new FIXLogger(context.getLogger(),context.getCurrentVirtualUser().getId());

		try {
			//Get session setting
			SessionSettings settings = parseParameters(context,parameters);

			appendLineToStringBuilder(requestBuilder, "-----------Config File----------");
			appendLineToStringBuilder(requestBuilder, sessionFilePath);
			appendLineToStringBuilder(requestBuilder, "-----------Connection Settings----------");
			appendLineToStringBuilder(requestBuilder, settings.toString());
			sampleResult.setRequestContent(requestBuilder.toString());

			//Instantiate the acceptor
			NeoLoadFIXHandler neoLoadFIXHandler = new NeoLoadFIXHandler(logger,settings);

			appendLineToStringBuilder(responseBuilder, "Acceptor started");

			for (SessionID sessionID : neoLoadFIXHandler.getConnector().getSessions()) {
				appendLineToStringBuilder(responseBuilder,sessionID.toString());
			}

			context.getCurrentVirtualUser().put("fixSession",neoLoadFIXHandler);

		} catch (Exception e) {
			sampleResult.setResponseContent(responseBuilder.toString());
			return NeoLoadUtils.throwNeoLoadError(logger.getLogger(),sampleResult,"FIX-Acceptor-Logon",e.getMessage(),e);
		}

		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	@Override
	public void stopExecute() {
		// TODO add code executed when the test have to stop.
	}

	private SessionSettings parseParameters(Context context, List<ActionParameter> parameters) throws IOException, ConfigError {

		for (ActionParameter temp : parameters) {
			switch (temp.getName().toLowerCase()) {
				case "settingsfile":
					sessionFilePath = context.getVariableManager().parseVariables(temp.getValue());
					break;
				default:
					break;
			}
		}

		return new SessionSettings(new FileInputStream(sessionFilePath));
	}

}
