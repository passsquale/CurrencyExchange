package com.pasquale.currencyExchange.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pasquale.currencyExchange.dto.CurrencyDto;
import com.pasquale.currencyExchange.dto.ExchangeDto;
import com.pasquale.currencyExchange.exception.DaoException;
import com.pasquale.currencyExchange.service.CurrencyService;
import com.pasquale.currencyExchange.util.ExceptionHandler;
import com.pasquale.currencyExchange.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Optional<String> baseCode = Optional.ofNullable(req.getParameter("from"));
        Optional<String> targetCode = Optional.ofNullable(req.getParameter("to"));
        Optional<String> amount = Optional.ofNullable(req.getParameter("amount"));


        if(baseCode.isEmpty() || targetCode.isEmpty() || amount.isEmpty()){
            ExceptionHandler.handle(
                    resp, HttpServletResponse.SC_BAD_REQUEST, "The required form field is missing"
            );
            return;
        }
        try {
            Optional<CurrencyDto> fromDto = currencyService.findByCode(baseCode.get());
            Optional<CurrencyDto> toDto = currencyService.findByCode(targetCode.get());

            if(fromDto.isPresent() && toDto.isPresent()){
                Optional<ExchangeDto> exchangeDto = exchangeRateService.calculateExchange(fromDto.get(), toDto.get(), Double.valueOf(amount.get()));
                if(exchangeDto.isPresent()){
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("application/json");
                    String json = objectMapper.writeValueAsString(exchangeDto.get());
                    try (PrintWriter printWriter = resp.getWriter()) {
                        printWriter.write(json);
                    }
                }
                else{
                    ExceptionHandler.handle(resp, HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
                }
            }else{
                ExceptionHandler.handle(resp, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
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
