package org.trackdev.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.trackdev.api.configuration.TrackDevProperties;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.DiscordInfo;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.DiscordInfoRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.Optional;

@Service
public class DiscordService extends BaseServiceUUID<DiscordInfo, DiscordInfoRepository> {

    private static final Logger log = LoggerFactory.getLogger(DiscordService.class);

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";
    private static final String TOKEN_URL = DISCORD_API_BASE + "/oauth2/token";
    private static final String USER_ME_URL = DISCORD_API_BASE + "/users/@me";
    private static final String AUTHORIZE_URL = "https://discord.com/oauth2/authorize";
    private static final int DISCORD_MAX_NICKNAME_LENGTH = 32;

    @Autowired
    private TrackDevProperties properties;

    @Autowired
    private UserService userService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DiscordService() {
        this.restTemplate = new RestTemplate(
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().build()));
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Build the Discord OAuth2 authorization URL.
     */
    public String buildAuthorizationUrl(String state) {
        TrackDevProperties.Discord discord = properties.getDiscord();
        return AUTHORIZE_URL
                + "?client_id=" + encode(discord.getClientId())
                + "&redirect_uri=" + encode(discord.getRedirectUri())
                + "&response_type=code"
                + "&scope=" + encode("identify guilds.join")
                + "&state=" + encode(state);
    }

