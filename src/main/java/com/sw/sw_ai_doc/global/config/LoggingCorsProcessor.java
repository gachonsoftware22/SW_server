package com.sw.sw_ai_doc.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;

import java.io.IOException;

@Slf4j
public class LoggingCorsProcessor extends DefaultCorsProcessor {

    @Override
    public boolean processRequest(@Nullable CorsConfiguration config, HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean allowed = super.processRequest(config, request, response);
        if (!allowed) {
            log.warn("CORS rejected - origin: {}, method: {}, uri: {}",
                    request.getHeader(HttpHeaders.ORIGIN),
                    request.getMethod(),
                    request.getRequestURI());
        }
        return allowed;
    }
}
