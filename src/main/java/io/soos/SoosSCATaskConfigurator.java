package io.soos;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import io.soos.integration.commons.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class SoosSCATaskConfigurator extends AbstractTaskConfigurator {
    private final Validation validation = new Validation();
    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);

        config.put(Constants.MAP_PARAM_PROJECT_NAME_KEY, params.getString(Constants.MAP_PARAM_PROJECT_NAME_KEY));
        config.put(Constants.MAP_PARAM_MODE_KEY, params.getString(Constants.MAP_PARAM_MODE_KEY));
        config.put(Constants.MAP_PARAM_ON_FAILURE_KEY, params.getString(Constants.MAP_PARAM_ON_FAILURE_KEY));
        config.put(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY, params.getString(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY));
        config.put(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY, params.getString(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY));
        config.put(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, params.getString(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY));
        config.put(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, params.getString(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY));
        config.put(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY, params.getString(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY));
        config.put(Constants.MAP_PARAM_BRANCH_NAME_KEY, params.getString(Constants.MAP_PARAM_BRANCH_NAME_KEY));
        config.put(Constants.MAP_PARAM_BRANCH_URI_KEY, params.getString(Constants.MAP_PARAM_BRANCH_URI_KEY));
        config.put(Constants.MAP_PARAM_COMMIT_HASH_KEY, params.getString(Constants.MAP_PARAM_COMMIT_HASH_KEY));
        config.put(Constants.MAP_PARAM_BUILD_VERSION_KEY, params.getString(Constants.MAP_PARAM_BUILD_VERSION_KEY));
        config.put(Constants.MAP_PARAM_BUILD_URI_KEY, params.getString(Constants.MAP_PARAM_BUILD_URI_KEY));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        context.put("modes", getModes());
        context.put("onFailureOptions", getOnFailureOptions());
        context.put("operatingEnvironmentOptions",getOperatingEnvironmentOptions());
        context.put(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, Constants.MIN_RECOMMENDED_ANALYSIS_RESULT_MAX_WAIT);
        context.put(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, Constants.MIN_ANALYSIS_RESULT_POLLING_INTERVAL);

    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        context.put(Constants.MAP_PARAM_PROJECT_NAME_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_PROJECT_NAME_KEY));
        context.put(Constants.MAP_PARAM_MODE_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_MODE_KEY));
        context.put(Constants.MAP_PARAM_ON_FAILURE_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_ON_FAILURE_KEY));
        context.put(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY));
        context.put(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY));
        context.put(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY));
        context.put(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY));
        context.put(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY));
        context.put(Constants.MAP_PARAM_BRANCH_NAME_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_BRANCH_NAME_KEY));
        context.put(Constants.MAP_PARAM_BRANCH_URI_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_BRANCH_URI_KEY));
        context.put(Constants.MAP_PARAM_COMMIT_HASH_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_COMMIT_HASH_KEY));
        context.put(Constants.MAP_PARAM_BUILD_VERSION_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_BUILD_VERSION_KEY));
        context.put(Constants.MAP_PARAM_BUILD_URI_KEY, taskDefinition.getConfiguration().get(Constants.MAP_PARAM_BUILD_URI_KEY));
        context.put("modes", getModes());
        context.put("onFailureOptions", getOnFailureOptions());
        context.put("operatingEnvironmentOptions",getOperatingEnvironmentOptions());
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
}
