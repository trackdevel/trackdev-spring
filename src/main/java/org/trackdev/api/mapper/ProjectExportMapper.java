package org.trackdev.api.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trackdev.api.dto.PullRequestDTO;
import org.trackdev.api.dto.StudentAttributeValueDTO;
import org.trackdev.api.dto.TaskAttributeValueDTO;
import org.trackdev.api.dto.TaskBasicDTO;
import org.trackdev.api.dto.UserSummaryDTO;
import org.trackdev.api.dto.export.PullRequestExportDTO;
import org.trackdev.api.dto.export.TaskExportDTO;
import org.trackdev.api.dto.export.TeamMemberExportDTO;
import org.trackdev.api.entity.PullRequest;
import org.trackdev.api.entity.PullRequestAttributeValue;
import org.trackdev.api.entity.Role;
import org.trackdev.api.entity.StudentAttributeValue;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskAttributeValue;
import org.trackdev.api.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectExportMapper {

    @Autowired
    UserMapper userMapper;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    PullRequestMapper pullRequestMapper;

    @Autowired
    TaskAttributeValueMapper taskAttributeValueMapper;

    @Autowired
    PullRequestAttributeValueMapper pullRequestAttributeValueMapper;

    @Autowired
    StudentAttributeValueMapper studentAttributeValueMapper;

    public TeamMemberExportDTO toTeamMemberExportDTO(User user, List<StudentAttributeValue> attrs) {
        TeamMemberExportDTO dto = new TeamMemberExportDTO();
        UserSummaryDTO summary = userMapper.toSummaryDTO(user);
        dto.setUser(summary);
        Set<Role> roles = user.getRoles();
        dto.setRoles(roles == null ? Collections.emptySet()
                : roles.stream().map(r -> r.getUserType().name()).collect(Collectors.toSet()));
        dto.setAttributeValues(attrs == null ? Collections.emptyList()
                : studentAttributeValueMapper.toDTOList(attrs));
        return dto;
    }

    public TaskExportDTO toTaskExportDTO(Task task, List<TaskAttributeValue> attrs) {
        TaskExportDTO dto = new TaskExportDTO();
        TaskBasicDTO basic = taskMapper.toBasicDTO(task);
        dto.setTask(basic);
        dto.setAttributeValues(attrs == null ? Collections.emptyList()
                : taskAttributeValueMapper.toDTOList(attrs));
        return dto;
    }

    public PullRequestExportDTO toPullRequestExportDTO(PullRequest pr, List<PullRequestAttributeValue> attrs) {
        PullRequestExportDTO dto = new PullRequestExportDTO();
        PullRequestDTO prDto = pullRequestMapper.toDTO(pr);
        dto.setPullRequest(prDto);
        List<PullRequestExportDTO.TaskRef> taskRefs = pr.getTasks() == null ? Collections.emptyList()
                : pr.getTasks().stream().map(t -> {
                    PullRequestExportDTO.TaskRef ref = new PullRequestExportDTO.TaskRef();
                    ref.setId(t.getId());
                    ref.setTaskKey(t.getTaskKey());
                    return ref;
                }).collect(Collectors.toList());
        dto.setTasks(taskRefs);
        dto.setAttributeValues(attrs == null ? Collections.emptyList()
                : pullRequestAttributeValueMapper.toDTOList(attrs));
        return dto;
    }
}
