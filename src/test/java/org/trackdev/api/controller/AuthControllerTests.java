package org.trackdev.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.trackdev.api.controller.AuthController;
import org.trackdev.api.controller.exceptions.RestResponseEntityExceptionHandler;
import org.trackdev.api.utils.ErrorConstants;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTests {

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new AuthController())
                .setControllerAdvice(new RestResponseEntityExceptionHandler())
                .build();
    }

    @Test
    void selfEndpoint_whenNoPrincipal_shouldThrowException() throws Exception {
        this.mockMvc.perform(get("/auth/self"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorConstants.USER_NOT_LOGGED_IN));
    }
}
