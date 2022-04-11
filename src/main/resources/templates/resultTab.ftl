<html>
<head>
    <meta name="tab" content="View SOOS SCA Results summary"/>
    <meta name="decorator" content="result"/>
</head>

<body>

    [#if reportData?? && (reportData.size()>0)]
    [@ui.header page='SOOS SCA Results'/]
    [#if reportData.get("vulnerabilitiesCount") != "0" && reportData.get("violationsCount") != "0"]
    <HR>
            <div class="aui-message success">
            We found ${reportData.get("vulnerabilitiesCount")} vulnerabilities and ${reportData.get("violationsCount")} violations, to  see the report click on the button below
            </br>
                <a href="${reportData.get("reportUrl")}">
                    <button class="aui-button aui-button-primary">SOOS SCA Report</button>
                </a>
            </div>
    [#else]
        <div class="aui-message warning">
             We found ${reportData.get("vulnerabilitiesCount")} vulnerabilities and ${reportData.get("violationsCount")} violations, to  see the report click on the button below
             </br>
             <a href="${reportData.get("reportUrl")}">
                <button class="aui-button aui-button-primary">SOOS SCA Report</button>
             </a>
        </div>
    [/#if]
    [#else]
        [@ui.header page='SOOS SCA Scan is still running'/]
    [/#if]

</body>
</html>