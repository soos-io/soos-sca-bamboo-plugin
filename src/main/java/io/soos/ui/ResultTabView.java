package io.soos.ui;

import com.atlassian.bamboo.build.PlanResultsAction;
import com.atlassian.bamboo.chains.ChainResultsSummaryImpl;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultTabView extends PlanResultsAction {
    private static final Logger log = Logger.getLogger(ResultTabView.class.getName());
    private Map<String, String> reportData = new HashMap<>();




    public String execute() throws Exception {
        String result = super.execute();

        ResultsSummary summary = this.getResultsSummary();
        if(summary instanceof ChainResultsSummaryImpl){
            ChainResultsSummaryImpl chainResults = (ChainResultsSummaryImpl) summary;
            log.log(Level.FINE,"Try to get report link for ChainResultsSummaryImpl");
            List<ChainStageResult> resultList = chainResults.getStageResults();
            for (ChainStageResult chainResult : resultList) {
                Set<BuildResultsSummary> resultSet = chainResult.getBuildResults();
                for (BuildResultsSummary sum : resultSet) {
                    Map<String, String> customBuildData = sum.getCustomBuildData();
                    for (String key : customBuildData.keySet()) {
                        if (key.startsWith("reportUrl")) {
                            log.log(Level.FINE,"Found report link for master =" + key);
                            reportData.put("reportUrl", customBuildData.get(key));
                        }
                        if (key.startsWith("violationsCount")) {
                            log.log(Level.FINE,"Found violations count");
                            reportData.put("violationsCount", customBuildData.get(key));
                        }
                        if (key.startsWith("vulnerabilitiesCount")) {
                            log.log(Level.FINE,"Found vulnerabilities count");
                            reportData.put("vulnerabilitiesCount", customBuildData.get(key));
                        }
                    }
                }
            }
        }
        return result;
    }

    public Map<String, String> getReportData() {
        return reportData;
    }

    public void setReportData(Map<String, String> reportData) {
        this.reportData = reportData;
    }
}
