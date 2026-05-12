package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trackdev.api.dto.PointsReviewActiveConversationDTO;
import org.trackdev.api.entity.PointsReviewConversation;
import org.trackdev.api.mapper.PointsReviewConversationMapper;
import org.trackdev.api.service.PointsReviewConversationService;

import java.security.Principal;
import java.util.List;

/**
 * Cross-project points-review queries. Endpoints here are not scoped to a single
 * task so they cannot live under {@code /tasks/{taskId}/points-reviews}.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "10. Points Review")
@RestController
@RequestMapping(path = "/points-reviews")
public class PointsReviewOverviewController extends BaseController {

    @Autowired
    private PointsReviewConversationService conversationService;

    @Autowired
    private PointsReviewConversationMapper mapper;

    @Operation(
        summary = "List active points review conversations visible to the current user",
        description = "Returns every points review conversation the caller can view, "
            + "with embedded task / project / course context. Intended for the "
            + "dashboard overview, where the client groups by project."
    )
    @GetMapping("/active")
    public List<PointsReviewActiveConversationDTO> listActive(Principal principal) {
        String userId = getUserId(principal);
        List<PointsReviewConversation> conversations =
            conversationService.getActiveConversationsForUser(userId);
        return mapper.toActiveDTOList(conversations);
    }
}
