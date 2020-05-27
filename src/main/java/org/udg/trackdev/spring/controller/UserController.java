package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.Views;
import org.udg.trackdev.spring.service.UserService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// This class is used to process all the authentication related URLs
@RequestMapping(path = "/users")
@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;

//    @PostMapping(path = "/logout")
//    @JsonView(Views.Private.class)
//    public String logout(HttpSession session) {
//
//        Long TID = getLoggedUser(session);
//
//        session.removeAttribute("simpleapp_auth_id");
//        return BaseController.OK_MESSAGE;
//    }

//    @GetMapping(path = "/{id}")
//    @JsonView(Views.Public.class)
//    public User getPublicT(HttpSession session, @PathVariable("id") Long TId) {
//
//        Long loggedTId = getLoggedUser(session);
//
//        return userService.getUser(loggedTId);
//    }

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


//    @PostMapping(path = "/register")
//    public String register(HttpSession session, @Valid @RequestBody RegisterT ru) {
//
//        checkNotLoggedIn(session);
//        userService.register(ru.username, ru.email, ru.password);
//        return BaseController.OK_MESSAGE;
//    }

//    @GetMapping(path = "/me")
//    @JsonView(Views.Complete.class)
//    public User getTProfile(HttpSession session) {
//
//        Long loggedTId = getLoggedUser(session);
//
//        return userService.getUser(loggedTId);
//    }

//    @GetMapping(path = "/check")
//    public String checkLoggedIn(HttpSession session) {
//
//        getLoggedUser(session);
//
//        return BaseController.OK_MESSAGE;
//    }

    static class LoginT {
        @NotNull
        public String username;
        @NotNull
        public String password;
    }

    static class RegisterT {
        @NotNull
        public String username;
        @NotNull
        public String email;
        @NotNull
        public String password;
        @NotNull
        public String name;
        @NotNull
        public String address;
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
