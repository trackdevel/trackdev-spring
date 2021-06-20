package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.Views;
import org.udg.trackdev.spring.service.UserService;


import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;

// This class is used to manage users and sign up
@RequestMapping(path = "/users")
@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    /**
     *  Returns the public profile of ANY user.
     * @param principal The current authenticated entity
     * @param id The id of the user to request.
     * @return The User identified by id
     */
    @GetMapping(path = "/{id}")
    @JsonView(Views.Public.class)
    public User getPublic(Principal principal, @PathVariable("id") String id) {
        super.checkLoggedIn(principal);
        return userService.get(id);
    }

//  @DeleteMapping(path="/{id}")
//  public String deleteT(HttpSession session, @PathVariable("id") Long TId) {
//
//    Long loggedTId = getLoggedT(session);
//
//    if (!loggedTId.equals(TId))
//      throw new ControllerException("You cannot delete other Ts!");
//
//    TService.crud().deleteById(TId);
//    session.removeAttribute("simpleapp_auth_id");
//
//    return BaseController.OK_MESSAGE;
//  }

    @PostMapping(path = "/register")
    public ResponseEntity register(Principal principal, @Valid @RequestBody RegisterT ru) {
        checkNotLoggedIn(principal);
        userService.register(ru.username, ru.email, ru.password);
        return okNoContent();
    }

    static class RegisterT {
        @NotBlank
        @Size(max = User.USERNAME_LENGTH)
        public String username;

        @NotBlank
        @Email
        @Size(max = User.EMAIL_LENGTH)
        public String email;

        @NotBlank
        public String password;
    }

    static class UpdateT {
        public String username;
        public String email;
        public String password;
        public String name;
        public String address;
        public double saldo;
    }

}
