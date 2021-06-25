package org.udg.trackdev.spring.entity.views;

// Views for use by Json serialization
// to define properties to be viewed only for current user (private)
// or for all users (public)
public class PrivacyLevelViews {
  static public class Public { }
  static public class Private extends Public { }
}