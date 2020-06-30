package com.cloud.config.client.vault;


import static com.cloud.config.client.vault.VaultConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class VaultAuthenticator {

    private enum VaultAuthenticationType {
        GITHUB_ORG, APP_ROLE
    }

    private VaultAuthenticationRequest vaultAuthenticationRequest;

    @Autowired
    public VaultAuthenticator(Environment env) {
        VaultAuthenticationType vaultAuthenticationType = env
                .getProperty(AUTHENTICATION_TYPE, VaultAuthenticationType.class);

        vaultAuthenticationRequest = new VaultAuthenticationRequest();

        switch (vaultAuthenticationType) {
            case APP_ROLE:
                vaultAuthenticationRequest.setLoginPath(env.getProperty(APPROLE_LOGINPATH, DEFAULT_APPROLE_LOGINPATH));
                vaultAuthenticationRequest.getRequestBody().put(ROLE_ID, env.getProperty(APPROLE_ROLEID));
                vaultAuthenticationRequest.getRequestBody().put(SECRET_ID, env.getProperty(APPROLE_SECRETID));
                break;
            case GITHUB_ORG:
                vaultAuthenticationRequest.setLoginPath(env.getProperty(GITHUB_LOGINPATH, DEFAULT_GITHUB_LOGINPATH));
                vaultAuthenticationRequest.getRequestBody().put(TOKEN, env.getProperty(GITHUB_ACCESSKEY));
                break;
            default:
                break;
        }
    }

    public VaultAuthenticationRequest getVaultAuthenticationRequest() {
        return this.vaultAuthenticationRequest;
    }

    public class VaultAuthenticationRequest {
        private String loginPath;
        private Map<String, String> requestBody;

        public String getLoginPath() {
            return loginPath;
        }

        public void setLoginPath(String loginPath) {
            this.loginPath = loginPath;
        }

        public Map<String, String> getRequestBody() {
            if (requestBody == null) {
                requestBody = new HashMap<>();
            }
            return requestBody;
        }

        public void setRequestBody(Map<String, String> requestBody) {
            this.requestBody = requestBody;
        }

    }
}