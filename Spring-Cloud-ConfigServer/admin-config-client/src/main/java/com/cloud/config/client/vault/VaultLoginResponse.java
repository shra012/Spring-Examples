package com.cloud.config.client.vault;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultLoginResponse {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("auth")
    private Auth auth;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Auth {

        @JsonProperty("client_token")
        private String clientToken;

        @JsonProperty("lease_duration")
        private Integer leaseDuration;

        public String getClientToken() {
            return clientToken;
        }

        public void setClientToken(String clientToken) {
            this.clientToken = clientToken;
        }

        public Integer getLeaseDuration() {
            return leaseDuration;
        }

        public Long getLeaseDurationInMills() {
            return this.leaseDuration * 1000L;
        }

        public void setLeaseDuration(Integer leaseDuration) {
            this.leaseDuration = leaseDuration;
        }
    }
}