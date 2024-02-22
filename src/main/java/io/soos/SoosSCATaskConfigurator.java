package io.soos;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import io.soos.integration.Enums;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SoosSCATaskConfigurator extends AbstractTaskConfigurator {

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        Map<String, Object> map = getParams(params);
        map.forEach((k, v) -> config.put(k, (String) v));

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.putAll(populateComboList());

    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.putAll(getParams(taskDefinition));

        context.putAll(populateComboList());

    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
    }

    private Map<String, String> getOnFailureOptions() {
        final Map<String, String> map = new LinkedHashMap<>();
        for (Enums.OnFailure onFailure : Enums.OnFailure.values()) {
            map.put(onFailure.name(), onFailure.name());
        }
        return map;
    }

    private Map<String, String> getLogLevelOptions() {
        final Map<String, String> map = new LinkedHashMap<>();
        for (Enums.LogLevel logLevel : Enums.LogLevel.values()) {
            map.put(logLevel.name(), logLevel.name());
        }
        return map;
    }


    private Map<String, Object> populateComboList() {
        Map<String, Object> map = new HashMap<>();
        map.put("onFailureOptions", getOnFailureOptions());
        map.put("logLevelOptions", getLogLevelOptions());

        return map;
    }

    private <T> Map<String, Object> getParams(T object) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectName", getParamValue(object, "projectName"));
        map.put("dirsToExclude", getParamValue(object, "dirsToExclude"));
        map.put("filesToExclude", getParamValue(object, "filesToExclude"));
        map.put("packageManagers", getParamValue(object, "packageManagers"));
        map.put("onFailure", getParamValue(object, "onFailure"));
        map.put("apiURL", getParamValue(object, "apiURL"));
        map.put("logLevel", getParamValue(object, "logLevel"));
        map.put("verbose", getParamValue(object, "verbose"));
        map.put("outputFormat", getParamValue(object, "outputFormat"));
        map.put("nodePath", getParamValue(object, "nodePath"));

        return map;
    }

    private <T> String getParamValue(T object, String param) {
        String value;
        if (object instanceof TaskDefinition) {
            TaskDefinition taskDefinition = (TaskDefinition) object;
            Map<String, String> objectMap = taskDefinition.getConfiguration();
            value = objectMap.get(param);
        } else {
            ActionParametersMap actionParametersMap = (ActionParametersMap) object;
            value = actionParametersMap.getString(param);
        }

        return value;
    }

}
