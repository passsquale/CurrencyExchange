package com.pasquale.currencyExchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pasquale.currencyExchange.dto.CurrencyDto;
import com.pasquale.currencyExchange.exception.DaoException;
import com.pasquale.currencyExchange.util.ExceptionHandler;
import com.pasquale.currencyExchange.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if(pathInfo == null || pathInfo.equals("/")) {
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_BAD_REQUEST, "The currency code is missing in the address"
            );
            return;
        }
        try {
            Optional<CurrencyDto> currency = currencyService.findByCode(
                    pathInfo.replace("/", "").toUpperCase()
            );

            if(currency.isPresent()){
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                String json = objectMapper.writeValueAsString(currency.get());
                try (PrintWriter printWriter = resp.getWriter()) {
                    printWriter.write(json);
                }
            }else{
                ExceptionHandler.handle(resp, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
            }


        } catch (DaoException e) {
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "the database is unavailable"
            );
        }

    }
}
