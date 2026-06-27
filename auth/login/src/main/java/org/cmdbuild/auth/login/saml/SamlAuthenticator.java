package org.cmdbuild.auth.login.saml;

import jakarta.annotation.Nullable;
import org.cmdbuild.auth.login.AuthRequestInfo;
import org.cmdbuild.auth.login.LoginModuleClientRequestAuthenticator;
import org.cmdbuild.auth.login.LoginUserIdentity;
import org.cmdbuild.auth.login.RequestAuthenticatorResponse;
import org.cmdbuild.auth.login.SessionDataSupplier;
import org.cmdbuild.ui.UiBaseUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SamlAuthenticator implements LoginModuleClientRequestAuthenticator<SamlAuthenticatorConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(SamlAuthenticator.class);
    private final UiBaseUrlService baseUrlService;
    private final SamlUserScriptService userScriptService;
    private final SessionDataSupplier sessionDataService;

    public SamlAuthenticator(UiBaseUrlService baseUrlService, SamlUserScriptService userScriptService, SessionDataSupplier sessionDataService) {
        this.baseUrlService = baseUrlService;
        this.userScriptService = userScriptService;
        this.sessionDataService = sessionDataService;
    }

    @Override
    public String getType() {
        return SamlAuthenticatorConfiguration.SAML_LOGIN_MODULE_TYPE;
    }

    @Override
    @Nullable
    public RequestAuthenticatorResponse<LoginUserIdentity> handleAuthRequest(AuthRequestInfo request, SamlAuthenticatorConfiguration loginModuleConfiguration) {
        logger.warn("SAML authentication is not available (library stubbed)");
        return null;
    }

    @Override
    @Nullable
    public RequestAuthenticatorResponse<LoginUserIdentity> handleAuthResponse(AuthRequestInfo request, SamlAuthenticatorConfiguration loginModuleConfiguration) {
        logger.warn("SAML authentication is not available (library stubbed)");
        return null;
    }

    @Override
    @Nullable
    public RequestAuthenticatorResponse<Void> logout(@Nullable Object request, SamlAuthenticatorConfiguration loginModuleConfiguration) {
        logger.warn("SAML authentication is not available (library stubbed)");
        return null;
    }

    public interface AuthResponse {
        @Nullable
        String getAttribute(String name);

        @Nullable
        String getNameId();
    }
}
