package io.soos.commons;

import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import io.soos.integration.validators.OSValidator;
import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class Utils {

    public static VariableDefinitionContext getVariable(TaskContext taskContext, String variableName) {
        return taskContext.getBuildContext().getVariableContext().getEffectiveVariables().get(variableName);
    }

    public static String getOperatingSystem() {
        return System.getProperty(PluginConstants.OS_NAME).toLowerCase();
    }

    public static String getBuildPath(TaskContext taskContext, Integer previousBuild){
        final String BAMBOO_ROOT_DIRECTORY = taskContext.getRootDirectory().getAbsolutePath();
        String pathSeparator = PluginConstants.SLASH;
        if ( OSValidator.isWindows() ){
            pathSeparator = PluginConstants.BACK_SLASH;
        }
        StringBuilder buildPath = new StringBuilder();
        buildPath.append(BAMBOO_ROOT_DIRECTORY);
        buildPath.append(pathSeparator);
        buildPath.append(PluginConstants.RESULT_FILE);

        return buildPath.toString();
    }

    public static void saveReportStatusUrl(String reportStatusUrl, TaskContext taskContext) {
        String resultFilePath = getBuildPath(taskContext, null);
        File file = new File(resultFilePath);
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(reportStatusUrl);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getReportStatusUrl(TaskContext taskContext, Integer previousBuild){
        String resultFilePath = getBuildPath(taskContext, previousBuild);
        File file = new File(resultFilePath);
        String resultStatusUrl = "";
        try {
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                resultStatusUrl = reader.nextLine();
            }
            reader.close();
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultStatusUrl;
    }
}
