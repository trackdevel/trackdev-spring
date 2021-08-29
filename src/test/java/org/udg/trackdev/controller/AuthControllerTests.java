package org.udg.trackdev.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.udg.trackdev.spring.controller.AuthController;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class AuthControllerTests {

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new AuthController()).build();
    }

    @Test
    void selfEndpoint_whenNoPrincipal_shouldThrowException() throws Exception {
        this.mockMvc.perform(get("/auth/self"))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ControllerException))
                .andExpect(result -> assertEquals(result.getResolvedException().getMessage(), "User should be authenticated"));
    }
}
