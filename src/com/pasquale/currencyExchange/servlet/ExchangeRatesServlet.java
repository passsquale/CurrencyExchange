package com.pasquale.currencyExchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pasquale.currencyExchange.dto.CurrencyDto;
import com.pasquale.currencyExchange.dto.ExchangeRateDto;
import com.pasquale.currencyExchange.exception.DaoException;
import com.pasquale.currencyExchange.util.ExceptionHandler;
import com.pasquale.currencyExchange.service.CurrencyService;
import com.pasquale.currencyExchange.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateDto> exchangeRates = exchangeRateService.findAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            try (PrintWriter printWriter = resp.getWriter()) {
                printWriter.write(objectMapper.writeValueAsString(exchangeRates));
            }

        } catch (DaoException e) {
            ExceptionHandler.handle(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "the database is unavailable");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if(baseCurrencyCode.isEmpty() || targetCurrencyCode.isEmpty() || rate.isEmpty()){
            ExceptionHandler.handle(resp, HttpServletResponse.SC_BAD_REQUEST, "The required form field is missing");
            return;
        }

        try {
            if(exchangeRateService.findByCode(baseCurrencyCode, targetCurrencyCode).isPresent()){
                ExceptionHandler.handle(resp, HttpServletResponse.SC_CONFLICT,
                        "A currency pair with this code already exists");
                return;
            }

            Optional<CurrencyDto> baseCurrency = currencyService.findByCode(baseCurrencyCode);
            Optional<CurrencyDto> targetCurrency = currencyService.findByCode(targetCurrencyCode);

            if(baseCurrency.isEmpty() || targetCurrency.isEmpty()){
                ExceptionHandler.handle(resp, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
                return;
            }

            ExchangeRateDto exchangeRateDto = new ExchangeRateDto(
                    -1,
                    baseCurrency.get(),
                    targetCurrency.get(),
                    Double.valueOf(rate)
            );

            ExchangeRateDto saveExchangeRate = exchangeRateService.save(exchangeRateDto);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            try (PrintWriter printWriter = resp.getWriter()) {
                printWriter.write(objectMapper.writeValueAsString(saveExchangeRate));
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
