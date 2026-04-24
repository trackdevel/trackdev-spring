package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.dto.export.PullRequestExportDTO;
import org.trackdev.api.dto.export.PullRequestsExportDTO;
import org.trackdev.api.dto.export.TaskExportDTO;
import org.trackdev.api.dto.export.TasksExportDTO;
import org.trackdev.api.dto.export.TeamExportDTO;
import org.trackdev.api.dto.export.TeamMemberExportDTO;
import org.trackdev.api.entity.AttributeType;
import org.trackdev.api.entity.Profile;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.PullRequestAttributeValue;
import org.trackdev.api.entity.StudentAttributeValue;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskAttributeValue;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.ProjectExportMapper;
import org.trackdev.api.repository.PullRequestAttributeValueRepository;
import org.trackdev.api.repository.StudentAttributeValueRepository;
import org.trackdev.api.repository.TaskAttributeValueRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectExportService {

    @Autowired
    ProjectService projectService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    StudentAttributeValueRepository studentAttributeValueRepository;

    @Autowired
    TaskAttributeValueRepository taskAttributeValueRepository;

    @Autowired
    PullRequestAttributeValueRepository pullRequestAttributeValueRepository;

    @Autowired
    ProjectExportMapper exportMapper;

    @Transactional(readOnly = true)
    public TeamExportDTO exportTeam(Long projectId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageProject(project, userId);

        Profile profile = project.getCourse() != null ? project.getCourse().getProfile() : null;
        Long profileId = profile != null ? profile.getId() : null;

        Set<User> members = project.getMembers();
        List<TeamMemberExportDTO> memberDTOs = members.stream()
                .map(user -> {
                    List<StudentAttributeValue> attrs = Collections.emptyList();
                    if (profileId != null) {
                        attrs = studentAttributeValueRepository.findByUserId(user.getId()).stream()
                                .filter(v -> v.getAttribute() != null
                                        && profileId.equals(v.getAttribute().getProfileId()))
                                .filter(v -> v.getAttribute().getType() != AttributeType.LIST)
                                .collect(Collectors.toList());
                    }
                    return exportMapper.toTeamMemberExportDTO(user, attrs);
                })
                .collect(Collectors.toList());

        TeamExportDTO dto = new TeamExportDTO();
        dto.setProjectId(projectId);
        dto.setMembers(memberDTOs);
        return dto;
    }

    @Transactional(readOnly = true)
    public TasksExportDTO exportTasks(Long projectId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageProject(project, userId);

        Collection<Task> tasks = project.getAllTasks();

        List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
        Map<Long, List<TaskAttributeValue>> attrsByTaskId = taskIds.isEmpty()
                ? Collections.emptyMap()
                : taskAttributeValueRepository.findByTaskIdIn(taskIds).stream()
                        .collect(Collectors.groupingBy(v -> v.getTask().getId()));

        List<TaskExportDTO> taskDTOs = tasks.stream()
                .map(t -> exportMapper.toTaskExportDTO(
                        t,
                        attrsByTaskId.getOrDefault(t.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        TasksExportDTO dto = new TasksExportDTO();
        dto.setProjectId(projectId);
        dto.setTasks(taskDTOs);
        return dto;
    }

    @Transactional(readOnly = true)
    public PullRequestsExportDTO exportPullRequests(Long projectId, String userId) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageProject(project, userId);

        // Deduplicate PRs: a PR may link to multiple tasks in the same project.
        Map<String, PullRequest> uniquePRs = new LinkedHashMap<>();
        for (Task task : project.getAllTasks()) {
            if (task.getPullRequests() != null) {
                for (PullRequest pr : task.getPullRequests()) {
                    uniquePRs.putIfAbsent(pr.getId(), pr);
                }
            }
        }

        Set<String> prIds = new HashSet<>(uniquePRs.keySet());
        Map<String, List<PullRequestAttributeValue>> attrsByPrId = prIds.isEmpty()
                ? Collections.emptyMap()
                : pullRequestAttributeValueRepository.findByPullRequestIdIn(prIds).stream()
                        .collect(Collectors.groupingBy(v -> v.getPullRequest().getId()));

        List<PullRequestExportDTO> prDTOs = uniquePRs.values().stream()
                .map(pr -> exportMapper.toPullRequestExportDTO(
                        pr,
                        attrsByPrId.getOrDefault(pr.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        PullRequestsExportDTO dto = new PullRequestsExportDTO();
        dto.setProjectId(projectId);
        dto.setPullRequests(prDTOs);
        return dto;
    }
}
