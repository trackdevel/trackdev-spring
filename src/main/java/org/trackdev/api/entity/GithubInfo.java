package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "github_users_info")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubInfo extends BaseEntityUUID{

    @OneToOne(mappedBy = "githubInfo")
    private User user;

    private String github_token;

    private String login;

    private String avatar_url;

    private String html_url;

    public GithubInfo() {}

    public String getGithub_token() { return github_token; }

    public String setGithubToken(String githubToken) { return this.github_token = githubToken; }

    public String getLogin() { return login; }

    public String setLogin(String login) { return this.login = login; }

    public String getAvatar_url() { return avatar_url; }

    public String setAvatar_url(String avatar_url) { return this.avatar_url = avatar_url; }

    public String getHtml_url() { return html_url; }

    public String setHtml_url(String html_url) { return this.html_url = html_url; }

}
