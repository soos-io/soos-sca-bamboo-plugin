package io.soos;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import io.soos.commons.PluginConstants;
import io.soos.domain.Mode;
import io.soos.domain.OnFailure;
import io.soos.domain.OperatingEnvironment;
import io.soos.integration.commons.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SoosSCATaskConfigurator extends AbstractTaskConfigurator {

    private final Validation validation = new Validation();

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        Map<String, Object> map = getParams(params);
        map.forEach( (k, v) -> config.put(k, (String) v) );

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        context.putAll(populateComboList());
        context.put(Constants.MAP_PARAM_API_BASE_URI_KEY, Constants.SOOS_DEFAULT_API_URL);
        context.put(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, Constants.MIN_RECOMMENDED_ANALYSIS_RESULT_MAX_WAIT);
        context.put(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, Constants.MIN_ANALYSIS_RESULT_POLLING_INTERVAL);

    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.putAll(getParams(taskDefinition));

        context.putAll(populateComboList());

        if(StringUtils.isEmpty(taskDefinition.getConfiguration().get(Constants.MAP_PARAM_API_BASE_URI_KEY))){
            context.put(Constants.MAP_PARAM_API_BASE_URI_KEY, Constants.SOOS_DEFAULT_API_URL);
        }
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection){
        super.validate(params, errorCollection);
        this.validation.validateParams(params, errorCollection);
    }

    private Map<String, String> getModes(){
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(Mode.RUN_AND_WAIT.getMode(), Mode.RUN_AND_WAIT.getName());
        map.put(Mode.ASYNC_INIT.getMode(), Mode.ASYNC_INIT.getName());
        map.put(Mode.ASYNC_RESULT.getMode(), Mode.ASYNC_RESULT.getName());
        return map;
    }
    private Map<String, String> getOnFailureOptions(){
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(OnFailure.FAIL_THE_BUILD.getValue(), OnFailure.FAIL_THE_BUILD.getName());
        map.put(OnFailure.CONTINUE_ON_FAILURE.getValue(), OnFailure.CONTINUE_ON_FAILURE.getName());
        return map;
    }
    private Map<String, String> getOperatingEnvironmentOptions(){
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(OperatingEnvironment.LINUX.getValue(), OperatingEnvironment.LINUX.getName());
        map.put(OperatingEnvironment.MAC.getValue(), OperatingEnvironment.MAC.getName());
        map.put(OperatingEnvironment.WINDOWS.getValue(), OperatingEnvironment.WINDOWS.getName());
        return map;
    }
    private Map<String, Object> populateComboList(){
        Map<String, Object> map = new HashMap<>();
        map.put(PluginConstants.MODES, getModes());
        map.put(PluginConstants.ON_FAILURE_OPTIONS, getOnFailureOptions());
        map.put(PluginConstants.OPERATING_SYSTEM_OPTIONS, getOperatingEnvironmentOptions());

        return map;
    }

    private <T> Map<String, Object> getParams(T object){
        Map<String, Object> map = new HashMap<>();

        map.put(Constants.MAP_PARAM_PROJECT_NAME_KEY, getParamValue(object, Constants.MAP_PARAM_PROJECT_NAME_KEY));
        map.put(Constants.MAP_PARAM_MODE_KEY, getParamValue(object, Constants.MAP_PARAM_MODE_KEY));
        map.put(Constants.MAP_PARAM_ON_FAILURE_KEY, getParamValue(object, Constants.MAP_PARAM_ON_FAILURE_KEY));
        map.put(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY, getParamValue(object, Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY));
        map.put(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY, getParamValue(object, Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY));
        map.put(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, getParamValue(object, Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY));
        map.put(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, getParamValue(object, Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY));
        map.put(Constants.MAP_PARAM_API_BASE_URI_KEY, getParamValue(object, Constants.MAP_PARAM_API_BASE_URI_KEY));
        map.put(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY, getParamValue(object, Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY));
        map.put(Constants.MAP_PARAM_BRANCH_NAME_KEY, getParamValue(object, Constants.MAP_PARAM_BRANCH_NAME_KEY));
        map.put(Constants.MAP_PARAM_BRANCH_URI_KEY, getParamValue(object, Constants.MAP_PARAM_BRANCH_URI_KEY));
        map.put(Constants.MAP_PARAM_COMMIT_HASH_KEY, getParamValue(object, Constants.MAP_PARAM_COMMIT_HASH_KEY));
        map.put(Constants.MAP_PARAM_BUILD_VERSION_KEY, getParamValue(object, Constants.MAP_PARAM_BUILD_VERSION_KEY));
        map.put(Constants.MAP_PARAM_BUILD_URI_KEY, getParamValue(object, Constants.MAP_PARAM_BUILD_URI_KEY));


        return map;
    }

    private <T> String getParamValue(T object, String param){
        String value;
        if(object instanceof TaskDefinition){
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
