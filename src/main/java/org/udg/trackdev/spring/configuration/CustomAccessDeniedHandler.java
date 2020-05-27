package org.udg.trackdev.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.service.Global;
import org.udg.trackdev.spring.entity.ErrorEntity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle
            (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {
        ErrorEntity errorEntityResponse = new ErrorEntity(Global.dateFormat.format(new Date()), HttpStatus.FORBIDDEN.value(), "Security error", ex.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(new ObjectMapper().writeValueAsString(errorEntityResponse));
        out.flush();
    }
}