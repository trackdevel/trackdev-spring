package org.trackdev.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.ProjectAnalysisDTO;
import org.trackdev.api.entity.ProjectAnalysis;
import org.trackdev.api.entity.ProjectAnalysisFile;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectAnalysisMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "startedByName", source = "startedBy.fullName")
    @Mapping(target = "startedById", source = "startedBy.id")
    @Mapping(target = "progressPercent", source = ".", qualifiedByName = "calculateProgress")
    @Mapping(target = "survivalRate", source = ".", qualifiedByName = "calculateSurvivalRate")
    ProjectAnalysisDTO toDTO(ProjectAnalysis entity);

    List<ProjectAnalysisDTO> toDTOList(List<ProjectAnalysis> entities);

    @Mapping(target = "prId", source = "pullRequest.id")
    @Mapping(target = "prNumber", source = "pullRequest.prNumber")
    @Mapping(target = "prTitle", source = "pullRequest.title")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskName", source = "task.name")
    @Mapping(target = "sprintId", source = "sprint.id")
    @Mapping(target = "sprintName", source = "sprint.name")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "survivalRate", source = ".", qualifiedByName = "calculateFileSurvivalRate")
    ProjectAnalysisDTO.FileDTO toFileDTO(ProjectAnalysisFile file);

    List<ProjectAnalysisDTO.FileDTO> toFileDTOList(List<ProjectAnalysisFile> files);

    @Named("calculateProgress")
    default Integer calculateProgress(ProjectAnalysis analysis) {
        if (analysis.getTotalPrs() == null || analysis.getTotalPrs() == 0) {
            return 0;
        }
        int processed = analysis.getProcessedPrs() != null ? analysis.getProcessedPrs() : 0;
        return (int) ((processed * 100.0) / analysis.getTotalPrs());
    }

    @Named("calculateSurvivalRate")
    default Double calculateSurvivalRate(ProjectAnalysis analysis) {
        int surviving = analysis.getTotalSurvivingLines() != null ? analysis.getTotalSurvivingLines() : 0;
        int deleted = analysis.getTotalDeletedLines() != null ? analysis.getTotalDeletedLines() : 0;
        int total = surviving + deleted;
        if (total == 0) return 100.0;
        return (surviving * 100.0) / total;
    }

    @Named("calculateFileSurvivalRate")
    default Double calculateFileSurvivalRate(ProjectAnalysisFile file) {
        int surviving = file.getSurvivingLines() != null ? file.getSurvivingLines() : 0;
        int deleted = file.getDeletedLines() != null ? file.getDeletedLines() : 0;
        int total = surviving + deleted;
        if (total == 0) return 100.0;
        return (surviving * 100.0) / total;
    }
}
