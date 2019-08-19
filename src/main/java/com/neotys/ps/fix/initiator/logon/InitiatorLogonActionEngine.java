package com.neotys.ps.fix.initiator.logon;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.ps.fix.common.FIXLogger;
import com.neotys.ps.fix.common.NeoLoadFIXHandler;
import com.neotys.ps.fix.common.NeoLoadUtils;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.neotys.ps.fix.common.NeoLoadUtils.appendLineToStringBuilder;

public final class InitiatorLogonActionEngine implements ActionEngine {

	private String sessionName;			//Name of the FIX session
	private String sessionFilePath;		//Configuration file path
    private long logonTimeout;          //Timeout to logon the initiator
	private String senderCompID;		//SenderCompID to override cfg file

	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {

		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		FIXLogger logger = new FIXLogger(context.getLogger(),context.getCurrentVirtualUser().getId());

		try {
			//Get session setting
			SessionSettings settings = parseParameters(context,parameters);

			appendLineToStringBuilder(requestBuilder, "---------------Parameters---------------");
			appendLineToStringBuilder(requestBuilder, "Session: " + sessionName);
			appendLineToStringBuilder(requestBuilder, "Logon Timeout: " + logonTimeout + " ms");
			appendLineToStringBuilder(requestBuilder, "-----------Connection Settings----------");
			appendLineToStringBuilder(requestBuilder, settings.toString());
			sampleResult.setRequestContent(requestBuilder.toString());

			//Instantiate the client and logon
			NeoLoadFIXHandler neoLoadFIXHandler = new NeoLoadFIXHandler(logger,settings,logonTimeout,sampleResult);

			appendLineToStringBuilder(responseBuilder, "Session opened: " + neoLoadFIXHandler.getConnector().getSessions().get(0).toString());

			context.getCurrentVirtualUser().put(sessionName,neoLoadFIXHandler);
			context.getCurrentVirtualUser().put("lineCount", (Integer) 1);

		} catch (Exception e) {
			sampleResult.setResponseContent(responseBuilder.toString());
			return NeoLoadUtils.throwNeoLoadError(logger.getLogger(),sampleResult,"FIX-Initiator-Logon",e.getMessage(),e);
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
				case "sessionname":
					sessionName = temp.getValue();
					break;
				case "settingsfile":
					sessionFilePath = temp.getValue();
					break;
                case "logontimeout":
                    logonTimeout = Long.parseLong(temp.getValue())*1000;
                    break;
				case "sendercompid":
					senderCompID = temp.getValue();
					break;
				default:
					break;
			}
		}

		if (logonTimeout == 0){
		    logonTimeout=500000;
        }

		SessionSettings settings = new SessionSettings(new FileInputStream(context.getVariableManager().parseVariables(sessionFilePath)));

		if (senderCompID != null){
			settings.setString(SessionSettings.SENDERCOMPID,senderCompID);
		}

		return settings;
	}

}
