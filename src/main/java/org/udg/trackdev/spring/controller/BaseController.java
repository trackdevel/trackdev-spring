package org.udg.trackdev.spring.controller;

import org.springframework.http.converter.json.MappingJacksonValue;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;
import java.security.Principal;

/**
 * Created by imartin on 21/02/17.
 */
public class BaseController {

  static String OK_MESSAGE = "\"ok\"";

  MappingJacksonValue toResponse(Object pojo, Class<?> view) {
    final MappingJacksonValue result = new MappingJacksonValue(pojo);
    result.setSerializationView(view);
    return result;
  }

  protected String getUserId(Principal principal) {
    checkLoggedIn(principal);
    return principal.getName();
  }

  void checkNotLoggedIn(Principal principal) {
    if(principal != null)
      throw new ControllerException("User should not be authenticated");
  }

  void checkLoggedIn(Principal principal) {
    if(principal == null)
      throw new ControllerException("User should be authenticated");
  }
}
