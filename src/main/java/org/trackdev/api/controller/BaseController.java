package org.trackdev.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.utils.ErrorConstants;

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
      throw new ControllerException(ErrorConstants.USER_SHOULD_NOT_LOGEDIN);
  }

  void checkLoggedIn(Principal principal) {
    if(principal == null)
      throw new ControllerException(ErrorConstants.USER_NOT_LOGGED_IN);
  }

  ResponseEntity<Void> okNoContent() {
    return ResponseEntity.noContent().build();
  }

}
