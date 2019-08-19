package com.neotys.ps.fix.common;

import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;

public class NeoLoadUtils {
    public static void appendLineToStringBuilder(final StringBuilder sb, final String line){
        sb.append(line).append("\n");
    }

    public static SampleResult throwNeoLoadError(final Logger logger, final SampleResult result, String neoloadAction , final String errorMessage, final Exception exception) {
        result.setError(true);
        result.setStatusCode("NL-" + neoloadAction +"-ERROR");
        result.setResponseContent(errorMessage);
        if(exception != null){
            logger.error(errorMessage, exception);
        } else{
            logger.error(errorMessage);
        }
        return result;
    }
}
