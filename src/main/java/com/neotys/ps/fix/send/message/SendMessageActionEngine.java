package com.neotys.ps.fix.send.message;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.ps.fix.common.FIXUtils;
import com.neotys.ps.fix.common.NeoLoadFIXHandler;
import com.neotys.ps.fix.common.NeoLoadUtils;
import quickfix.Initiator;
import quickfix.InvalidMessage;
import quickfix.Session;
import quickfix.SessionID;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.neotys.ps.fix.common.NeoLoadUtils.appendLineToStringBuilder;

public final class SendMessageActionEngine implements ActionEngine {

	private String sessionName;			//Name of the FIX session
	private String messagePath;			//Path of the FIX messages to send
	private String message;				//FIX message to send

	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {

		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();

		//Get the message parameters and iteration context
		parseParameters(parameters);
		Integer iterationNumber = context.getCurrentVirtualUser().getCurrentIteration();

		List<String> lines = new ArrayList<>();

		//Get the FIX handler
		NeoLoadFIXHandler neoLoadFIXHandler = (NeoLoadFIXHandler) context.getCurrentVirtualUser().get(sessionName);

		//If we're sending messages from a file
		if (message == null){
			//Initialise the list of messages on 1st iteration or load from context in other cases
			switch (iterationNumber) {
				case 1:
					try {
						BufferedReader reader = getMessageFile(context);
						String line = reader.readLine();
						while (line != null) {
							neoLoadFIXHandler.getFixApplication().getLogger().debug("Line read from " + context.getVariableManager().parseVariables(messagePath) + ": " + line);
							lines.add(line);
							line = reader.readLine();
						}

						context.getCurrentVirtualUser().put("lineCount", lines.size());
						reader.close();

					} catch (FileNotFoundException e) {
						NeoLoadUtils.throwNeoLoadError(neoLoadFIXHandler.getFixApplication().getLogger(), sampleResult, "FIX-SENDMESSAGE", "Couldn't find file", e);
					} catch (IOException e) {
						e.printStackTrace();
						NeoLoadUtils.throwNeoLoadError(neoLoadFIXHandler.getFixApplication().getLogger(), sampleResult, "FIX-SENDMESSAGE", "Couldn't read from file", e);
					}
					break;
				default:
					//Get the messages from the context
					lines = (List<String>) context.getCurrentVirtualUser().get("messages");
					break;
			}

			//Get the number of remaining lines in the file
			Integer lineCount = (Integer) context.getCurrentVirtualUser().get("lineCount");

			//If we still have remaining messages to send from the file
			if (iterationNumber <= lineCount) {
				//Dequeue the message
				String line = lines.get(0);
				lines.remove(0);
				context.getCurrentVirtualUser().put("messages", lines);

				//Wait for the delay
				try {
					Thread.sleep(SendMessageUtils.getDelay(line));
					appendLineToStringBuilder(responseBuilder, "Waiting delay: " + SendMessageUtils.getDelay(line) + " ms");
				} catch (InterruptedException e) {
					NeoLoadUtils.throwNeoLoadError(neoLoadFIXHandler.getFixApplication().getLogger(), sampleResult, "FIX-SENDMESSAGE", "Wait delay interrupted", e);
				}

				//Load the message
				message = SendMessageUtils.getMessage(line);
			} else {
				appendLineToStringBuilder(responseBuilder, "Message number: " + iterationNumber.toString());
				appendLineToStringBuilder(responseBuilder, "No more message for VU: " + context.getCurrentVirtualUser().getId());
				context.getVariableManager().setValue("stopVU", "true");
			}
		}

		//Load the request parameters
		appendLineToStringBuilder(requestBuilder, "---------------Parameters---------------");
		appendLineToStringBuilder(requestBuilder, "Session: " + sessionName);
		appendLineToStringBuilder(requestBuilder, "-----------Session Settings----------");
		appendLineToStringBuilder(requestBuilder, neoLoadFIXHandler.getConnector().getSessions().get(0).toString());


		//If we have a message to send
		if (message != null){
			//Send the message
			try {
				appendLineToStringBuilder(responseBuilder, "---------------Message---------------");
				appendLineToStringBuilder(responseBuilder, FIXUtils.printFIXMessage(message));
				sampleResult.setDuration(neoLoadFIXHandler.sendOrder(sampleResult,message));
			} catch (InvalidMessage e) {
				NeoLoadUtils.throwNeoLoadError(neoLoadFIXHandler.getFixApplication().getLogger(),sampleResult,"FIX-SEND-MESSAGE","Couldn't send the message",e);
			}
		}

		sampleResult.setResponseContent(responseBuilder.toString());
		return sampleResult;
	}

	@Override
	public void stopExecute() {
		// TODO add code executed when the test have to stop.
	}

	private BufferedReader getMessageFile(Context context) throws FileNotFoundException {
		return new BufferedReader(new FileReader(context.getVariableManager().parseVariables(messagePath)));
	}

	private void parseParameters(List<ActionParameter> parameters) {

		for (ActionParameter temp : parameters) {
			switch (temp.getName().toLowerCase()) {
				case "sessionname":
					sessionName = temp.getValue();
					break;
				case "messagepath":
					messagePath = temp.getValue();
					break;
				case "message":
					message = temp.getValue();
					break;
				default:
					break;
			}
		}
	}

}
