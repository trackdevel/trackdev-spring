package org.trackdev.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.CourseRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseService.
 * Tests that course retrieval methods properly fetch associated data.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SubjectService subjectService;

    @Mock
    private UserService userService;

    @Mock
    private AccessChecker accessChecker;

    private CourseService courseService;

    private Course testCourse;
    private Subject testSubject;
    private User testOwner;

    @BeforeEach
    void setUp() {
        // Create service and inject mocks
        courseService = new CourseService();
        ReflectionTestUtils.setField(courseService, "repo", courseRepository);
        ReflectionTestUtils.setField(courseService, "subjectService", subjectService);
        ReflectionTestUtils.setField(courseService, "userService", userService);
        ReflectionTestUtils.setField(courseService, "accessChecker", accessChecker);
        
        // Create test owner
        testOwner = new User();
        
        // Create test subject
        testSubject = new Subject("Software Engineering", "SE", testOwner);
        
        // Create test course with projects
        testCourse = new Course(2025);
        testCourse.setSubject(testSubject);
        
        // Create and add projects to the course
        Project project1 = new Project("Project Alpha");
        project1.setCourse(testCourse);
        
        Project project2 = new Project("Project Beta");
        project2.setCourse(testCourse);
        
        Project project3 = new Project("Project Gamma");
        project3.setCourse(testCourse);
        
        // Initialize projects collection and add projects
        Set<Project> projects = new HashSet<>();
        projects.add(project1);
        projects.add(project2);
        projects.add(project3);
        
        // Use reflection to set the projects collection since there's no setter
        ReflectionTestUtils.setField(testCourse, "projects", projects);
    }

    @Test
    void getAll_shouldReturnCoursesWithProjects() {
        // Given
        List<Course> courses = List.of(testCourse);
        when(courseRepository.findAllWithProjectsAndStudents()).thenReturn(courses);

        // When
        Collection<Course> result = courseService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Course returnedCourse = result.iterator().next();
        assertNotNull(returnedCourse.getProjects());
        assertEquals(3, returnedCourse.getProjects().size());
        
        // Verify the correct repository method was called
        verify(courseRepository, times(1)).findAllWithProjectsAndStudents();
        verify(courseRepository, never()).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoCourses() {
        // Given
        when(courseRepository.findAllWithProjectsAndStudents()).thenReturn(List.of());

        // When
        Collection<Course> result = courseService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(courseRepository, times(1)).findAllWithProjectsAndStudents();
    }

    @Test
    void getCoursesForUser_shouldReturnCoursesWithProjects() {
        // Given
        String userId = "user-123";
        List<Course> courses = new ArrayList<>();
        courses.add(testCourse);
        when(courseRepository.findByOwnerIdOrSubjectOwnerId(userId)).thenReturn(courses);

        // When
        Collection<Course> result = courseService.getCoursesForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Course returnedCourse = result.iterator().next();
        assertNotNull(returnedCourse.getProjects());
        assertEquals(3, returnedCourse.getProjects().size());
        
        verify(courseRepository, times(1)).findByOwnerIdOrSubjectOwnerId(userId);
    }

    @Test
    void getCoursesForUser_withMultipleCourses_shouldReturnAllWithProjectCounts() {
        // Given
        String userId = "user-123";
        
        // Create second course with 2 projects
        Course course2 = new Course(2024);
        course2.setSubject(testSubject);
        
        Set<Project> projects2 = new HashSet<>();
        projects2.add(new Project("Project X"));
        projects2.add(new Project("Project Y"));
        
        ReflectionTestUtils.setField(course2, "projects", projects2);

        List<Course> courses = List.of(testCourse, course2);
        when(courseRepository.findByOwnerIdOrSubjectOwnerId(userId)).thenReturn(courses);

        // When
        Collection<Course> result = courseService.getCoursesForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify each course has the expected project count
        int totalProjects = result.stream()
                .mapToInt(c -> c.getProjects().size())
                .sum();
        assertEquals(5, totalProjects); // 3 from testCourse + 2 from course2
    }

    @Test
    void courseProjects_shouldNotBeNull() {
        // Given
        when(courseRepository.findAllWithProjectsAndStudents()).thenReturn(List.of(testCourse));

        // When
        Collection<Course> result = courseService.getAll();

        // Then
        for (Course course : result) {
            assertNotNull(course.getProjects(), 
                "Course projects should not be null - this would cause projectCount to be 0 in the DTO");
        }
    }
}
