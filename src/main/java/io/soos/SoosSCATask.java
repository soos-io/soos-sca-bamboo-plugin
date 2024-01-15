package io.soos;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.PlanExecutionManager;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.bamboo.v2.build.trigger.TriggerReason;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bamboo.variable.VariableDefinition;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.atlassian.bamboo.variable.VariableSubstitutionContext;
import com.atlassian.crowd.model.authentication.Session;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.user.impl.hibernate3.configuration.HibernateAccessor;
import io.soos.commons.PluginConstants;
import io.soos.commons.Utils;
import io.soos.integration.commons.Constants;
import io.soos.integration.domain.SOOS;
import io.soos.integration.domain.analysis.AnalysisResultResponse;
import io.soos.integration.domain.scan.ScanResponse;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.jetbrains.annotations.NotNull;
import org.springframework.orm.hibernate5.HibernateCallback;

import java.io.IOException;
import java.util.*;


public class SoosSCATask implements TaskType {

    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        Map<String, String> map = getTaskParameters(taskContext);
        String onFailure = taskContext.getConfigurationMap().get(Constants.MAP_PARAM_ON_FAILURE_KEY);
        try {
            map.putAll(getEnvironmentVariables(taskContext));
            setEnvProperties(map);
            SOOS soos = new SOOS();
            soos.getContext().setScriptVersion(getVersionFromProperties(buildLogger));
            ScanResponse scan;
            Map<String, String> customData = taskContext.getBuildContext().getBuildResult().getCustomBuildData();

            buildLogger.addBuildLogEntry("---------- Custom Build Data ----------");

            for (Map.Entry<String, String> entry : customData.entrySet()) {
                buildLogger.addBuildLogEntry("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            VariableContext variableContext = taskContext.getBuildContext().getVariableContext();
            VariableDefinitionContext manualTriggerReason = variableContext.getEffectiveVariables().get("bamboo_ManualBuildTriggerReason_userName");
            BuildContext buildContext = taskContext.getBuildContext();
            PlanManager planManager = ComponentLocator.getComponent(PlanManager.class);
            PlanResultKey planResultKey = taskContext.getBuildContext().getPlanResultKey();
            Plan plan = planManager.getPlanByKey(planResultKey.getPlanKey());

            ResultsSummaryManager resultsSummaryManager = ComponentLocator.getComponent(ResultsSummaryManager.class);
            ImmutablePlan immutablePlan = planManager.getPlanByKey(planResultKey.getPlanKey());

            List<BuildResultsSummary> summaries = resultsSummaryManager.getResultSummariesForPlan(immutablePlan, 0, 100);
            try {
                if (summaries != null && !summaries.isEmpty()) {
                    // Sorting to get the latest build summary.
                    BuildResultsSummary latestSummary = Collections.max(summaries, Comparator.comparing(BuildResultsSummary::getBuildDate));

                    if (latestSummary != null) {
                        var test = latestSummary.getShortReasonSummary();
                        var test2 = latestSummary.getChangesListSummary();
                        var test3 = latestSummary.getReasonSummary();
                        TriggerReason triggerReason = latestSummary.getTriggerReason();
                        if (triggerReason != null) {
                            // This will give you a string description of the reason.
                            String reasonDescription = triggerReason.getName();

                            // If it's a manual trigger, you can get more details.
                            if (triggerReason instanceof ManualBuildTriggerReason) {
                                String username = ((ManualBuildTriggerReason) triggerReason).getUserName();
                                taskContext.getBuildLogger().addBuildLogEntry("username" +
                                        username);
                            }
                        }
                    }
                }
            }catch (Exception ex){
                buildLogger.addBuildLogEntry("error:" + ex.getMessage());
            }

            List<VariableDefinition> variablesPlan = plan.getVariables();
            for (var entry : variablesPlan) {
                taskContext.getBuildLogger().addBuildLogEntry("variablesPlan Name Result: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            Map<String, VariableDefinitionContext> variables = variableContext.getResultVariables();
            for (Map.Entry<String, VariableDefinitionContext> entry : variables.entrySet()) {
                String variableName = entry.getKey();
                String variableValue = entry.getValue().getValue(); // Extract the actual value from the VariableDefinitionContext
                taskContext.getBuildLogger().addBuildLogEntry("Variable Name Result: " + variableName + ", Value: " + variableValue);
            }

            Map<String, VariableDefinitionContext> variables2 = variableContext.getEffectiveVariables();
            for (Map.Entry<String, VariableDefinitionContext> entry : variables2.entrySet()) {
                String variableName = entry.getKey();
                String variableValue = entry.getValue().getValue(); // Extract the actual value from the VariableDefinitionContext
                taskContext.getBuildLogger().addBuildLogEntry("Variable Name Effective: " + variableName + ", Value: " + variableValue);
            }

            Map<String, VariableDefinitionContext> variables3 = variableContext.getOriginalVariables();
            for (Map.Entry<String, VariableDefinitionContext> entry : variables3.entrySet()) {
                String variableName = entry.getKey();
                String variableValue = entry.getValue().getValue(); // Extract the actual value from the VariableDefinitionContext
                taskContext.getBuildLogger().addBuildLogEntry("Variable Name Original: " + variableName + ", Value: " + variableValue);
            }

            Map<String, VariableSubstitutionContext> variables4 = variableContext.getSubstitutions();
            for (Map.Entry<String, VariableSubstitutionContext> entry : variables4.entrySet()) {
                String variableName = entry.getKey();
                String variableValue = entry.getValue().getValue(); // Extract the actual value from the VariableDefinitionContext
                taskContext.getBuildLogger().addBuildLogEntry("Variable Name Substitution: " + variableName + ", Value: " + variableValue);
            }

            if (manualTriggerReason != null) {
                String userName = manualTriggerReason.getValue();
                buildLogger.addBuildLogEntry("Username of who triggered pipeline" + userName);
            }


            buildLogger.addBuildLogEntry("---------------------------------------");

            buildLogger.addBuildLogEntry("--------------------------------------------");
            AnalysisResultResponse result = null;
            buildLogger.addBuildLogEntry("--------------------------------------------");
            buildLogger.addBuildLogEntry("Run SOOS SCA Scan");
            buildLogger.addBuildLogEntry("--------------------------------------------");
            scan = soos.startAnalysis();
            buildLogger.addBuildLogEntry("Analysis request is running");
            result = soos.getResults(scan.getScanStatusUrl());
            buildLogger.addBuildLogEntry("Scan analysis finished successfully. To see the results go to: " + result.getScanUrl());
            buildLogger.addBuildLogEntry("Vulnerabilities found: " + result.getVulnerabilities() + ", Violations found: " + result.getViolations());
            taskContext.getBuildContext().getBuildResult().getCustomBuildData().put("isSOOSSCAScanTask", "true");
            taskContext.getBuildContext().getBuildResult().getCustomBuildData().put("reportUrl", result.getScanUrl());
            taskContext.getBuildContext().getBuildResult().getCustomBuildData().put("violationsCount", String.valueOf(result.getViolations()));
            taskContext.getBuildContext().getBuildResult().getCustomBuildData().put("vulnerabilitiesCount", String.valueOf(result.getVulnerabilities()));
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

    private Map<String, String> getTaskParameters(TaskContext taskContext){
        Map<String, String> map = new HashMap<>();

        Map<String, String> params = taskContext.getConfigurationMap();
        String workingDirectoryPath = taskContext.getWorkingDirectory().getPath();

        String dirsToExclude = addSoosDirToExclusion(params.get(Constants.MAP_PARAM_DIRS_TO_EXCLUDE_KEY));
        map.put(Constants.PARAM_PROJECT_NAME_KEY, params.get(Constants.MAP_PARAM_PROJECT_NAME_KEY));
        map.put(Constants.PARAM_ON_FAILURE_KEY, params.get(Constants.MAP_PARAM_ON_FAILURE_KEY));
        map.put(Constants.PARAM_DIRS_TO_EXCLUDE_KEY, dirsToExclude);
        map.put(Constants.PARAM_FILES_TO_EXCLUDE_KEY, params.get(Constants.MAP_PARAM_FILES_TO_EXCLUDE_KEY));
        map.put(Constants.PARAM_PACKAGE_MANAGERS_KEY, params.getOrDefault(Constants.MAP_PARAM_PACKAGE_MANAGERS_KEY, ""));
        map.put(Constants.PARAM_WORKSPACE_DIR_KEY, workingDirectoryPath);
        map.put(Constants.PARAM_CHECKOUT_DIR_KEY, workingDirectoryPath);
        map.put(Constants.PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, params.get(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY));
        map.put(Constants.PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, params.get(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY));
        map.put(Constants.PARAM_API_BASE_URI_KEY, params.get(Constants.MAP_PARAM_API_BASE_URI_KEY));
        map.put(Constants.PARAM_OPERATING_ENVIRONMENT_KEY, Utils.getOperatingSystem());
        map.put(Constants.PARAM_BRANCH_NAME_KEY, params.get(Constants.MAP_PARAM_BRANCH_NAME_KEY));
        map.put(Constants.PARAM_BRANCH_URI_KEY, params.get(Constants.MAP_PARAM_BRANCH_URI_KEY));
        map.put(Constants.PARAM_COMMIT_HASH_KEY, params.get(Constants.MAP_PARAM_COMMIT_HASH_KEY));
        map.put(Constants.PARAM_BUILD_VERSION_KEY, String.valueOf(taskContext.getBuildContext().getBuildNumber()));
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

    private void setEnvProperties(Map<String, String> map){
        map.forEach((key, value) -> {
                System.setProperty(key, value);
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

    private Map<String, String> getEnvironmentVariables(TaskContext taskContext) throws Exception {
        Map<String, String> map = new HashMap<>();

        final VariableDefinitionContext clientId = Utils.getVariable(taskContext, PluginConstants.SOOS_CLIENT_ID);
        final VariableDefinitionContext apiKey = Utils.getVariable(taskContext, PluginConstants.SOOS_API_KEY);
        if(clientId == null || apiKey == null){
            throw new Exception("There was an issue retrieving your Client ID and API Key, make sure you have them set up on your global variables.");
        }
        map.put(PluginConstants.SOOS_CLIENT_ID,clientId.getValue());
        map.put(PluginConstants.SOOS_API_KEY,apiKey.getValue());

        return map;
    }

    private String getVersionFromProperties(BuildLogger buildLogger){
        Properties prop = new Properties();
        try {
            prop.load(this.getClass().getResourceAsStream(PluginConstants.PROPERTIES_FILE));
            return prop.getProperty(PluginConstants.VERSION);
        } catch (IOException e) {
            String error = "Cannot read file '" + PluginConstants.PROPERTIES_FILE + "'";
            buildLogger.addErrorLogEntry(error);
        }
        return null;
    }


}

