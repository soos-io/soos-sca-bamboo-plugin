package io.soos.ui;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;
import java.util.logging.Logger;

public class ResultTabCondition implements Condition {

    private static final Logger log = Logger.getLogger(ResultTabCondition.class.getName());


    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return true;

    }
}
