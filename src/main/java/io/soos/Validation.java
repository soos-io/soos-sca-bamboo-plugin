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
    private String mode;
    private String onFailure;
    private String operatingEnvironment;
    private String analysisResultMaxWait;
    private String analysisResultPollingInterval;

    public Validation(){};

    public void validateParams(ActionParametersMap params, ErrorCollection errorCollection){
        this.projectName = params.getString(Constants.MAP_PARAM_PROJECT_NAME_KEY);
        this.mode = params.getString(Constants.MAP_PARAM_MODE_KEY);
        this.onFailure = params.getString(Constants.MAP_PARAM_ON_FAILURE_KEY);
        this.operatingEnvironment = params.getString(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY);
        this.analysisResultMaxWait = params.getString(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY);
        this.analysisResultPollingInterval = params.getString(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY);

        if( StringUtils.isEmpty(projectName) ) {
            errorCollection.addError(Constants.MAP_PARAM_PROJECT_NAME_KEY, ErrorMessage.SHOULD_NOT_BE_NULL);
        }
        if( !StringUtils.isEmpty(projectName) && projectName.length() < PluginConstants.MIN_NUMBER_OF_CHARACTERS) {
            errorCollection.addError(Constants.MAP_PARAM_PROJECT_NAME_KEY, ErrorMessage.shouldBeMoreThanXCharacters(PluginConstants.MIN_NUMBER_OF_CHARACTERS));
        }
        if( !validateMode() ){
            errorCollection.addError(Constants.MAP_PARAM_MODE_KEY, ErrorMessage.SHOULD_BE_ONE_PERMITTED_OPTION);
        }
        if( !validateOnFailure() ){
            errorCollection.addError(Constants.MAP_PARAM_ON_FAILURE_KEY, ErrorMessage.SHOULD_BE_ONE_PERMITTED_OPTION);
        }
        if( !validateOE() ){
            errorCollection.addError(Constants.MAP_PARAM_OPERATING_ENVIRONMENT_KEY, ErrorMessage.SHOULD_BE_ONE_PERMITTED_OPTION);
        }
        if( !ObjectUtils.isEmpty(analysisResultMaxWait) && !StringUtils.isNumeric(analysisResultMaxWait) ) {
            errorCollection.addError(Constants.MAP_PARAM_ANALYSIS_RESULT_MAX_WAIT_KEY, ErrorMessage.SHOULD_BE_A_NUMBER);
        }
        if( !ObjectUtils.isEmpty(analysisResultPollingInterval) && !StringUtils.isNumeric(analysisResultPollingInterval) ) {
            errorCollection.addError(Constants.MAP_PARAM_ANALYSIS_RESULT_POLLING_INTERVAL_KEY, ErrorMessage.SHOULD_BE_A_NUMBER);
        }
    }

    private Boolean validateMode(){
        return !StringUtils.isEmpty(this.mode) && Arrays.stream(Mode.values()).anyMatch(mode -> mode.getMode().equals(this.mode));
    }

    private Boolean validateOnFailure(){
        return !StringUtils.isEmpty(this.onFailure) && Arrays.stream(OnFailure.values()).anyMatch(onFailure -> onFailure.getValue().equals(this.onFailure));
    }

    private Boolean validateOE() {
        if (!StringUtils.isEmpty(this.operatingEnvironment)) {
            return Arrays.stream(OperatingEnvironment.values()).anyMatch(opEn -> opEn.getValue().equals(this.operatingEnvironment));
        }
        return true;
    }

}
