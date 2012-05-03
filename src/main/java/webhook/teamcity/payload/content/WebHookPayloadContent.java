package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;

public class WebHookPayloadContent {
		String buildStatus, buildStatusPrevious,
		buildResult, buildResultPrevious, buildResultDelta,
		notifyType,
		buildFullName,
		buildName,
		buildId,
		buildTypeId,
		buildStatusUrl,
		projectName,
		projectId,
		buildNumber,
		agentName,
		agentOs,
		agentHostname,
		triggeredBy,
		comment,
		message,
		text;
		
		List<String> buildRunners;
		ExtraParametersMap extraParameters;
		
		
		public WebHookPayloadContent(SBuildServer server, SBuildType buildType, BuildStateEnum buildState, SortedMap<String, String> extraParameters) {
			populateCommonContent(server, buildType, buildState);
			this.extraParameters =  new ExtraParametersMap(extraParameters);
		}

		public WebHookPayloadContent(SBuildServer server, SRunningBuild sRunningBuild, SFinishedBuild previousBuild, 
				BuildStateEnum buildState, 
				SortedMap<String, String> extraParameters) {
			
    		populateCommonContent(server, sRunningBuild, previousBuild, buildState);
    		populateMessageAndText(sRunningBuild, buildState);
    		populateArtifacts(sRunningBuild);
    		this.extraParameters =  new ExtraParametersMap(extraParameters);
		}

		private void populateArtifacts(SRunningBuild runningBuild) {
			//ArtifactsInfo artInfo = new ArtifactsInfo(runningBuild);
			//artInfo.
			
		}

		private void populateCommonContent(SBuildServer server, SBuildType buildType, BuildStateEnum state) {
			setNotifyType(state.getShortName());
			setBuildRunner(buildType.getBuildRunners());
			setBuildFullName(buildType.getFullName().toString());
			setBuildName(buildType.getName());
			setBuildTypeId(buildType.getBuildTypeId());
			setProjectName(buildType.getProjectName());
			setProjectId(buildType.getProjectId());
			setBuildStatusUrl(server.getRootUrl() + "/viewLog.html?buildTypeId=" + buildType.getBuildTypeId() + "&buildId=lastFinished");
		}
		
