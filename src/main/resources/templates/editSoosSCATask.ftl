[@ww.textfield label="Project Name" name="projectName" value=projectName required='true'/]

[@ww.textfield label="Directories To Exclude" name="directoriesToExclude" value=directoriesToExclude /]
<label style="color: darkgray;font-size: 12px">Separate the directory names with a comma</label>

[@ww.textfield label="Files To Exclude" name="filesToExclude" value=filesToExclude /]
<label style="color: darkgray;font-size: 12px">Separate the file names with a comma</label>

[@ww.textfield label="Package Managers to look for" name="packageManagers" value=packageManagers /]
<label style="color: darkgray;font-size: 12px">Separate Package Manager names with a comma</label>

[@ww.select label="On Failure" listKey="key" listValue="value" list=onFailureOptions name="onFailure" value=onFailure toggle=true /]

[@ww.textfield label="API Base URL" name="apiURL" default=apiURL/]

[@ww.select label="Log Level" listKey="key" listValue="value" list=logLevelOptions name="logLevel" value=logLevel /]

[@ww.checkbox label="Verbose Logging" name="verbose" value=verbose /]

[@ww.textfield label="Output Format" name="outputFormat" value=outputFormat /]

[@ww.textfield label="Node Path" name="nodePath" value=nodePath /]


