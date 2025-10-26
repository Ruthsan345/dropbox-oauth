package com.dropbox.demo.service.impl;

import com.dropbox.demo.dao.TokenData;
import com.dropbox.demo.service.DropBoxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class DropBoxServiceImpl implements DropBoxService {

    @Value("${dropbox.client.id}")
    String clientId;

    @Value("${dropbox.client.secret}")
    private String clientSecret;

    @Value("${dropbox.token.url}")
    private String accessTokenUrl;

    @Value("${dropbox.team.api.url}")
    private String teamApiUrl;

    @Value("${dropbox.memberlist.api.url}")
    private String memberApiUrl;

    @Value("${dropbox.token.url}")
    private String tokenUrl;

    private final WebClient webClient = WebClient.builder().build();

    private final Map<String, TokenData> basicTokenCache = new ConcurrentHashMap<>();


    public String getAccessToken() {
        log.info("fetching token");
        TokenData tokenData = basicTokenCache.get("dropboxTokenData");

        if (tokenData.isExpired()) {
            log.info("Token expired, attempting refresh...");
            return refreshAccessToken();
        }

        return tokenData.getAccessToken();
    }

    public void setTokens(TokenData tokenData) {
        log.info("set token data");
        basicTokenCache.put("dropboxTokenData", tokenData);
    }

    public Map getTeamInfo() {
        String accessToken = getAccessToken();

        if (Objects.isNull(accessToken)) {
            return setErrorInfo();
        }

        Map teamInfo = webClient.post()
                .uri(teamApiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("Team info obtained {}", teamInfo);
        return teamInfo;
    }

    private Map setErrorInfo() {
        return Map.of(
                "status", "Error",
                "access_token", "EMPTY",
                "message", "Access token is expired or removed",
                "next_steps", "please retry auth api again"
        );
    }

    public Map getMemberList() {

        String accessToken = getAccessToken();

        if (Objects.isNull(accessToken)) {
            return setErrorInfo();
        }

        return webClient.post()
                .uri(memberApiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue("{\"limit\": 100}")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }


    private String refreshAccessToken() {
        TokenData oldTokenData = basicTokenCache.get("dropboxTokenData");

        if (Objects.isNull(oldTokenData) || Objects.isNull(oldTokenData.getRefreshToken())) {
            throw new RuntimeException("No refresh token available. Please try to reauthorize.");
        }

        try {
            String credentials = clientId + ":" + clientSecret;
            String base64Credentials = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            String formData = "grant_type=refresh_token" +
                    "&refresh_token=" + oldTokenData.getRefreshToken();

            Map response = webClient.post()
                    .uri(tokenUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + base64Credentials)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (Objects.isNull(response) || !response.containsKey("access_token")) {
                throw new RuntimeException("Failed to refresh token");
            }

            long expiresInSeconds = ((Number) response.get("expires_in")).longValue();
            long expiryTimeMs = System.currentTimeMillis() + (expiresInSeconds * 1000);

            String newRefreshToken = (String) response.get("refresh_token");

            if (Objects.isNull(newRefreshToken)) {
                newRefreshToken = oldTokenData.getRefreshToken();
            }
            setTokens(TokenData.builder()
                    .accessToken((String) response.get("access_token"))
                    .refreshToken(newRefreshToken)
                    .expiryTime(expiryTimeMs).build());

            return (String) response.get("access_token");

        } catch (Exception e) {
            System.err.println("Token refresh failed: " + e.getMessage());
            throw new RuntimeException("Token refresh failed. Please re-authenticate.");
        }
    }
}
