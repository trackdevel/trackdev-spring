package org.trackdev.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project extends BaseEntityLong {

   //-- CONSTANTS

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 100;
    public static final int SLUG_LENGTH = 120;
    public static final int MIN_QUALIFICATION = 0;
    public static final int MAX_QUALIFICATION = 10;

    //-- ATTRIBUTES

    @NotNull
    @Column(length = NAME_LENGTH)
    private String name;

    @NotNull
    @Column(length = SLUG_LENGTH, unique = true)
    private String slug;

    @ManyToOne
    private Course course;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "projects_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Sprint> sprints = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<GitHubRepo> githubRepos = new ArrayList<>();

    @Max(10)
    private Double qualification;

    /**
     * Counter for generating unique task numbers within this project.
     * Each new task gets the next number and this is incremented.
     */
    private Integer nextTaskNumber = 1;

    //--- CONSTRUCTOR

    public Project() {}

    public Project(String name) {
        this.name = name;
        // Note: slug should be set by the service after uniqueness check
    }

    //--- GETTERS AND SETTERS

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public String getSlug() { return this.slug; }

    public void setSlug(String slug) { this.slug = slug; }

    public Set<User> getMembers() { return this.members; }

    public Course getCourse() {
        return this.course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Collection<Task> getTasks() {
        Collection<Task> mainTasks = new ArrayList<>();
        this.tasks.stream().filter(task -> task.getParentTask() == null).forEach(mainTasks::add);
        return mainTasks;
    }

    public Collection<Sprint> getSprints() {
        return this.sprints;
    }

    public Double getQualification() { return this.qualification; }
    public void setQualification(Double qualification) { this.qualification = qualification; }

    //--- METHODS

    public void addMember(User member) { this.members.add(member); }

    public boolean isMember(User user) {
        return this.members.contains(user);
    }

    public boolean isMember(String userId) {
        for(User user: this.members) {
            if(user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    /**
     * Adds a task to this project and assigns it a unique task number.
     * The task's taskKey is set to "{projectSlug}-{taskNumber}".
     */
    public void addTask(Task task) {
        int taskNumber = getAndIncrementNextTaskNumber();
        task.setTaskNumber(taskNumber);
        task.setTaskKey(this.slug + "-" + taskNumber);
        task.setProject(this);
        this.tasks.add(task);
    }

    /**
     * Gets the next task number and increments the counter.
     * @return The next available task number for this project
     */
    public synchronized int getAndIncrementNextTaskNumber() {
        if (nextTaskNumber == null) {
            nextTaskNumber = 1;
        }
        int current = nextTaskNumber;
        nextTaskNumber++;
        return current;
    }

    public Integer getNextTaskNumber() {
        return nextTaskNumber;
    }

    public void addSprint(Sprint sprint) {
        this.sprints.add(sprint);
    }

    public Collection<GitHubRepo> getGithubRepos() {
        return this.githubRepos;
    }

    public void addGithubRepo(GitHubRepo repo) {
        this.githubRepos.add(repo);
        repo.setProject(this);
    }

    public void removeGithubRepo(GitHubRepo repo) {
        this.githubRepos.remove(repo);
    }

    //--- STATIC METHODS

    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final java.util.Random RANDOM = new java.util.Random();
    public static final int MIN_SLUG_LENGTH = 3;
    public static final int MAX_SLUG_LENGTH = 5;

    /**
     * Generates slug candidates based on the project name.
     * Returns a list of potential slugs ordered by preference:
     * 1. Acronym from word initials (e.g., "My Project" -> "mp", "mpr")
     * 2. First letter + consonants from name
     * 3. First letter + various characters from name
     * 
     * @param name The project name
     * @param length The desired slug length (3-5 characters)
     * @return List of candidate slugs derived from the name
     */
    public static List<String> generateSlugCandidatesFromName(String name, int length) {
        List<String> candidates = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return candidates;
        }
        
        // Normalize: lowercase and extract only alphanumeric
        String normalized = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (normalized.isEmpty()) {
            return candidates;
        }
        
        char firstChar = normalized.charAt(0);
        
        // Strategy 1: Acronym from words (first letter of each word)
        String[] words = name.toLowerCase().split("[^a-z0-9]+");
        StringBuilder acronym = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                acronym.append(word.charAt(0));
            }
        }
        if (acronym.length() >= length) {
            candidates.add(acronym.substring(0, length));
        } else if (acronym.length() > 0 && acronym.length() < length) {
            // Pad acronym with remaining chars from first word
            String firstWord = words.length > 0 ? words[0] : "";
            String padded = padWithChars(acronym.toString(), firstWord, length);
            if (padded.length() == length) {
                candidates.add(padded);
            }
        }
        
        // Strategy 2: First letter + consonants from rest of name
        String consonants = extractConsonants(normalized.substring(1));
        if (consonants.length() >= length - 1) {
            candidates.add(firstChar + consonants.substring(0, length - 1));
        }
        
        // Strategy 3: First letter + evenly spaced characters from name
        if (normalized.length() >= length) {
            StringBuilder spaced = new StringBuilder();
            spaced.append(firstChar);
            int step = Math.max(1, (normalized.length() - 1) / (length - 1));
            for (int i = 1; i < length && (1 + (i-1) * step) < normalized.length(); i++) {
                spaced.append(normalized.charAt(1 + (i-1) * step));
            }
            // Pad if needed
            while (spaced.length() < length && spaced.length() < normalized.length()) {
                spaced.append(normalized.charAt(spaced.length()));
            }
            if (spaced.length() == length) {
                candidates.add(spaced.toString());
            }
        }
        
        // Strategy 4: First chars of the normalized name
        if (normalized.length() >= length) {
            candidates.add(normalized.substring(0, length));
        }
        
        // Strategy 5: Variations with numbers appended
        String base = normalized.length() >= length - 1 
            ? normalized.substring(0, length - 1) 
            : normalized;
        if (base.length() == length - 1) {
            for (char c = '0'; c <= '9'; c++) {
                candidates.add(base + c);
            }
        }
        
        // Remove duplicates while preserving order
        return candidates.stream().distinct().collect(java.util.stream.Collectors.toList());
    }
    
    private static String extractConsonants(String s) {
        return s.replaceAll("[aeiou0-9]", "");
    }
    
    private static String padWithChars(String base, String source, int targetLength) {
        StringBuilder result = new StringBuilder(base);
        int sourceIndex = 1; // Start after first char (already used)
        while (result.length() < targetLength && sourceIndex < source.length()) {
            char c = source.charAt(sourceIndex);
            if (!base.contains(String.valueOf(c))) {
                result.append(c);
            }
            sourceIndex++;
        }
        // If still not enough, just append remaining chars
        sourceIndex = 1;
        while (result.length() < targetLength && sourceIndex < source.length()) {
            result.append(source.charAt(sourceIndex));
            sourceIndex++;
        }
        return result.toString();
    }

    /**
     * Generates a random alphanumeric slug of the specified length.
     * @param length The length of the slug (3-5 characters)
     * @return A random alphanumeric string
     */
    public static String generateRandomSlug(int length) {
        if (length < MIN_SLUG_LENGTH) length = MIN_SLUG_LENGTH;
        if (length > MAX_SLUG_LENGTH) length = MAX_SLUG_LENGTH;
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * @deprecated Use generateSlugCandidatesFromName() for name-based slugs
     * or generateRandomSlug() for random slugs.
     */
    @Deprecated
    public static String generateSlug(int length) {
        return generateRandomSlug(length);
    }

    /**
     * @deprecated Use the service method generateUniqueSlug(name) instead.
     */
    @Deprecated
    public static String generateSlug() {
        return generateRandomSlug(MIN_SLUG_LENGTH);
    }

    /**
     * @deprecated Use the service method generateUniqueSlug(name) instead.
     */
    @Deprecated
    public static String generateSlug(String name) {
        return generateSlug();
    }

}
