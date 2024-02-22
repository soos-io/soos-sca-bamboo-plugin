package io.soos;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import io.soos.commons.PluginConstants;
import io.soos.commons.Utils;
import io.soos.integration.Configuration;
import io.soos.integration.Enums;
import io.soos.integration.SoosScaWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


public class SoosSCATask implements TaskType {

    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        try {
            Map<String, String> parameters = gatherParameters(taskContext);
            Configuration configuration = setupConfiguration(parameters);

            PrintStream printStream =  createLoggingPrintStream(buildLogger);
            SoosScaWrapper soosScaWrapper = new SoosScaWrapper(configuration,printStream);
            int exitCode = soosScaWrapper.runSca();

            if(exitCode != 0 ){
                if(configuration.getOnFailure().equalsIgnoreCase(Enums.OnFailure.FAIL_THE_BUILD.toString())){
                    return TaskResultBuilder.newBuilder(taskContext).failed().build();
                } else if(configuration.getOnFailure().equalsIgnoreCase(Enums.OnFailure.CONTINUE_ON_FAILURE.toString())){
                    return TaskResultBuilder.newBuilder(taskContext).success().build();
                }
            }

        } catch (Exception e) {
            buildLogger.addBuildLogEntry("SOOS SCA cannot be done, error: " + e);
        }

        return TaskResultBuilder.newBuilder(taskContext).success().build();
    }

    private Map<String, String> gatherParameters(TaskContext taskContext) throws Exception {
        Map<String, String> params = new HashMap<>(getTaskParameters(taskContext));
        params.putAll(getEnvironmentVariables(taskContext));
        setEnvProperties(params);
        return params;
    }

    private Configuration setupConfiguration(Map<String, String> parameters) {
        Configuration configuration = new Configuration();
        setConfigurationProperties(parameters, configuration);
        return configuration;
    }


    private PrintStream createLoggingPrintStream(final BuildLogger buildLogger) {
        return new PrintStream(new OutputStream() {
            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                buffer.write(b);
                if (b == '\n') {
                    buildLogger.addBuildLogEntry(buffer.toString("UTF-8"));
                    buffer.reset();
                }
            }
        });
    }

    private void setConfigurationProperties(Map<String, String> map, Configuration configuration) {
        Class<?> configClass = configuration.getClass();
        for (Field field : configClass.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String value = map.get(fieldName);
            if (map.containsKey(fieldName)) {
                try {
                    if (field.getType().equals(boolean.class)) {
                        field.setBoolean(configuration, Boolean.parseBoolean(value));
                    } else {
                        field.set(configuration, value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, String> getTaskParameters(TaskContext taskContext){
        Map<String, String> map = new HashMap<>(taskContext.getConfigurationMap());
        String workingDirectoryPath = taskContext.getWorkingDirectory().getPath();
        map.put("workingDirectory", workingDirectoryPath);
        map.put("sourceCodePath", workingDirectoryPath);
        map.put("integrationName", PluginConstants.INTEGRATION_NAME);

        if (map.get("projectName") == null || map.get("projectName").isEmpty()) {
            map.put("projectName", taskContext.getBuildContext().getPlanName());
        }

        return map;
    }

    private void setEnvProperties(Map<String, String> map){
        map.forEach(System::setProperty);
    }

    private Map<String, String> getEnvironmentVariables(TaskContext taskContext) throws Exception {
        Map<String, String> map = new HashMap<>();
        final VariableDefinitionContext clientId = Utils.getVariable(taskContext, PluginConstants.SOOS_CLIENT_ID);
        final VariableDefinitionContext apiKey = Utils.getVariable(taskContext, PluginConstants.SOOS_API_KEY);
        if(clientId == null || apiKey == null){
            throw new Exception("There was an issue retrieving your Client ID and API Key, make sure you have them set up on your global variables.");
        }
        map.put("clientId",clientId.getValue());
        map.put("apiKey",apiKey.getValue());

        return map;
    }

}

