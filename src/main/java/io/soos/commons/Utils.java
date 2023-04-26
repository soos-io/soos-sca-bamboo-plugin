package io.soos.commons;

import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;

public class Utils {

    public static VariableDefinitionContext getVariable(TaskContext taskContext, String variableName) {
        return taskContext.getBuildContext().getVariableContext().getEffectiveVariables().get(variableName);
    }

    public static String getOperatingSystem() {
        return System.getProperty(PluginConstants.OS_NAME).toLowerCase();
    }

}
