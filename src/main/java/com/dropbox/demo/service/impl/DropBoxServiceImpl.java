package com.dropbox.demo.service.impl;

import com.dropbox.demo.service.DropBoxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final WebClient webClient = WebClient.builder().build();

    private final Map<String, String> basicTokenCache = new ConcurrentHashMap<>();


    public String getAccessToken() {
        log.info("fetching token");
        return basicTokenCache.get("dropboxAccessToken");
    }

    public String cacheAccessToken(String accessToken) {
        basicTokenCache.put("dropboxAccessToken", accessToken);
        log.info("Access token cached in Spring Cache");
        return accessToken;
    }


    public void setAccessToken(String accessToken) {
        cacheAccessToken(accessToken);
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

        Map<String, Object> members = webClient.post()
                .uri(memberApiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue("{\"limit\": 100}")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return members;
    }

}
