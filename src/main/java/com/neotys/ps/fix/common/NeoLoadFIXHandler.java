package com.neotys.ps.fix.common;

import com.neotys.extensions.action.engine.SampleResult;
import org.quickfixj.QFJException;
import quickfix.*;
import quickfix.field.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeoLoadFIXHandler {
    private FIXApplication fixApplication;
    private final Connector connector;

    //FIX Logon Handler
    public NeoLoadFIXHandler(FIXLogger logger, SessionSettings sessionSettings, long logonTimeout, SampleResult result) throws ConfigError, InterruptedException {
        this.fixApplication = new FIXApplication(logger);
        MessageStoreFactory storeFactory = new FileStoreFactory(sessionSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        this.connector = new SocketInitiator(fixApplication,storeFactory,sessionSettings,messageFactory);
        this.connector.start();
        logon(this.connector);
        waitForSessionLogon(logonTimeout,result,"FIX-Logon");
    }

    public FIXApplication getFixApplication() {
        return fixApplication;
    }

    public Connector getConnector() {
        return connector;
    }

    public long logout(SampleResult result) throws InterruptedException {
        logout(connector);
        waitForSessionLogout(1000,result,"FIX-Logout");
        return result.getDuration();
    }

    public long sendOrder (SampleResult result, String message) throws InvalidMessage {
        //Get the session
        SessionID sessionID = connector.getSessions().get(0);

        //Get the begin string from the session
        String beginString = sessionID.getBeginString();
        //Message fixMessage = null;

        //Update the log line with the session settings
        message = changeMessageSenderAndTargetFromConnector(message);

        //Build the message
        Order order = new Order(message);

        //Send the message
        result.sampleStart();
        switch (beginString){
            case "FIX.4.0":
                fixApplication.send(fixApplication.buildOrder40(order),sessionID);
                break;
            case "FIX.4.1":
                fixApplication.send(fixApplication.buildOrder41(order),sessionID);
                break;
            case "FIX.4.2":
                fixApplication.send(fixApplication.buildOrder42(order),sessionID);
                break;
            case "FIX.4.3":
                fixApplication.send(fixApplication.buildOrder43(order),sessionID);
                break;
            case "FIX.4.4":
                fixApplication.send(fixApplication.buildOrder44(order),sessionID);
                break;
            case "FIX.5.0":
                fixApplication.send(fixApplication.buildOrder50(order),sessionID);
                break;
            default:
                NeoLoadUtils.throwNeoLoadError(fixApplication.getLogger(),result,"FIX-SEND-MESSAGE","Message not sent",new QFJException());
        }

        result.sampleEnd();

        return result.getDuration();
    }

    private SampleResult waitForSessionLogon(long logonTimeout, SampleResult result, String neoloadAction) throws InterruptedException {
        long logonStart = System.currentTimeMillis();
        long logonTime = 0;

        result.sampleStart();
        while (!connector.isLoggedOn() && logonTime < logonTimeout){
            Thread.sleep(500);
            logonTime = System.currentTimeMillis() - logonStart;
        }
        result.sampleEnd();

        if (connector.isLoggedOn()){
            fixApplication.getLogger().info("Logon in " + result.getDuration() + " ms");
            return result;
        } else {
            return NeoLoadUtils.throwNeoLoadError(fixApplication.getLogger(),result,neoloadAction,"Couldn't logon",new Exception());
        }
    }

    private SampleResult waitForSessionLogout(long logoutTimeout, SampleResult result, String neoloadAction) throws InterruptedException {
        long logoutStart = System.currentTimeMillis();
        long logoutTime = 0;

        result.sampleStart();
        while (connector.isLoggedOn() && logoutTime < logoutTimeout){
            Thread.sleep(logoutTimeout);
            logoutTime = System.currentTimeMillis() - logoutStart;
        }
        result.sampleEnd();

        if (!connector.isLoggedOn()){
            fixApplication.getLogger().info("Logout in " + result.getDuration() + " ms");
            return result;
        } else {
            return NeoLoadUtils.throwNeoLoadError(fixApplication.getLogger(),result,neoloadAction,"Couldn't logout",new Exception());
        }
    }

    private static synchronized void logon (Connector connector) {
        for (SessionID sessionId : connector.getSessions()) {
            Session.lookupSession(sessionId).logon();
        }
    }

    private static synchronized void logout(Connector connector) {
        for (SessionID sessionId : connector.getSessions()) {
            Session.lookupSession(sessionId).logout("user requested");
        }
    }

    private String changeMessageSenderAndTargetFromConnector(String message){
        String senderTag = "49" ;
        String targetTag = "56";
        String senderValue = "";
        String targetValue = "";

        if (message.contains(senderTag)) {
            String pattern = senderTag + "=(\\w+)\\x01";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(message);
            m.find();
            senderValue = m.group(1);
        }

        if (message.contains(targetTag)) {
            String pattern = targetTag + "=(\\w+)\\x01";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(message);
            m.find();
            targetValue = m.group(1);
        }

        if (!senderValue.isEmpty() && !targetValue.isEmpty()) {
            message = message.replace(senderValue, connector.getSessions().get(0).getSenderCompID());
            message = message.replace(targetValue, connector.getSessions().get(0).getTargetCompID());
        }

        return message;
    }

}
