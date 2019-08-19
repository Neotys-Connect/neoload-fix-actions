package com.neotys.ps.fix.common;

import com.neotys.extensions.action.engine.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FIXLogger implements Logger {
    private Logger logger;
    private String VUIdLogger;
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    public FIXLogger(Logger logger, String VUId) {
        this.logger = logger;
        this.VUIdLogger = "[" + VUId + "]: ";
    }

    @Override
    public void fatal(String message) {
        logger.fatal(VUIdLogger + message);
    }

    @Override
    public void error(String message) {
        logger.error(VUIdLogger + message);

    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(VUIdLogger  + message,t);
    }

    @Override
    public void warn(String message) {
        logger.warn(VUIdLogger + message);
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void debug(String message) {
        logger.debug(VUIdLogger + message);
    }

    @Override
    public void info(String message) {
        logger.info(VUIdLogger + message);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isFatalEnabled();
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
