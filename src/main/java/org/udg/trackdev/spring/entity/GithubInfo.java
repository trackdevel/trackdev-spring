package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @JsonView(PrivacyLevelViews.Private.class)
    public String getGithub_token() { return github_token; }

    public String setGithubToken(String githubToken) { return this.github_token = githubToken; }

    @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
    public String getLogin() { return login; }

    public String setLogin(String login) { return this.login = login; }

    @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
    public String getAvatar_url() { return avatar_url; }

    public String setAvatar_url(String avatar_url) { return this.avatar_url = avatar_url; }

    @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
    public String getHtml_url() { return html_url; }

    public String setHtml_url(String html_url) { return this.html_url = html_url; }

}