		private void populateMessageAndText(SRunningBuild sRunningBuild,
				BuildStateEnum state) {
			// Message is a long form message, for on webpages or in email.
    		setMessage("Build " + sRunningBuild.getBuildType().getFullName().toString() 
    				+ " has " + state.getDescriptionSuffix() + ". This is build number " + sRunningBuild.getBuildNumber() 
    				+ ", has a status of \"" + this.buildResult + "\" and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
    		
			// Text is designed to be shorter, for use in Text messages and the like.    		
    		setText(sRunningBuild.getBuildType().getFullName().toString() 
    				+ " has " + state.getDescriptionSuffix() + ". Status: " + this.buildResult);
		}

		private void populateCommonContent(SBuildServer server, SRunningBuild sRunningBuild, SFinishedBuild previousBuild,
				BuildStateEnum buildState) {
			setBuildStatus(sRunningBuild.getStatusDescriptor().getText());
			setBuildResult(sRunningBuild, previousBuild, buildState);
    		setNotifyType(buildState.getShortName());
    		setBuildRunner(sRunningBuild.getBuildType().getBuildRunners());
    		setBuildFullName(sRunningBuild.getBuildType().getFullName().toString());
    		setBuildName(sRunningBuild.getBuildType().getName());
			setBuildId(Long.toString(sRunningBuild.getBuildId()));
    		setBuildTypeId(sRunningBuild.getBuildType().getBuildTypeId());
    		setProjectName(sRunningBuild.getBuildType().getProjectName());
    		setProjectId(sRunningBuild.getBuildType().getProjectId());
    		setBuildNumber(sRunningBuild.getBuildNumber());
    		setAgentName(sRunningBuild.getAgentName());
    		setAgentOs(sRunningBuild.getAgent().getOperatingSystemName());
    		setAgentHostname(sRunningBuild.getAgent().getHostName());
    		setTriggeredBy(sRunningBuild.getTriggeredBy().getAsString());
    		setBuildStatusUrl(server.getRootUrl() + "/viewLog.html?buildTypeId=" + getBuildTypeId() + "&buildId=" + getBuildId());
		}
		
		private void setBuildResult(SRunningBuild sRunningBuild,
				SFinishedBuild previousBuild, BuildStateEnum buildState) {

			if (previousBuild != null){
				if (previousBuild.isFinished()){ 
					if (previousBuild.getStatusDescriptor().isSuccessful()){
						this.buildResultPrevious = WebHookPayload.BUILD_STATUS_SUCCESS;
					} else {
						this.buildResultPrevious = WebHookPayload.BUILD_STATUS_FAILURE;
					}
				} else {
					this.buildResultPrevious = WebHookPayload.BUILD_STATUS_RUNNING;
				}
			} else {
				this.buildResultPrevious = WebHookPayload.BUILD_STATUS_UNKNOWN;
			}

			if (buildState == BuildStateEnum.BEFORE_BUILD_FINISHED || buildState == BuildStateEnum.BUILD_FINISHED){ 
				if (sRunningBuild.getStatusDescriptor().isSuccessful()){
					this.buildResult = WebHookPayload.BUILD_STATUS_SUCCESS;
					if (this.buildResultPrevious.equals(this.buildResult)){
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_NO_CHANGE;
					} else {
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_FIXED;
					}
				} else {
					this.buildResult = WebHookPayload.BUILD_STATUS_FAILURE;
					if (this.buildResultPrevious.equals(this.buildResult)){
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_NO_CHANGE;
					} else {
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_BROKEN;
					}
				}
			} else {
				this.buildResult = WebHookPayload.BUILD_STATUS_RUNNING;
				this.buildResultDelta = WebHookPayload.BUILD_STATUS_UNKNOWN;
			}
			
		}

		// Getters and setters
		
		public String getBuildStatus() {
			return buildStatus;
		}

		public void setBuildStatus(String buildStatus) {
			this.buildStatus = buildStatus;
		}

		public String getBuildStatusPrevious() {
			return buildStatusPrevious;
		}

		public void setBuildStatusPrevious(String buildStatusPrevious) {
			this.buildStatusPrevious = buildStatusPrevious;
		}

		public String getBuildResultDelta() {
			return buildResultDelta;
		}

		public void setBuildResultDelta(String buildResultDelta) {
			this.buildResultDelta = buildResultDelta;
		}

		public String getNotifyType() {
			return notifyType;
		}

		public void setNotifyType(String notifyType) {
			this.notifyType = notifyType;
		}

		public List<String> getBuildRunner() {
			return buildRunners;
		}

		public void setBuildRunner(List<SBuildRunnerDescriptor> list) {
			if (list != null){
				buildRunners = new ArrayList<String>(); 
				for (SBuildRunnerDescriptor runner : list){
					buildRunners.add(runner.getRunType().getDisplayName());
				}
			}
		}

		public String getBuildFullName() {
			return buildFullName;
		}

		public void setBuildFullName(String buildFullName) {
			this.buildFullName = buildFullName;
		}

		public String getBuildName() {
			return buildName;
		}

		public void setBuildName(String buildName) {
			this.buildName = buildName;
		}

		public String getBuildId() {
			return buildId;
		}

		public void setBuildId(String buildId) {
			this.buildId = buildId;
		}

		public String getBuildTypeId() {
			return buildTypeId;
		}

		public void setBuildTypeId(String buildTypeId) {
			this.buildTypeId = buildTypeId;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public String getProjectId() {
			return projectId;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		public String getBuildNumber() {
			return buildNumber;
		}

		public void setBuildNumber(String buildNumber) {
			this.buildNumber = buildNumber;
		}

		public String getAgentName() {
			return agentName;
		}

		public void setAgentName(String agentName) {
			this.agentName = agentName;
		}

		public String getAgentOs() {
			return agentOs;
		}

		public void setAgentOs(String agentOs) {
			this.agentOs = agentOs;
		}

		public String getAgentHostname() {
			return agentHostname;
		}

		public void setAgentHostname(String agentHostname) {
			this.agentHostname = agentHostname;
		}

		public String getTriggeredBy() {
			return triggeredBy;
		}

		public void setTriggeredBy(String triggeredBy) {
			this.triggeredBy = triggeredBy;
		}

		public String getBuildStatusUrl() {
			return buildStatusUrl;
		}

		public void setBuildStatusUrl(String buildStatusUrl) {
			this.buildStatusUrl = buildStatusUrl;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getMessage() {
			return message;
		}


		public void setMessage(String message) {
			this.message = message;
		}


		public String getText() {
			return text;
		}


		public void setText(String text) {
			this.text = text;
		}

		public ExtraParametersMap getExtraParameters() {
			if (this.extraParameters.size() > 0){
				return extraParameters;
			} else {
				return null;
			}
				
		}

		public void setExtraParameters(SortedMap<String, String> extraParameters) {
			this.extraParameters = new ExtraParametersMap(extraParameters);
		}

		
}