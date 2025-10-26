package com.dropbox.demo.controller;


import com.dropbox.demo.service.DropBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Value("${dropbox.client.id}")
    private String clientId;

    @Value("${dropbox.client.secret}")
    private String clientSecret;

    @Value("${dropbox.auth.url}")
    private String authUrl;

    @Value("${dropbox.token.url}")
    private String tokenUrl;

    @Autowired
    DropBoxService dropBoxService;

    private final WebClient webClient = WebClient.builder().build();

    @GetMapping("/authorize")
    public Map<String, String> authorize() {
        String authorizationUrl = UriComponentsBuilder.fromUriString(authUrl)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", "http://localhost:8080/oauth/callback")
                .queryParam("scope", "team_info.read team_data.member members.read groups.read events.read")
                .toUriString();

        return Map.of(
                "authorization_url", authorizationUrl,
                "message", "Visit this URL in your browser to authorize"
        );
    }

    @GetMapping("/callback")
    public Map<String, Object> callback(@RequestParam("code") String code,
                                        @RequestParam(value = "state", required = false) String state) {
        try {
            String accessToken = exchangeCodeForToken(code);
            dropBoxService.setAccessToken(accessToken);


            return Map.of(
                    "status", "success",
                    "access_token", accessToken,
                    "message", "Successfully authenticated with Dropbox!",
                    "next_steps", "Use this access_token to call /api/team-info and /api/members"
            );

        } catch (Exception e) {
            return Map.of(
                    "status", "error",
                    "error", "Authentication failed: " + e.getMessage()
            );
        }
    }

    private String exchangeCodeForToken(String code) {
        String formData = "code=" + code +
                "&grant_type=authorization_code" +
                "&redirect_uri=http://localhost:8080/oauth/callback" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        Map response = webClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (Objects.isNull(response) || !response.containsKey("access_token")) {
            throw new RuntimeException("Failed to get access token from Dropbox. Response: " + response);
        }

        return (String) response.get("access_token");
    }

}
