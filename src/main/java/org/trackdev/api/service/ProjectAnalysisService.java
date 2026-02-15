package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.dto.PRFileDetailDTO;
import org.trackdev.api.dto.ProjectAnalysisDTO;
import org.trackdev.api.entity.*;
import org.trackdev.api.mapper.ProjectAnalysisMapper;
import org.trackdev.api.repository.ProjectAnalysisFileLineRepository;
import org.trackdev.api.repository.ProjectAnalysisFileRepository;
import org.trackdev.api.repository.ProjectAnalysisRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ProjectAnalysisService.class);

    @Autowired
    private ProjectAnalysisRepository analysisRepository;

    @Autowired
    private ProjectAnalysisFileRepository fileRepository;

    @Autowired
    private ProjectAnalysisFileLineRepository lineRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private AccessChecker accessChecker;

    @Autowired
    private ProjectAnalysisMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Start a new project analysis. Only professors/admins who can manage the course can do this.
     */
    @Transactional
    public ProjectAnalysisDTO startAnalysis(Long projectId, User currentUser) {
        // Check permissions - only course managers (professors/admins) can run analysis
        Project project = projectService.get(projectId);
        accessChecker.checkCanManageCourse(project.getCourse(), currentUser.getId());

        // Check if there's already an in-progress analysis
        if (analysisRepository.existsInProgressByProjectId(projectId)) {
            throw new ServiceException("An analysis is already in progress for this project");
        }

        // Create new analysis
        ProjectAnalysis analysis = new ProjectAnalysis(project, currentUser);
        analysis = analysisRepository.save(analysis);

        // Schedule async processing AFTER transaction commits
        // This is important because @Async within the same class doesn't work due to Spring proxy limitations
        final String analysisId = analysis.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Get the bean from ApplicationContext to ensure @Async works through the proxy
                ProjectAnalysisService self = applicationContext.getBean(ProjectAnalysisService.class);
                self.runAnalysisAsync(analysisId);
            }
        });

        return mapper.toDTO(analysis);
    }

    /**
     * Get analysis status
     */
    public ProjectAnalysisDTO getAnalysisStatus(String analysisId, User currentUser) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));
        
        // Check permissions
        accessChecker.checkCanViewProject(analysis.getProject(), currentUser.getId());
        
        return mapper.toDTO(analysis);
    }

    /**
     * Get the latest analysis for a project
     */
    public ProjectAnalysisDTO getLatestAnalysis(Long projectId, User currentUser) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, currentUser.getId());

        return analysisRepository.findFirstByProjectIdOrderByStartedAtDesc(projectId)
                .map(mapper::toDTO)
                .orElse(null);
    }

    /**
     * Get all analyses for a project
     */
    public List<ProjectAnalysisDTO> getProjectAnalyses(Long projectId, User currentUser) {
        Project project = projectService.get(projectId);
        accessChecker.checkCanViewProject(project, currentUser.getId());

        return mapper.toDTOList(analysisRepository.findByProjectIdOrderByStartedAtDesc(projectId));
    }

    /**
     * Get precomputed file details for a specific PR in an analysis.
     * Returns the same structure as PullRequestService.getFileDetails() but from stored data.
     */
    @Transactional(readOnly = true)
    public List<PRFileDetailDTO> getPrecomputedFileDetails(String analysisId, String prId, User currentUser) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));
        
        accessChecker.checkCanViewProject(analysis.getProject(), currentUser.getId());

        // Get all files for this PR in the analysis
        List<ProjectAnalysisFile> files = fileRepository.findByAnalysisIdAndPullRequestId(analysisId, prId);
        
        if (files.isEmpty()) {
            throw new ServiceException("No file data found for this PR in the analysis");
        }

        // Build DTOs with line details
        // Cache for GitHub username to fullName lookups to avoid repeated database queries
        Map<String, String> githubUsernameToFullName = new HashMap<>();
        
        List<PRFileDetailDTO> result = new ArrayList<>();
        for (ProjectAnalysisFile file : files) {
            PRFileDetailDTO dto = new PRFileDetailDTO();
            dto.setFilePath(file.getFilePath());
            dto.setStatus(file.getStatus());
            dto.setAdditions(file.getAdditions());
            dto.setDeletions(file.getDeletions());
            dto.setSurvivingLines(file.getSurvivingLines());
            dto.setDeletedLines(file.getDeletedLines());
            dto.setCurrentLines(file.getCurrentLines());

            // Get lines for this file
            List<ProjectAnalysisFileLine> lines = lineRepository.findByFileIdOrderByDisplayOrderAsc(file.getId());
            List<PRFileDetailDTO.LineDetailDTO> lineDTOs = new ArrayList<>();
            for (ProjectAnalysisFileLine line : lines) {
                PRFileDetailDTO.LineDetailDTO lineDTO = new PRFileDetailDTO.LineDetailDTO();
                lineDTO.setLineNumber(line.getLineNumber());
                lineDTO.setOriginalLineNumber(line.getOriginalLineNumber());
                lineDTO.setContent(line.getContent());
                if (line.getStatus() != null) {
                    lineDTO.setStatus(PRFileDetailDTO.LineStatus.valueOf(line.getStatus()));
                }
                lineDTO.setCommitSha(line.getCommitSha());
                lineDTO.setCommitUrl(line.getCommitUrl());
                
                // Dynamically lookup fullName from GitHub username to get current user data
                String githubUsername = line.getAuthorGithubUsername();
                if (githubUsername != null) {
                    String fullName = githubUsernameToFullName.computeIfAbsent(githubUsername, login -> {
                        User user = userService.findByGithubUsernameOrUsername(login);
                        return user != null ? user.getFullName() : null;
                    });
                    lineDTO.setAuthorFullName(fullName);
                } else {
                    lineDTO.setAuthorFullName(line.getAuthorFullName());
                }
                lineDTO.setAuthorGithubUsername(githubUsername);
                
                lineDTO.setPrFileUrl(line.getPrFileUrl());
                lineDTO.setOriginPrNumber(line.getOriginPrNumber());
                lineDTO.setOriginPrUrl(line.getOriginPrUrl());
                lineDTOs.add(lineDTO);
            }
            dto.setLines(lineDTOs);
            result.add(dto);
        }

        return result;
    }

    /**
     * Get analysis results with optional filters
     */
    @Transactional(readOnly = true)
    public ProjectAnalysisDTO.ResultsDTO getAnalysisResults(String analysisId, Long sprintId, 
            String authorId, User currentUser) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));
        
        accessChecker.checkCanViewProject(analysis.getProject(), currentUser.getId());

        if (analysis.getStatus() != ProjectAnalysis.AnalysisStatus.DONE) {
            throw new ServiceException("Analysis is not complete yet");
        }

        ProjectAnalysisDTO.ResultsDTO results = new ProjectAnalysisDTO.ResultsDTO();
        results.setAnalysis(mapper.toDTO(analysis));

        // Get files with filters
        List<ProjectAnalysisFile> files;
        if (sprintId != null && authorId != null) {
            files = fileRepository.findByAnalysisIdAndSprintIdAndAuthorId(analysisId, sprintId, authorId);
        } else if (sprintId != null) {
            files = fileRepository.findByAnalysisIdAndSprintId(analysisId, sprintId);
        } else if (authorId != null) {
            files = fileRepository.findByAnalysisIdAndAuthorId(analysisId, authorId);
        } else {
            files = fileRepository.findByAnalysisId(analysisId);
        }
        results.setFiles(mapper.toFileDTOList(files));

        // Get summaries
        results.setAuthorSummaries(getAuthorSummaries(analysisId, sprintId));
        results.setSprintSummaries(getSprintSummaries(analysisId));

        return results;
    }

    /**
     * Get author summaries for an analysis
     */
    private List<ProjectAnalysisDTO.AuthorSummaryDTO> getAuthorSummaries(String analysisId, Long sprintId) {
        List<Object[]> rawData = sprintId != null 
                ? fileRepository.getSummaryByAuthorAndSprint(analysisId, sprintId)
                : fileRepository.getSummaryByAuthor(analysisId);

        return rawData.stream().map(row -> {
            ProjectAnalysisDTO.AuthorSummaryDTO dto = new ProjectAnalysisDTO.AuthorSummaryDTO();
            dto.setAuthorId((String) row[0]);
            dto.setAuthorName((String) row[1]);
            dto.setAuthorUsername((String) row[2]);
            dto.setSurvivingLines(((Number) row[3]).intValue());
            dto.setDeletedLines(((Number) row[4]).intValue());
            dto.setFileCount(((Number) row[5]).intValue());
            int total = dto.getSurvivingLines() + dto.getDeletedLines();
            dto.setSurvivalRate(total > 0 ? (dto.getSurvivingLines() * 100.0 / total) : 100.0);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Get sprint summaries for an analysis
     */
    private List<ProjectAnalysisDTO.SprintSummaryDTO> getSprintSummaries(String analysisId) {
        List<Object[]> rawData = fileRepository.getSummaryBySprint(analysisId);

        return rawData.stream().map(row -> {
            ProjectAnalysisDTO.SprintSummaryDTO dto = new ProjectAnalysisDTO.SprintSummaryDTO();
            dto.setSprintId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setSprintName((String) row[1]);
            dto.setSurvivingLines(((Number) row[2]).intValue());
            dto.setDeletedLines(((Number) row[3]).intValue());
            dto.setFileCount(((Number) row[4]).intValue());
            int total = dto.getSurvivingLines() + dto.getDeletedLines();
            dto.setSurvivalRate(total > 0 ? (dto.getSurvivingLines() * 100.0 / total) : 100.0);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Run analysis asynchronously
     */
    @Async
    public void runAnalysisAsync(String analysisId) {
        try {
            // Get the bean from ApplicationContext to ensure @Transactional works through the proxy
            ProjectAnalysisService self = applicationContext.getBean(ProjectAnalysisService.class);
            self.doRunAnalysis(analysisId);
        } catch (Exception e) {
            log.error("Error running analysis {}: {}", analysisId, e.getMessage(), e);
            try {
                markAnalysisFailed(analysisId, e.getMessage());
            } catch (Exception ex) {
                log.error("Error marking analysis as failed: {}", ex.getMessage());
            }
        }
    }

    /**
     * Main analysis orchestration - NOT transactional so each step commits separately
     */
    public void doRunAnalysis(String analysisId) {
        ProjectAnalysisService self = applicationContext.getBean(ProjectAnalysisService.class);
        
        try {
            // Phase 1: Collect PRs and set total count (separate transaction)
            List<PrTaskPair> prsToProcess = self.initializeAnalysis(analysisId);
            
            if (prsToProcess.isEmpty()) {
                log.info("No merged PRs found to analyze");
                self.completeAnalysis(analysisId, 0, 0, 0);
                return;
            }

            int totalFiles = 0;
            int totalSurviving = 0;
            int totalDeleted = 0;

            // Phase 2: Process each PR (each in its own transaction)
            for (PrTaskPair pair : prsToProcess) {
                try {
                    ProcessingResult result = self.processPullRequest(analysisId, pair.prId, pair.taskId);
                    totalFiles += result.fileCount;
                    totalSurviving += result.survivingLines;
                    totalDeleted += result.deletedLines;
                } catch (Exception e) {
                    log.warn("Error analyzing PR {}: {}", pair.prId, e.getMessage());
                }
                // Increment processed count (separate transaction)
                self.incrementProcessedPrs(analysisId);
            }

            // Phase 3: Complete the analysis (separate transaction)
            self.completeAnalysis(analysisId, totalFiles, totalSurviving, totalDeleted);

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage(), e);
            self.markAnalysisFailed(analysisId, e.getMessage());
        }
    }

    /**
     * Helper class to pass PR/Task IDs between transactions
     */
    private static class PrTaskPair {
        final String prId;
        final Long taskId;
        PrTaskPair(String prId, Long taskId) {
            this.prId = prId;
            this.taskId = taskId;
        }
    }

    /**
     * Helper class for processing results
     */
    private static class ProcessingResult {
        final int fileCount;
        final int survivingLines;
        final int deletedLines;
        ProcessingResult(int fileCount, int survivingLines, int deletedLines) {
            this.fileCount = fileCount;
            this.survivingLines = survivingLines;
            this.deletedLines = deletedLines;
        }
    }

    /**
     * Initialize analysis: collect PRs and set total count
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<PrTaskPair> initializeAnalysis(String analysisId) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));

        Project project = analysis.getProject();
        log.info("Starting analysis for project: {}", project.getName());

        // Get all DONE tasks in the project
        List<Task> doneTasks = taskService.findByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
        log.info("Found {} DONE tasks", doneTasks.size());

        // Collect unique PRs from tasks
        List<PrTaskPair> prsToProcess = new ArrayList<>();
        Set<String> seenPrIds = new HashSet<>();
        
        for (Task task : doneTasks) {
            for (PullRequest pr : task.getPullRequests()) {
                if (pr.getMerged() != null && pr.getMerged() && !seenPrIds.contains(pr.getId())) {
                    seenPrIds.add(pr.getId());
                    prsToProcess.add(new PrTaskPair(pr.getId(), task.getId()));
                }
            }
        }

        // Set total PRs count - this commits immediately due to REQUIRES_NEW
        analysis.setTotalPrs(prsToProcess.size());
        analysisRepository.save(analysis);

        log.info("Found {} unique merged PRs to analyze", prsToProcess.size());
        return prsToProcess;
    }

    /**
     * Process a single PR and save its file results
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessingResult processPullRequest(String analysisId, String prId, Long taskId) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));

        PullRequest pr = pullRequestService.get(prId);
        Task task = taskService.get(taskId);
        
        log.info("Analyzing PR #{}: {}", pr.getPrNumber(), pr.getTitle());

        // Get file details using existing PR analysis logic
        List<PRFileDetailDTO> fileDetails = pullRequestService.getFileDetails(pr.getId());

        Sprint sprint = getTaskSprint(task);
        User author = pr.getAuthor();

        int fileCount = 0;
        int survivingLines = 0;
        int deletedLines = 0;

        // Store file results
        for (PRFileDetailDTO fileDetail : fileDetails) {
            ProjectAnalysisFile analysisFile = new ProjectAnalysisFile(analysis, pr, fileDetail.getFilePath());
            analysisFile.setTask(task);
            analysisFile.setSprint(sprint);
            analysisFile.setAuthor(author);
            analysisFile.setStatus(fileDetail.getStatus());
            analysisFile.setAdditions(fileDetail.getAdditions());
            analysisFile.setDeletions(fileDetail.getDeletions());
            analysisFile.setSurvivingLines(fileDetail.getSurvivingLines());
            analysisFile.setDeletedLines(fileDetail.getDeletedLines());
            analysisFile.setCurrentLines(fileDetail.getCurrentLines());

            fileRepository.save(analysisFile);

            // Save line details for precomputed analysis
            if (fileDetail.getLines() != null) {
                int displayOrder = 0;
                for (PRFileDetailDTO.LineDetailDTO line : fileDetail.getLines()) {
                    ProjectAnalysisFileLine fileLine = new ProjectAnalysisFileLine(analysisFile, displayOrder++);
                    fileLine.setLineNumber(line.getLineNumber());
                    fileLine.setOriginalLineNumber(line.getOriginalLineNumber());
                    fileLine.setContent(line.getContent());
                    fileLine.setStatus(line.getStatus() != null ? line.getStatus().name() : null);
                    fileLine.setCommitSha(line.getCommitSha());
                    fileLine.setCommitUrl(line.getCommitUrl());
                    fileLine.setAuthorFullName(line.getAuthorFullName());
                    fileLine.setAuthorGithubUsername(line.getAuthorGithubUsername());
                    fileLine.setPrFileUrl(line.getPrFileUrl());
                    fileLine.setOriginPrNumber(line.getOriginPrNumber());
                    fileLine.setOriginPrUrl(line.getOriginPrUrl());
                    lineRepository.save(fileLine);
                }
            }

            fileCount++;
            survivingLines += fileDetail.getSurvivingLines() != null ? fileDetail.getSurvivingLines() : 0;
            deletedLines += fileDetail.getDeletedLines() != null ? fileDetail.getDeletedLines() : 0;
        }

        return new ProcessingResult(fileCount, survivingLines, deletedLines);
    }

    /**
     * Increment processed PRs count
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementProcessedPrs(String analysisId) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));
        analysis.incrementProcessedPrs();
        analysisRepository.save(analysis);
    }

    /**
     * Complete the analysis with final totals
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeAnalysis(String analysisId, int totalFiles, int totalSurviving, int totalDeleted) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ServiceException("Analysis not found"));
        
        analysis.setTotalFiles(totalFiles);
        analysis.setTotalSurvivingLines(totalSurviving);
        analysis.setTotalDeletedLines(totalDeleted);
        analysis.complete();
        analysisRepository.save(analysis);

        log.info("Analysis complete: {} files, {} surviving lines, {} deleted lines",
                totalFiles, totalSurviving, totalDeleted);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAnalysisFailed(String analysisId, String errorMessage) {
        ProjectAnalysis analysis = analysisRepository.findById(analysisId).orElse(null);
        if (analysis != null) {
            analysis.fail(errorMessage);
            analysisRepository.save(analysis);
        }
    }

    /**
     * Delete all analysis files for a specific task (used when task is deleted).
     */
    @Transactional
    public void deleteFilesByTask(Task task) {
        fileRepository.deleteByTask(task);
    }

    /**
     * Get the sprint for a task (first sprint if multiple)
     */
    private Sprint getTaskSprint(Task task) {
        if (task == null) return null;
        Collection<Sprint> sprints = task.getActiveSprints();
        if (sprints == null || sprints.isEmpty()) return null;
        return sprints.iterator().next();
    }
}