    /**
     * Exchange the OAuth2 authorization code for access + refresh tokens.
     * Returns a map with keys: access_token, refresh_token, token_type, expires_in,
     * scope.
     */
    public Map<String, Object> exchangeCodeForTokens(String code) {
        TrackDevProperties.Discord discord = properties.getDiscord();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", discord.getClientId());
        body.add("client_secret", discord.getClientSecret());
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", discord.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            return objectMapper.convertValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.error("Discord OAuth token exchange failed", e);
            throw new ServiceException(ErrorConstants.DISCORD_OAUTH_FAILED, e);
        }
    }

    /**
     * Fetch the authenticated Discord user's information using the access token.
     */
    public JsonNode getDiscordUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_ME_URL, HttpMethod.GET, request, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to fetch Discord user info", e);
            throw new ServiceException(ErrorConstants.DISCORD_OAUTH_FAILED, e);
        }
    }

    /**
     * Add a user to the Discord guild using the bot token and the user's OAuth
     * access token.
     */
    public void addToGuild(String discordUserId, String accessToken) {
        TrackDevProperties.Discord discord = properties.getDiscord();
        String url = DISCORD_API_BASE + "/guilds/" + discord.getGuildId() + "/members/" + discordUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + discord.getBotToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(Map.of("access_token", accessToken));
        } catch (Exception e) {
            throw new ServiceException(ErrorConstants.DISCORD_GUILD_JOIN_FAILED, e);
        }
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            log.info("Added Discord user {} to guild {}", discordUserId, discord.getGuildId());
        } catch (HttpClientErrorException e) {
            // 204 No Content = user already in guild — that's fine
            if (e.getStatusCode() != HttpStatus.NO_CONTENT) {
                log.error("Failed to add Discord user to guild: {}", e.getMessage());
                throw new ServiceException(ErrorConstants.DISCORD_GUILD_JOIN_FAILED);
            }
        } catch (Exception e) {
            log.error("Failed to add Discord user to guild", e);
            throw new ServiceException(ErrorConstants.DISCORD_GUILD_JOIN_FAILED, e);
        }
    }

    /**
     * Assign the verified role to a user in the Discord guild.
     */
    public void assignVerifiedRole(String discordUserId) {
        TrackDevProperties.Discord discord = properties.getDiscord();
        String url = DISCORD_API_BASE + "/guilds/" + discord.getGuildId()
                + "/members/" + discordUserId + "/roles/" + discord.getVerifiedRoleId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + discord.getBotToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            log.info("Assigned verified role to Discord user {}", discordUserId);
        } catch (Exception e) {
            log.error("Failed to assign verified role to Discord user: {}", e.getMessage());
            throw new ServiceException(ErrorConstants.DISCORD_ROLE_ASSIGN_FAILED, e);
        }
    }

    /**
     * Update a member nickname in the Discord guild.
     */
    public void updateGuildNickname(String discordUserId, String fullName) {
        TrackDevProperties.Discord discord = properties.getDiscord();
        String url = DISCORD_API_BASE + "/guilds/" + discord.getGuildId()
                + "/members/" + discordUserId;

        String nickname = fullName == null ? "" : fullName.trim();
        if (nickname.length() > DISCORD_MAX_NICKNAME_LENGTH) {
            nickname = nickname.substring(0, DISCORD_MAX_NICKNAME_LENGTH);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + discord.getBotToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(Map.of("nick", nickname));
        } catch (Exception e) {
            throw new ServiceException(ErrorConstants.DISCORD_NICKNAME_UPDATE_FAILED, e);
        }

        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);
            log.info("Updated Discord nickname for user {}", discordUserId);
        } catch (Exception e) {
            log.error("Failed to update Discord nickname for user {}", discordUserId, e);
            throw new ServiceException(ErrorConstants.DISCORD_NICKNAME_UPDATE_FAILED, e);
        }
    }

    /**
     * Kick a member from the Discord guild.
     */
    private void kickFromGuild(String discordUserId) {
        TrackDevProperties.Discord discord = properties.getDiscord();
        String url = DISCORD_API_BASE + "/guilds/" + discord.getGuildId()
                + "/members/" + discordUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + discord.getBotToken());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Kicked Discord user {} from guild", discordUserId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Discord user {} not found in guild, skipping kick", discordUserId);
        } catch (Exception e) {
            log.error("Failed to kick Discord user: {}", e.getMessage());
            throw new ServiceException(ErrorConstants.DISCORD_KICK_FAILED, e);
        }
    }

    /**
     * Link the Discord account to the TrackDev user.
     * Exchanges the code, fetches the user, joins the guild, and assigns the role.
     */
    public void linkDiscordAccount(User user, String code) {
        DiscordInfo userDiscordInfo = user.getDiscordInfo();
        if (userDiscordInfo == null) {
            userDiscordInfo = new DiscordInfo();
            user.setDiscordInfo(userDiscordInfo);
        }

        // 1. Exchange code for tokens
        Map<String, Object> tokens = exchangeCodeForTokens(code);
        String accessToken = (String) tokens.get("access_token");
        String refreshToken = (String) tokens.get("refresh_token");

        // 2. Fetch Discord user info
        JsonNode discordUser = getDiscordUser(accessToken);
        String discordId = discordUser.get("id").asText();

        // Check if this Discord account is already linked to another user
        Optional<DiscordInfo> existingDiscordInfo = repo().findByDiscordId(discordId);

        if (existingDiscordInfo.isPresent()
                && (userDiscordInfo.getId() == null
                        || !existingDiscordInfo.get().getId().equals(userDiscordInfo.getId()))) {
            throw new ServiceException(ErrorConstants.DISCORD_ALREADY_LINKED);
        }

        String username = discordUser.get("username").asText();
        String discriminator = discordUser.has("discriminator") ? discordUser.get("discriminator").asText() : "0";
        String avatarHash = discordUser.has("avatar") && !discordUser.get("avatar").isNull()
                ? discordUser.get("avatar").asText()
                : null;

        // 3. Add to guild
        addToGuild(discordId, accessToken);

        updateGuildNickname(discordId, user.getFullName());
        assignVerifiedRole(discordId);

        // 6. Save Discord info to user
        userDiscordInfo.setDiscordId(discordId);
        userDiscordInfo.setUsername(username);
        userDiscordInfo.setDiscriminator(discriminator);
        userDiscordInfo.setAvatarHash(avatarHash);
        userDiscordInfo.setAccessToken(accessToken);
        userDiscordInfo.setRefreshToken(refreshToken);

        userService.save(user);
        log.info("Linked Discord account {} to user {}", username, user.getUsername());
    }

    /**
     * Unlink the Discord account from the TrackDev user.
     */
    public void unlinkDiscordAccount(User user) {
        DiscordInfo discordInfo = user.getDiscordInfo();

        if (discordInfo == null || !discordInfo.isLinked()) {
            throw new ServiceException(ErrorConstants.DISCORD_NOT_LINKED);
        }

        String discordId = discordInfo.getDiscordId();

        if (discordId != null) {
            try {
                kickFromGuild(discordId);
            } catch (Exception e) {
                log.error("Failed to kick user from Discord during unlink: {}", e.getMessage());
                throw e;
            }
        }

        discordInfo.clear();
        userService.save(user);
        log.info("Unlinked Discord account from user {}", user.getUsername());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
