package org.udg.trackdev.spring.entity.views;

// Views for use by Json serialization
// to solve problems of recursion in entities with bidirectional relationships
public class EntityLevelViews {
  static public class Hierarchy { }
  static public class Basic { }
  static public class SubjectComplete extends Basic { }
  static public class CourseComplete extends Basic { }
  static public class ProjectComplete extends Basic { }
  static public class TaskComplete extends Basic { }
}