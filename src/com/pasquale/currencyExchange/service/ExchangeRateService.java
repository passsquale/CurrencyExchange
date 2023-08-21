package com.pasquale.currencyExchange.service;

import com.pasquale.currencyExchange.dao.ExchangeRateDao;
import com.pasquale.currencyExchange.dto.CurrencyDto;
import com.pasquale.currencyExchange.dto.ExchangeDto;
import com.pasquale.currencyExchange.dto.ExchangeRateDto;
import com.pasquale.currencyExchange.entity.ExchangeRate;
import com.pasquale.currencyExchange.exception.DaoException;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();

    private ExchangeRateService() {
    }

    public List<ExchangeRateDto> findAll() throws DaoException {
        return exchangeRateDao.findAll().stream()
                .map(ExchangeRateService::buildToExchangeRateDto)
                .collect(toList());
    }

    public Optional<ExchangeRateDto> findByCode(String baseCode, String targetCode) throws DaoException {
        return exchangeRateDao.findByCode(baseCode, targetCode)
                .map(ExchangeRateService::buildToExchangeRateDto);
    }

    public ExchangeRateDto save(ExchangeRateDto exchangeRateDto) throws DaoException {
        return buildToExchangeRateDto(
                exchangeRateDao.save(buildToExchangeRate(exchangeRateDto))
        );
    }
    public ExchangeRateDto update(ExchangeRateDto exchangeRateDto) throws DaoException {
        return buildToExchangeRateDto(
                exchangeRateDao.update(buildToExchangeRate(exchangeRateDto))
        );
    }
    public Optional<ExchangeDto> calculateExchange(CurrencyDto from, CurrencyDto to, Double amount) throws DaoException {
        Optional<ExchangeRateDto> exchangeRateDtoOptional = findByCode(from.code(), to.code());
        if(exchangeRateDtoOptional.isPresent()){
            ExchangeRateDto exchangeRateDto = exchangeRateDtoOptional.get();
            return Optional.of(buildExchangeDto(exchangeRateDto, exchangeRateDto.rate(), amount));
        }
        exchangeRateDtoOptional = findByCode(to.code(), from.code());
        if(exchangeRateDtoOptional.isPresent()){
            ExchangeRateDto exchangeRateDto = exchangeRateDtoOptional.get();
            return Optional.of(buildExchangeDto(exchangeRateDto, 1 / exchangeRateDto.rate(), amount));
        }
        Optional<ExchangeRateDto> usdToBaseOpt = findByCode("USD", from.code());
        Optional<ExchangeRateDto> usdToTargetOpt = findByCode("USD", to.code());
        if(usdToBaseOpt.isPresent() && usdToTargetOpt.isPresent()){
            ExchangeRateDto usdToBase = usdToBaseOpt.get();
            ExchangeRateDto usdToTarget = usdToTargetOpt.get();
            Double rate = usdToTarget.rate() / usdToBase.rate();
            Double convertedAmount = amount * rate;
            return Optional.of(new ExchangeDto(from, to, rate, amount, convertedAmount));

        }else{
            return Optional.empty();
        }
    }

    private static ExchangeRateDto buildToExchangeRateDto(ExchangeRate exchangeRate) {
        return new ExchangeRateDto(
                exchangeRate.getId(),
                CurrencyService.buildToCurrencyDto(exchangeRate.getBaseCurrency()),
                CurrencyService.buildToCurrencyDto(exchangeRate.getTargetCurrency()),
                exchangeRate.getRate()
        );
    }

    private static ExchangeRate buildToExchangeRate(ExchangeRateDto exchangeRateDto) {
        return new ExchangeRate(
                exchangeRateDto.id(),
                CurrencyService.buildToCurrency(exchangeRateDto.baseCurrency()),
                CurrencyService.buildToCurrency(exchangeRateDto.targetCurrency()),
                exchangeRateDto.rate()
        );
    }
    private ExchangeDto buildExchangeDto(ExchangeRateDto exchangeRateDto, Double rate, Double amount){
        Double convertedAmount = amount * rate;
        return new ExchangeDto(
                exchangeRateDto.baseCurrency(),
                exchangeRateDto.targetCurrency(),
                rate,
                amount,
                convertedAmount
        );
    }

    public static ExchangeRateService getInstance(){
        return INSTANCE;
    }
}
