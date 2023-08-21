package com.pasquale.currencyExchange.service;

import com.pasquale.currencyExchange.dao.CurrencyDao;
import com.pasquale.currencyExchange.dto.CurrencyDto;
import com.pasquale.currencyExchange.entity.Currency;
import com.pasquale.currencyExchange.exception.DaoException;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class CurrencyService {

    private static final CurrencyService INSTANCE = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();

    private CurrencyService(){}

    public List<CurrencyDto> findAll() throws DaoException {
        return currencyDao.findAll().stream()
                .map(CurrencyService::buildToCurrencyDto)
                .collect(toList());
    }
    public Optional<CurrencyDto> findByCode(String code) throws DaoException {
        return currencyDao.findByCode(code).map(CurrencyService::buildToCurrencyDto);
    }
    public CurrencyDto save(CurrencyDto currencyDto) throws DaoException {
        return buildToCurrencyDto(currencyDao.save(buildToCurrency(currencyDto)));
    }

    public static CurrencyService getInstance(){
        return INSTANCE;
    }

    public static Currency buildToCurrency(CurrencyDto currencyDto){
        return new Currency(
                currencyDto.id(),
                currencyDto.code(),
                currencyDto.fullName(),
                currencyDto.sign()
        );
    }
    public static CurrencyDto buildToCurrencyDto(Currency currency){
        return new CurrencyDto(
                currency.getId(),
                currency.getCode(),
                currency.getFullName(),
                currency.getSign()
        );
    }
}
