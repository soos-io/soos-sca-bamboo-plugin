package io.soos;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import io.soos.commons.PluginConstants;
import io.soos.commons.Utils;
import io.soos.integration.commons.Constants;
import io.soos.integration.domain.SOOS;
import io.soos.integration.domain.structure.StructureResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SoosSCATask implements TaskType {
    private final Logger LOG = LoggerFactory.getLogger(SoosSCATask.class);

    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        Map<String, String> map = getTaskParameters(taskContext);
        map.putAll(getEnvironmentVariable(taskContext));

        setEnvProperties(map, buildLogger);

        String onFailure = taskContext.getConfigurationMap().get(Constants.MAP_PARAM_ON_FAILURE_KEY);

        try {
            SOOS soos = new SOOS();
            StructureResponse structure = soos.getStructure();
            LOG.info(structure.toString());
            long filesProcessed = soos.sendManifestFiles(structure.getProjectId(), structure.getAnalysisId());
            StringBuilder fileProcessed = new StringBuilder("File processed: ").append(String.valueOf(filesProcessed));
            buildLogger.addBuildLogEntry(fileProcessed.toString());
            if (filesProcessed > 0L) {

                String reportUrl = soos.getStructure().getReportURL();
                StringBuilder reportMsg = new StringBuilder();
                reportMsg.append("Open the following url to see the report: ").append(reportUrl);

                switch(soos.getMode()) {
                    case RUN_AND_WAIT:
                        buildLogger.addBuildLogEntry(PluginConstants.RUN_AND_WAIT_MODE_SELECTED);
                        startAnalysis(soos);
                        getResult(soos);
                        buildLogger.addBuildLogEntry(reportMsg.toString());
                        break;
                    case ASYNC_INIT:
                        buildLogger.addBuildLogEntry(PluginConstants.ASYNC_INIT_MODE_SELECTED);
                        startAnalysis(soos);
                        break;
                    case ASYNC_RESULT:
                        buildLogger.addBuildLogEntry(PluginConstants.ASYNC_RESULT_MODE_SELECTED);
                        getResult(soos);
                        buildLogger.addBuildLogEntry(reportMsg.toString());
                        break;
                }
            }

        } catch (Exception e) {
            if(onFailure.equals(PluginConstants.FAIL_THE_BUILD)){
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("SOOS SCA cannot be done, error: ").append(e).append("- the build has failed!");
                buildLogger.addErrorLogEntry(errorMsg.toString());
                return TaskResultBuilder.newBuilder(taskContext).failed().build();
            }
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("SOOS SCA cannot be done, error: ").append(e).append("- Continuing the build... ");
            buildLogger.addBuildLogEntry(errorMsg.toString());
        }

        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }

    private void startAnalysis(SOOS soos) throws Exception {
        StructureResponse structure = soos.getStructure();
        soos.startAnalysis(structure.getProjectId(), structure.getAnalysisId());
    }

    private void getResult(SOOS soos) throws Exception {
        StructureResponse structure = soos.getStructure();
        soos.getResults(structure.getReportStatusUrl());
    }

    private Map<String, String> getTaskParameters(TaskContext taskContext){
        Map<String, String> map = new HashMap<>();

        Map<String, String> params = taskContext.getConfigurationMap();
        String workingDirectoryPath = taskContext.getWorkingDirectory().getPath();

        String dirsToExclude = addSoosDirToExclusion(taskContext.getConfigurationMap().get(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY));

        map.put(Constants.PARAM_PROJECT_NAME_KEY, params.get(Constants.MAP_PARAM_PROJECT_NAME_KEY));
        map.put(Constants.PARAM_MODE_KEY, params.get(Constants.MAP_PARAM_MODE_KEY));
        map.put(Constants.PARAM_ON_FAILURE_KEY, params.get(Constants.MAP_PARAM_ON_FAILURE_KEY));
        map.put(Constants.PARAM_DIRS_TO_EXCLUDE_KEY, dirsToExclude);
        map.put(Constants.PARAM_FILES_TO_EXCLUDE_KEY, params.get(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY));
        map.put(Constants.PARAM_WORKSPACE_DIR_KEY, workingDirectoryPath);
        map.put(Constants.PARAM_CHECKOUT_DIR_KEY, workingDirectoryPath);
        map.put(Constants.PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, params.get(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY));
        map.put(Constants.PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, params.get(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY));
        map.put(Constants.PARAM_API_BASE_URI_KEY, params.get(Constants.MAP_PARAM_API_BASE_URI_KEY));
        map.put(Constants.PARAM_OPERATING_ENVIRONMENT_KEY, params.get(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY));
        map.put(Constants.PARAM_BRANCH_NAME_KEY, params.get(Constants.MAP_PARAM_BRANCH_NAME_KEY));
        map.put(Constants.PARAM_BRANCH_URI_KEY, params.get(Constants.MAP_PARAM_BRANCH_URI_KEY));
        map.put(Constants.PARAM_COMMIT_HASH_KEY, params.get(Constants.MAP_PARAM_COMMIT_HASH_KEY));
        map.put(Constants.PARAM_BUILD_VERSION_KEY, params.get(Constants.MAP_PARAM_BUILD_VERSION_KEY));
        map.put(Constants.PARAM_BUILD_URI_KEY, params.get(Constants.MAP_PARAM_BUILD_URI_KEY));
        map.put(Constants.PARAM_INTEGRATION_NAME_KEY, PluginConstants.INTEGRATION_NAME);

        if(StringUtils.isBlank(params.get(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY))) {
            map.put(Constants.PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, String.valueOf(Constants.MIN_RECOMMENDED_ANALYSIS_RESULT_MAX_WAIT));
        }
        if(StringUtils.isBlank(params.get(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY))) {
            map.put(Constants.PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, String.valueOf(Constants.MIN_ANALYSIS_RESULT_POLLING_INTERVAL));
        }
        if(StringUtils.isBlank(params.get(Constants.MAP_PARAM_API_BASE_URI_KEY))){
            map.put(Constants.PARAM_API_BASE_URI_KEY, Constants.SOOS_DEFAULT_API_URL);
        }
        return map;
    }

    private void setEnvProperties(Map<String, String> map, BuildLogger buildLogger){

        map.forEach((key, value) -> {
            if(StringUtils.isNotBlank(value)) {
                System.setProperty(key, value);
            }
        });
    }

    private String addSoosDirToExclusion(String dirs){
        if(StringUtils.isNotBlank(dirs)){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(dirs).append(",").append(PluginConstants.SOOS_DIR_NAME);

            return stringBuilder.toString();
        }

        return PluginConstants.SOOS_DIR_NAME;
    }

    private Map<String, String> getEnvironmentVariable(TaskContext taskContext){
        Map<String, String> map = new HashMap<>();

        final VariableDefinitionContext clientId = Utils.getVariable(taskContext, PluginConstants.SOOS_CLIENT_ID);
        final VariableDefinitionContext apiKey = Utils.getVariable(taskContext, PluginConstants.SOOS_API_KEY);

        map.put(PluginConstants.SOOS_CLIENT_ID,clientId.getValue());
        map.put(PluginConstants.SOOS_API_KEY,apiKey.getValue());

        return map;
    }

}
