package org.trackdev.api.dto;

public class NotificationPreferencesDTO {

    private Boolean notifyComments;
    private Boolean notifyPointsReview;
    private Boolean notifyTeamActivity;

    public NotificationPreferencesDTO() {}

    public NotificationPreferencesDTO(Boolean notifyComments,
                                      Boolean notifyPointsReview,
                                      Boolean notifyTeamActivity) {
        this.notifyComments = notifyComments;
        this.notifyPointsReview = notifyPointsReview;
        this.notifyTeamActivity = notifyTeamActivity;
    }

    public Boolean getNotifyComments() { return notifyComments; }
    public void setNotifyComments(Boolean notifyComments) { this.notifyComments = notifyComments; }

    public Boolean getNotifyPointsReview() { return notifyPointsReview; }
    public void setNotifyPointsReview(Boolean notifyPointsReview) { this.notifyPointsReview = notifyPointsReview; }

    public Boolean getNotifyTeamActivity() { return notifyTeamActivity; }
    public void setNotifyTeamActivity(Boolean notifyTeamActivity) { this.notifyTeamActivity = notifyTeamActivity; }
}
