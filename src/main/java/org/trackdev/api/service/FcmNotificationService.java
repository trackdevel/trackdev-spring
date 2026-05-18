package org.trackdev.api.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.trackdev.api.entity.Comment;
import org.trackdev.api.entity.PointsReviewConversation;
import org.trackdev.api.entity.PointsReviewMessage;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.UserPushToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FcmNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationService.class);
    private static final int BODY_PREVIEW_MAX = 140;

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    private final UserService userService;
    private final UserPushTokenService userPushTokenService;

    public FcmNotificationService(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider,
                                  UserService userService,
                                  UserPushTokenService userPushTokenService) {
        this.firebaseMessagingProvider = firebaseMessagingProvider;
        this.userService = userService;
        this.userPushTokenService = userPushTokenService;
    }

    public boolean isEnabled() {
        return firebaseMessagingProvider.getIfAvailable() != null;
    }

    // -- Domain-specific notification entry points --------------------------

    /**
     * Notify the task assignee (if any, and not the comment author) of a new comment.
     * Honors the recipient's notifyComments preference.
     */
    public void notifyComment(Comment comment) {
        if (!isEnabled() || comment == null) return;
        Task task = comment.getTask();
        User author = comment.getAuthor();
        if (task == null || task.getAssignee() == null) return;
        User assignee = task.getAssignee();
        if (author != null && author.getId().equals(assignee.getId())) return;
        if (!Boolean.TRUE.equals(assignee.getNotifyComments())) return;

        String authorName = author != null ? author.getFullName() : "Someone";
        String title = authorName + " commented on " + task.getTaskKey();
        String body = preview(comment.getContent());
        Map<String, String> data = baseTaskPayload("comment", task);
        data.put("commentId", String.valueOf(comment.getId()));
        sendToUserAfterCommit(assignee.getId(), title, body, data);
    }

    /**
     * Notify the points-review recipients (initiator + participants + course owner, minus the actor)
     * when a new conversation is created. Honors notifyPointsReview.
     */
    public void notifyPointsReviewCreated(PointsReviewConversation conversation, User actor) {
        if (!isEnabled() || conversation == null) return;
        Task task = conversation.getTask();
        if (task == null) return;
        Set<User> recipients = pointsReviewRecipients(conversation, actor);
        if (recipients.isEmpty()) return;

        String actorName = actor != null ? actor.getFullName() : "A team member";
        String title = "Points review on " + task.getTaskKey();
        String body = actorName + " disagrees with the estimation";
        Map<String, String> data = baseTaskPayload("points_review_created", task);
        data.put("conversationId", String.valueOf(conversation.getId()));

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyPointsReview())) continue;
            sendToUserAfterCommit(r.getId(), title, body, data);
        }
    }

    /**
     * Notify all current points-review recipients (minus the message author) of a new message.
     * Honors notifyPointsReview.
     */
    public void notifyPointsReviewMessage(PointsReviewConversation conversation,
                                          PointsReviewMessage message) {
        if (!isEnabled() || conversation == null || message == null) return;
        Task task = conversation.getTask();
        if (task == null) return;
        User author = message.getAuthor();
        Set<User> recipients = pointsReviewRecipients(conversation, author);
        if (recipients.isEmpty()) return;

        String authorName = author != null ? author.getFullName() : "Someone";
        String title = authorName + " replied on " + task.getTaskKey();
        String body = preview(message.getContent());
        Map<String, String> data = baseTaskPayload("points_review_message", task);
        data.put("conversationId", String.valueOf(conversation.getId()));
        data.put("messageId", String.valueOf(message.getId()));

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyPointsReview())) continue;
            sendToUserAfterCommit(r.getId(), title, body, data);
        }
    }

    /**
     * Notify a user just added to a points-review conversation.
     * Honors notifyPointsReview.
     */
    public void notifyPointsReviewParticipantAdded(PointsReviewConversation conversation,
                                                   User addedUser,
                                                   User actor) {
        if (!isEnabled() || conversation == null || addedUser == null) return;
        if (!Boolean.TRUE.equals(addedUser.getNotifyPointsReview())) return;
        Task task = conversation.getTask();
        if (task == null) return;

        String actorName = actor != null ? actor.getFullName() : "A professor";
        String title = "Added to points review on " + task.getTaskKey();
        String body = actorName + " added you to the discussion";
        Map<String, String> data = baseTaskPayload("points_review_added", task);
        data.put("conversationId", String.valueOf(conversation.getId()));
        sendToUserAfterCommit(addedUser.getId(), title, body, data);
    }

    /**
     * Notify all members of the task's project (minus the actor) that a new PR
     * has been opened and linked to the task. Honors recipients' notifyTeamActivity.
     */
    public void notifyPrOpened(PullRequest pr, Task task, User actor) {
        if (!isEnabled() || pr == null || task == null || task.getProject() == null) return;
        Set<User> recipients = projectMembersExcept(task, actor);
        if (recipients.isEmpty()) return;

        String actorName = actor != null ? actor.getFullName() : "Someone";
        String title = actorName + " opened PR #" + pr.getPrNumber() + " on " + task.getTaskKey();
        String body = pr.getTitle();
        Map<String, String> data = baseTaskPayload("pr_opened", task);
        data.put("prId", String.valueOf(pr.getId()));
        data.put("prNumber", String.valueOf(pr.getPrNumber()));

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyTeamActivity())) continue;
            sendToUserAfterCommit(r.getId(), title, body, data);
        }
    }

    /**
     * Notify all members of the task's project (minus the actor) that a PR has been merged.
     * Honors recipients' notifyTeamActivity.
     */
    public void notifyPrMerged(PullRequest pr, Task task, User actor) {
        if (!isEnabled() || pr == null || task == null || task.getProject() == null) return;
        Set<User> recipients = projectMembersExcept(task, actor);
        if (recipients.isEmpty()) return;

        String actorName = actor != null ? actor.getFullName() : "Someone";
        String title = actorName + " merged PR #" + pr.getPrNumber();
        String body = pr.getTitle();
        Map<String, String> data = baseTaskPayload("pr_merged", task);
        data.put("prId", String.valueOf(pr.getId()));
        data.put("prNumber", String.valueOf(pr.getPrNumber()));

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyTeamActivity())) continue;
            sendToUserAfterCommit(r.getId(), title, body, data);
        }
    }

    /**
     * Notify all members of the task's project (minus the actor) that a task transitioned to DONE.
     * Honors recipients' notifyTeamActivity.
     */
    public void notifyTaskDone(Task task, User actor) {
        if (!isEnabled() || task == null || task.getProject() == null) return;
        Set<User> recipients = projectMembersExcept(task, actor);
        if (recipients.isEmpty()) return;

        String actorName = actor != null ? actor.getFullName() : "Someone";
        String title = actorName + " completed " + task.getTaskKey();
        String body = task.getName();
        Map<String, String> data = baseTaskPayload("task_done", task);

        for (User r : recipients) {
            if (!Boolean.TRUE.equals(r.getNotifyTeamActivity())) continue;
            sendToUserAfterCommit(r.getId(), title, body, data);
        }
    }

    // -- Low-level send API -------------------------------------------------

    public BatchResponse sendToUser(String userId,
                                    String title,
                                    String body,
                                    Map<String, String> data) {
        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.debug("FCM disabled — skipping sendToUser({})", userId);
            return null;
        }

        User user = userService.get(userId);
        List<UserPushToken> tokens = userPushTokenService.findByUser(user);
        if (tokens.isEmpty()) {
            log.debug("User {} has no registered push tokens", userId);
            return null;
        }

        List<String> tokenStrings = tokens.stream().map(UserPushToken::getToken).toList();
        MulticastMessage message = buildMulticastMessage(tokenStrings, title, body, data);

        try {
            BatchResponse response = messaging.sendEachForMulticast(message);
            handleStaleTokens(tokenStrings, response);
            log.info("FCM multicast for user {}: success={}, failure={}",
                    userId, response.getSuccessCount(), response.getFailureCount());
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public String sendToToken(String token,
                              String title,
                              String body,
                              Map<String, String> data) {
        FirebaseMessaging messaging = firebaseMessagingProvider.getIfAvailable();
        if (messaging == null) {
            log.debug("FCM disabled — skipping sendToToken");
            return null;
        }

        Message message = buildMessage(token, title, body, data);

        try {
            return messaging.send(message);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                userPushTokenService.deleteStaleTokens(Collections.singletonList(token));
            }
            log.error("FCM send failed (code={}): {}", code, e.getMessage());
            return null;
        }
    }

    /**
     * Schedule a push send to fire after the current transaction commits, so we never
     * deliver a notification for work that ends up rolled back. Falls back to immediate
     * send when called outside a transaction.
     */
    public void sendToUserAfterCommit(String userId,
                                      String title,
                                      String body,
                                      Map<String, String> data) {
        if (!isEnabled()) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        sendToUser(userId, title, body, data);
                    } catch (RuntimeException ex) {
                        log.warn("FCM after-commit send failed for user {}: {}", userId, ex.getMessage());
                    }
                }
            });
        } else {
            sendToUser(userId, title, body, data);
        }
    }

    // -- Internal helpers ---------------------------------------------------

    private Set<User> pointsReviewRecipients(PointsReviewConversation conversation, User actor) {
        Set<User> recipients = new LinkedHashSet<>();
        if (conversation.getInitiator() != null) {
            recipients.add(conversation.getInitiator());
        }
        if (conversation.getParticipants() != null) {
            recipients.addAll(conversation.getParticipants());
        }
        // Course owner (the professor) is implicitly part of every conversation.
        Task task = conversation.getTask();
        if (task != null && task.getProject() != null && task.getProject().getCourse() != null) {
            User professor = task.getProject().getCourse().getOwner();
            if (professor != null) {
                recipients.add(professor);
            }
        }
        if (actor != null) {
            recipients.removeIf(u -> u != null && u.getId().equals(actor.getId()));
        }
        recipients.removeIf(u -> u == null || u.getId() == null);
        return recipients;
    }

    private Set<User> projectMembersExcept(Task task, User actor) {
        Collection<User> members = task.getProject().getMembers();
        if (members == null || members.isEmpty()) return Collections.emptySet();
        Set<User> result = new HashSet<>(members);
        if (actor != null) {
            result.removeIf(u -> u != null && u.getId().equals(actor.getId()));
        }
        result.removeIf(u -> u == null || u.getId() == null);
        return result;
    }

    private Map<String, String> baseTaskPayload(String type, Task task) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        if (task != null) {
            data.put("taskId", String.valueOf(task.getId()));
            if (task.getTaskKey() != null) data.put("taskKey", task.getTaskKey());
            if (task.getProject() != null && task.getProject().getId() != null) {
                data.put("projectId", String.valueOf(task.getProject().getId()));
            }
        }
        return data;
    }

    private static String preview(String html) {
        if (html == null) return "";
        String stripped = html.replaceAll("<[^>]*>", "").trim();
        if (stripped.length() <= BODY_PREVIEW_MAX) return stripped;
        return stripped.substring(0, BODY_PREVIEW_MAX - 1) + "…";
    }

    private MulticastMessage buildMulticastMessage(List<String> tokens,
                                                   String title,
                                                   String body,
                                                   Map<String, String> data) {
        MulticastMessage.Builder builder = MulticastMessage.builder().addAllTokens(tokens);
        if (title != null || body != null) {
            builder.setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());
        }
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }
        return builder.build();
    }

    private Message buildMessage(String token,
                                 String title,
                                 String body,
                                 Map<String, String> data) {
        Message.Builder builder = Message.builder().setToken(token);
        if (title != null || body != null) {
            builder.setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build());
        }
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }
        return builder.build();
    }

    private void handleStaleTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        List<String> stale = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse r = responses.get(i);
            if (r.isSuccessful()) {
                continue;
            }
            FirebaseMessagingException ex = r.getException();
            MessagingErrorCode code = ex != null ? ex.getMessagingErrorCode() : null;
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                stale.add(tokens.get(i));
            } else {
                log.warn("FCM send failed (code={}): {}", code, ex != null ? ex.getMessage() : "unknown");
            }
        }
        if (!stale.isEmpty()) {
            userPushTokenService.deleteStaleTokens(stale);
        }
    }
}
