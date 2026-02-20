package org.trackdev.api.entity;

import jakarta.persistence.*;
import org.springframework.lang.NonNull;

/**
 * Stores the actual value of a profile attribute for a specific pull request.
 * Only applies to attributes with target = PULL_REQUEST.
 */
@Entity
@Table(name = "pull_request_attribute_values", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pull_request_id", "attribute_id"})
})
public class PullRequestAttributeValue extends BaseEntityLong {

    public static final int VALUE_LENGTH = 500;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private PullRequest pullRequest;

    @Column(name = "pull_request_id", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String pullRequestId;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private ProfileAttribute attribute;

    @Column(name = "attribute_id", insertable = false, updatable = false)
    private Long attributeId;

    /**
     * The actual value stored as a string.
     */
    @Column(length = VALUE_LENGTH)
    private String value;

    public PullRequestAttributeValue() {}

    public PullRequestAttributeValue(PullRequest pullRequest, ProfileAttribute attribute, String value) {
        this.pullRequest = pullRequest;
        this.attribute = attribute;
        this.value = value;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    public String getPullRequestId() {
        return pullRequestId;
    }

    public ProfileAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(ProfileAttribute attribute) {
        this.attribute = attribute;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
