package com.pasquale.currencyExchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pasquale.currencyExchange.dto.ExchangeRateDto;
import com.pasquale.currencyExchange.exception.DaoException;
import com.pasquale.currencyExchange.util.ExceptionHandler;
import com.pasquale.currencyExchange.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if(method.equals("PATCH")){
            this.doPatch(req, resp);
        }else {
            super.service(req, resp);
        }
    }

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if(pathInfo == null || pathInfo.equals("/")) {
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_BAD_REQUEST, "Currency codes of the pair are missing in the address"
            );
            return;
        }
        String baseCode = pathInfo.replace("/", "").substring(0, 3).toUpperCase();
        String targetCode = pathInfo.replace("/", "").substring(3).toUpperCase();
        try {
            Optional<ExchangeRateDto> exchangeRate = exchangeRateService.findByCode(baseCode, targetCode);
            if(exchangeRate.isEmpty()){
                ExceptionHandler.handle(resp, HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
            }else{
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                try (PrintWriter printWriter = resp.getWriter()) {
                    printWriter.write(objectMapper.writeValueAsString(exchangeRate.get()));
                }
            }

        } catch (DaoException e) {
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "the database is unavailable"
            );
        }

    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        Optional<String> rateOptional = Optional.ofNullable(req.getReader().readLine());
        if(pathInfo == null || pathInfo.equals("/") || rateOptional.isEmpty()) {
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_BAD_REQUEST, "The required form field is missing"
            );
            return;
        }
        String rate = rateOptional.get().replace("rate=","");
        String baseCode = pathInfo.replace("/", "").substring(0, 3).toUpperCase();
        String targetCode = pathInfo.replace("/", "").substring(3).toUpperCase();
        try {
            Optional<ExchangeRateDto> exchangeRateDto = exchangeRateService.findByCode(baseCode, targetCode);
            if(exchangeRateDto.isEmpty()){
                ExceptionHandler.handle(resp, HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
            }else{
                ExchangeRateDto rateDto = exchangeRateDto.get();
                ExchangeRateDto updatedExchangeRate = exchangeRateService.update(new ExchangeRateDto(
                        rateDto.id(),
                        rateDto.baseCurrency(),
                        rateDto.targetCurrency(),
                        Double.valueOf(rate)
                ));
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                try (PrintWriter printWriter = resp.getWriter()) {
                    printWriter.write(objectMapper.writeValueAsString(updatedExchangeRate));
                }
            }
        } catch (DaoException e) {
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "the database is unavailable"
            );
        }
    }

}
