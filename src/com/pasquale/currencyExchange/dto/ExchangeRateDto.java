package com.pasquale.currencyExchange.dto;


public record ExchangeRateDto(Integer id, CurrencyDto baseCurrency, CurrencyDto targetCurrency, Double rate) {
}
