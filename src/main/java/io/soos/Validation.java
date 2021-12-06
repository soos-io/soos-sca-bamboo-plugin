package io.soos;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import io.soos.commons.ErrorMessage;
import io.soos.commons.PluginConstants;
import io.soos.domain.Mode;
import io.soos.domain.OnFailure;
import io.soos.domain.OperatingEnvironment;
import io.soos.integration.commons.Constants;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class Validation {

    private String projectName;
    private String analysisResultMaxWait;
    private String analysisResultPollingInterval;

    public Validation(){};

    public void validateParams(ActionParametersMap params, ErrorCollection errorCollection){
        this.projectName = params.getString(Constants.MAP_PARAM_PROJECT_NAME_KEY);
        this.analysisResultMaxWait = params.getString(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY);
        this.analysisResultPollingInterval = params.getString(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY);

        if( StringUtils.isEmpty(projectName) ) {
            errorCollection.addError(Constants.MAP_PARAM_PROJECT_NAME_KEY, ErrorMessage.SHOULD_NOT_BE_NULL);
        }
        if( !StringUtils.isEmpty(projectName) && projectName.length() < PluginConstants.MIN_NUMBER_OF_CHARACTERS) {
            errorCollection.addError(Constants.MAP_PARAM_PROJECT_NAME_KEY, ErrorMessage.shouldBeMoreThanXCharacters(PluginConstants.MIN_NUMBER_OF_CHARACTERS));
        }
        if( validateIsNotEmptyAndIsNumeric(analysisResultMaxWait) ) {
            errorCollection.addError(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, ErrorMessage.SHOULD_BE_A_NUMBER);
        }
        if( validateIsNotEmptyAndIsNumeric(analysisResultPollingInterval) ) {
            errorCollection.addError(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, ErrorMessage.SHOULD_BE_A_NUMBER);
        }
    }

    private Boolean validateIsNotEmptyAndIsNumeric( String value ) {
        return !ObjectUtils.isEmpty(value) && !StringUtils.isNumeric(value);
    }
}
