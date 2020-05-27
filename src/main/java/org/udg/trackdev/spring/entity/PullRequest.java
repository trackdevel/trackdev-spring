package org.udg.trackdev.spring.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "pull_requests")
public class PullRequest extends BaseEntityUUID {

    @ManyToOne
    private Task task;

    private String url;
}
