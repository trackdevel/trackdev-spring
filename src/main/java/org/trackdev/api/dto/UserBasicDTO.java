package org.trackdev.api.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * DTO for User - Basic level (used in task assignments, member lists, etc.)
 */
@Data
public class UserBasicDTO {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String color;
    private String capitalLetters;
    private Long currentProject;
    private Set<String> roles;
    private Date lastLogin;
    private Boolean changePassword;
    private Boolean enabled;
    private GithubInfoDTO githubInfo;
}
