package com.cloud.config.client.vault;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.environment.ConfigTokenProvider;
import org.springframework.cloud.config.server.environment.VaultEnvironmentProperties;
import org.springframework.cloud.config.server.environment.VaultEnvironmentRepositoryFactory.VaultRestTemplateFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class VaultTokenProvider implements ConfigTokenProvider {
    private final RestTemplate restTemplate;
    private final String loginRequestUrl;
    private final Map<String, String> loginRequestBody;
    private TokenCache tokenCache;

    @Autowired
    public VaultTokenProvider(VaultAuthenticator vaultAuthenticator, VaultEnvironmentProperties environmentProperties, Optional<VaultRestTemplateFactory> vaultRestTemplateFactory) throws Exception {
        if (vaultRestTemplateFactory.isPresent()) {
            restTemplate = vaultRestTemplateFactory.get().build(environmentProperties);
        } else {
            restTemplate = new RestTemplate();
        }

        loginRequestUrl = String.format("%s://%s:%s%s", environmentProperties.getScheme(), environmentProperties.getHost(), environmentProperties.getPort(), vaultAuthenticator.getVaultAuthenticationRequest().getLoginPath());
        loginRequestBody = vaultAuthenticator.getVaultAuthenticationRequest().getRequestBody();

        tokenCache = new TokenCache();
    }

    @Override
    public String getToken() {
        if (tokenCache.token == null || System.currentTimeMillis() > tokenCache.expirationTime) {
            aquireAndCacheToken();
        }
        return tokenCache.token;
    }

    private void aquireAndCacheToken() {
        synchronized (tokenCache) {
            if (tokenCache.token == null || System.currentTimeMillis() > tokenCache.expirationTime) {
                ResponseEntity<VaultLoginResponse> vaultLoginResponse = restTemplate.postForEntity(loginRequestUrl, loginRequestBody, VaultLoginResponse.class);
                if (vaultLoginResponse.getStatusCode() == HttpStatus.OK
                        && vaultLoginResponse.getBody() != null
                        && vaultLoginResponse.getBody().getAuth() != null) {
                    tokenCache.token = vaultLoginResponse.getBody().getAuth().getClientToken();
                    tokenCache.expirationTime = Math.addExact(System.currentTimeMillis(), Math.subtractExact(vaultLoginResponse.getBody().getAuth().getLeaseDurationInMills(), 2 * 60 * 1000));
                }
            }
        }
    }

    class TokenCache {
        String token;
        long expirationTime;
    }

}