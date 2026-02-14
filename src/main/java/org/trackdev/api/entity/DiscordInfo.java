package org.trackdev.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.trackdev.api.converter.EncryptedStringConverter;

/**
 * Entity storing Discord account information linked to a TrackDev user.
 * Follows the same pattern as {@link GithubInfo}.
 */
@Entity
@Table(name = "discord_users_info")
public class DiscordInfo extends BaseEntityUUID {

    @OneToOne(mappedBy = "discordInfo")
    private User user;

    @Column(length = 256)
    private String discordId;

    @Column(length = 256)
    private String username;

    @Column(length = 10)
    private String discriminator;

    @Column(length = 512)
    private String avatarHash;

    @Column(length = 512) // Increased for encrypted data
    @Convert(converter = EncryptedStringConverter.class)
    private String accessToken;

    @Column(length = 512) // Increased for encrypted data
    @Convert(converter = EncryptedStringConverter.class)
    private String refreshToken;

    public DiscordInfo() {
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getAvatarHash() {
        return avatarHash;
    }

    public void setAvatarHash(String avatarHash) {
        this.avatarHash = avatarHash;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Checks whether this Discord info has been linked (i.e. has a Discord ID).
     */
    public boolean isLinked() {
        return discordId != null && !discordId.isBlank();
    }

    /**
     * Clears all Discord account data (used when unlinking).
     */
    public void clear() {
        this.discordId = null;
        this.username = null;
        this.discriminator = null;
        this.avatarHash = null;
        this.accessToken = null;
        this.refreshToken = null;
    }
}
