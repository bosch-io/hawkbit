package org.eclipse.hawkbit.ui.common.data.proxies;

import java.io.Serializable;
import java.time.Duration;

import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutoCleanupConfigurationItem.ActionStatusOption;

public class ProxySystemConfigWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Long distributionSetTypeId;
    private Long repositoryConfigId;
    private Long rolloutConfigId;
    private Long caRootAuthorityId;
    private String caRootAuthority;
    private String gatewaySecurityToken;
    private ActionStatusOption actionCleanupStatus;
    private boolean  pollingOverdue;
    private Duration pollingOverdueDuration;
    private boolean  pollingTime;
    private Duration pollingTimeDuration;
    private String actionExpiryDays;
    private boolean rolloutApproval;
    private boolean actionAutoclose;
    private boolean actionAutocleanup;
    private boolean multiAssignments;
    private boolean certificateAuth;
    private boolean targetSecToken;
    private boolean gatewaySecToken;
    private boolean downloadAnonymous;
    private Long authConfigId;
    private Long pollingConfigId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaRootAuthority() {
        return caRootAuthority;
    }

    public void setCaRootAuthority(String caRootAuthority) {
        this.caRootAuthority = caRootAuthority;
    }

    public String getGatewaySecurityToken() {
        return gatewaySecurityToken;
    }

    public void setGatewaySecurityToken(String gatewaySecurityToken) {
        this.gatewaySecurityToken = gatewaySecurityToken;
    }

    public boolean isPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(boolean pollingTime) {
        this.pollingTime = pollingTime;
    }

    public Duration getPollingTimeDuration() {
        return pollingTimeDuration;
    }

    public void setPollingTimeDuration(Duration pollingTimeDuration) {
        this.pollingTimeDuration = pollingTimeDuration;
    }

    public boolean isPollingOverdue() {
        return pollingOverdue;
    }

    public void setPollingOverdue(boolean pollingOverdue) {
        this.pollingOverdue = pollingOverdue;
    }

    public Duration getPollingOverdueDuration() {
        return pollingOverdueDuration;
    }

    public void setPollingOverdueDuration(Duration pollingOverdueDuration) {
        this.pollingOverdueDuration = pollingOverdueDuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDistributionSetTypeId() {
        return distributionSetTypeId;
    }

    public String getActionExpiryDays() {
        return actionExpiryDays;
    }

    public void setActionExpiryDays(String actionExpiryDays) {
        this.actionExpiryDays = actionExpiryDays;
    }

    public ActionStatusOption getActionCleanupStatus() {
        return actionCleanupStatus;
    }

    public void setActionCleanupStatus(ActionStatusOption actionCleanupStatus) {
        this.actionCleanupStatus = actionCleanupStatus;
    }
    public Long getCaRootAuthorityId() {
        return caRootAuthorityId;
    }

    public void setCaRootAuthorityId(Long caRootAuthorityId) {
        this.caRootAuthorityId = caRootAuthorityId;
    }

    public boolean isCertificateAuth() {
        return certificateAuth;
    }

    public void setCertificateAuth(boolean certificateAuth) {
        this.certificateAuth = certificateAuth;
    }

    public boolean isTargetSecToken() {
        return targetSecToken;
    }

    public void setTargetSecToken(boolean targetSecToken) {
        this.targetSecToken = targetSecToken;
    }

    public boolean isGatewaySecToken() {
        return gatewaySecToken;
    }

    public void setGatewaySecToken(boolean gatewaySecToken) {
        this.gatewaySecToken = gatewaySecToken;
    }

    public boolean isDownloadAnonymous() {
        return downloadAnonymous;
    }

    public void setDownloadAnonymous(boolean downloadAnonymous) {
        this.downloadAnonymous = downloadAnonymous;
    }

    public boolean isActionAutocleanup() {
        return actionAutocleanup;
    }

    public void setActionAutocleanup(boolean actionAutocleanup) {
        this.actionAutocleanup = actionAutocleanup;
    }

    public boolean isMultiAssignments() {
        return multiAssignments;
    }

    public void setMultiAssignments(boolean multiAssignments) {
        this.multiAssignments = multiAssignments;
    }

    public boolean isActionAutoclose() {
        return actionAutoclose;
    }

    public void setActionAutoclose(boolean actionAutoclose) {
        this.actionAutoclose = actionAutoclose;
    }

    public boolean isRolloutApproval() {
        return rolloutApproval;
    }

    public void setRolloutApproval(boolean rolloutApproval) {
        this.rolloutApproval = rolloutApproval;
    }

    public void setDistributionSetTypeId(Long distributionSetTypeId) {
        this.distributionSetTypeId = distributionSetTypeId;
    }

    public Long getRepositoryConfigId() {
        return repositoryConfigId;
    }

    public void setRepositoryConfigId(Long repositoryConfigId) {
        this.repositoryConfigId = repositoryConfigId;
    }

    public Long getRolloutConfigId() {
        return rolloutConfigId;
    }

    public void setRolloutConfigId(Long rolloutConfigId) {
        this.rolloutConfigId = rolloutConfigId;
    }

    public Long getAuthConfigId() {
        return authConfigId;
    }

    public void setAuthConfigId(Long authConfigId) {
        this.authConfigId = authConfigId;
    }

    public Long getPollingConfigId() {
        return pollingConfigId;
    }

    public void setPollingConfigId(Long pollingConfigId) {
        this.pollingConfigId = pollingConfigId;
    }
}