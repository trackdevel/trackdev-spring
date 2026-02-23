package org.trackdev.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.trackdev.api.model.ErrorEntity;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle
            (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException, ServletException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormattingConfiguration.SIMPLE_DATE_FORMAT);
        ErrorEntity errorEntityResponse = new ErrorEntity(dateFormat.format(new Date()), HttpStatus.FORBIDDEN.value(), "Security error", ex.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(new ObjectMapper().writeValueAsString(errorEntityResponse));
        out.flush();
    }
}