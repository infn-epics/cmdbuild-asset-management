/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.command;


import com.google.common.collect.Ordering;
import jakarta.servlet.http.HttpServletRequest;
import org.cmdbuild.auth.grant.UserPrivilegesForObject;
import org.cmdbuild.auth.login.LoginDataImpl;
import org.cmdbuild.auth.login.RequestAuthenticatorResponse;
import org.cmdbuild.auth.session.SessionService;
import org.cmdbuild.auth.session.model.Session;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.auth.user.SessionType;
import org.cmdbuild.config.CoreConfiguration;
import org.cmdbuild.service.rest.v4.model.WsSessionData;
import org.cmdbuild.service.rest.v4.wshelpers.SessionWsCommons;
import org.cmdbuild.utils.lang.CmMapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 * @author ldare
 */
@Component
public class SessionsWsCommand extends SessionWsCommons {

    private final CoreConfiguration coreConfiguration;

    public SessionsWsCommand(SessionService sessionService, CoreConfiguration coreConfiguration) {
        super(sessionService);
        this.coreConfiguration = checkNotNull(coreConfiguration);
    }

    public String doCreateSessionId(WsSessionData WsSessionData, SessionType sessionType, Boolean serviceUsersAllowed) {
        return sessionService.create(LoginDataImpl.builder()
                .withLoginString(WsSessionData.getUsername())
                .withPassword(WsSessionData.getPassword())
                .withGroupName(WsSessionData.getRole())
                .withServiceUsersAllowed(serviceUsersAllowed)
                .withIgnoreTenantPolicies(WsSessionData.ignoreTenants)
                .withTargetDevice(WsSessionData.device)
                .withSessionType(sessionType)
                .build());
    }

    public Session doReadOne(String sessionId) {
        return sessionService.getSessionById(sessionId);
    }

    public Set<Map.Entry<String, UserPrivilegesForObject>> doReadPrivileges(String sessionId) {
        sessionId = sessionIdOrCurrent(sessionId);
        checkArgument(sessionService.exists(sessionId), "session not found for id = %s", sessionId);
        OperationUser user = sessionService.getUser(sessionId);
        return user.getPrivilegeContext().getAllPrivileges().entrySet();
    }

    public List<Session> doReadAll() {
        return sessionService.getAllSessions().stream().sorted(Ordering.natural().onResultOf(Session::getLastActiveDate).reversed()).collect(toList());
    }

    public Session doUpdate(String sessionId, WsSessionData sessionData, Boolean includeExtendedData) {
        sessionId = sessionIdOrCurrent(sessionId);
        checkArgument(sessionService.exists(sessionId), "session not found for id = %s", sessionId);
        checkArgument(!isBlank(sessionData.getRole()), "'group' param cannot be null");
        OperationUser currentOperationUser = sessionService.getUser(sessionId);
        sessionService.update(sessionId, LoginDataImpl.builder()
                .withLoginString(currentOperationUser.getLoginUser().getUsername())
                .withGroupName(sessionData.getRole())
                .withDefaultTenant(sessionData.getDefaultTenant())
                .withActiveTenants(sessionData.getActiveTenants())
                .withIgnoreTenantPolicies(sessionData.ignoreTenants)
                .withTargetDevice(sessionData.device)
                .withServiceUsersAllowed(true)//TODO use scope
                .build());
        return sessionService.getSessionById(sessionId);
    }

    public RequestAuthenticatorResponse<Void> doDelete(HttpServletRequest request, String sessionId) {
        sessionId = sessionIdOrCurrent(sessionId);
        checkArgument(sessionService.exists(sessionId), "session not found for id = %s", sessionId);
        return sessionService.deleteSession(sessionId, request);
    }

    public void doDeleteAll() {
        logger.info("delete all sessions");
        sessionService.deleteAll();
    }

    public CmMapUtils.FluentMap<String, Object> doKeepAlive() {
        return map("timeToLiveSeconds", coreConfiguration.getSessionTimeoutOrDefault(), "recommendedKeepaliveIntervalSeconds", coreConfiguration.getSessionTimeoutOrDefault() / 3);
    }


}
