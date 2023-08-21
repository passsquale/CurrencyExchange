package com.pasquale.currencyExchange.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pasquale.currencyExchange.exception.ErrorMessage;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public final class ExceptionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ExceptionHandler(){}

    public static void handle(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        try (PrintWriter printWriter = resp.getWriter()) {
            printWriter.write(objectMapper.writeValueAsString(new ErrorMessage(message)));
        }
    }
}
