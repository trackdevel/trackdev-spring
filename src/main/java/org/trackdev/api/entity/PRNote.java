package org.trackdev.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pr_notes")
public class PRNote extends BaseEntityLong {

    public PRNote(PullRequest pullRequest, User subject, User author, String url, Integer level, String type) {
        this.pullRequest = pullRequest;
        this.subject = subject;
        this.author = author;
        this.url = url;
        this.level = level;
        this.type = type;
    }

    @ManyToOne
    private PullRequest pullRequest;

    @ManyToOne
    private User subject;

    @ManyToOne
    private User author;

    private String url;

    private Integer level;

    private String type;
}
