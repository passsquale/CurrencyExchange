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
import java.util.List;
import java.util.Optional;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyDto> currencies = currencyService.findAll();
            String json = objectMapper.writeValueAsString(currencies);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            try (PrintWriter printWriter = resp.getWriter()) {
                printWriter.write(json);
            }
        } catch (DaoException e) {
            ExceptionHandler.handle(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "the database is unavailable"
            );
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<String> code = Optional.ofNullable(req.getParameter("code"));
        Optional<String> fullName = Optional.ofNullable(req.getParameter("fullName"));
        Optional<String> sign = Optional.ofNullable(req.getParameter("sign"));
        if(code.isEmpty() || fullName.isEmpty()|| sign.isEmpty()){
            ExceptionHandler.handle(resp, HttpServletResponse.SC_BAD_REQUEST, "The required form field is missing");
            return;
        }
        try {

            if(currencyService.findByCode(code.get()).isPresent()){
                ExceptionHandler.handle(resp, HttpServletResponse.SC_CONFLICT,
                        "A currency with this code already exists");
                return;
            }

            CurrencyDto currencyDto = new CurrencyDto(-1, code.get(), fullName.get(), sign.get());
            CurrencyDto save = currencyService.save(currencyDto);
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter printWriter = resp.getWriter()) {
                printWriter.write(objectMapper.writeValueAsString(save));
            }
        } catch (DaoException e) {
            ExceptionHandler.handle(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "the database is unavailable"
            );
        }
    }

}
