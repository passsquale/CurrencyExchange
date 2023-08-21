package com.pasquale.currencyExchange.dao;

import com.pasquale.currencyExchange.entity.Currency;
import com.pasquale.currencyExchange.entity.ExchangeRate;
import com.pasquale.currencyExchange.exception.DaoException;
import com.pasquale.currencyExchange.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao implements Dao<Integer, ExchangeRate>{

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String FIND_ALL_SQL = """
            SELECT exchange_rate.id as id, base_currency_id, target_currency_id, rate,
            c.code as base_currency_code, c.full_name as base_currency_full_name, c.sign as base_currency_sign,
            c2.code as target_currency_code, c2.full_name as target_currency_full_name, c2.sign as target_currency_sign
            FROM exchange_rate
            JOIN currency c
            ON exchange_rate.base_currency_id = c.id
            JOIN currency c2 on c2.id = exchange_rate.target_currency_id
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id = ?
            """;

    private static final String FIND_BY_CODE_SQL = FIND_ALL_SQL + """
            WHERE base_currency_code = ?
            and target_currency_code = ?
            """;

    private static final String SAVE_SQL = """
            INSERT INTO exchange_rate(base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE exchange_rate
            SET base_currency_id=?, target_currency_id=?, rate=?
            WHERE id = ?
            """;
    private ExchangeRateDao(){}

    @Override
    public List<ExchangeRate> findAll() throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()){
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Optional<ExchangeRate> findById(Integer id) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();
            return findOne(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    public Optional<ExchangeRate> findByCode(String baseCode, String targetCode) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            var resultSet = preparedStatement.executeQuery();
            return findOne(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
            preparedStatement.setDouble(3, exchangeRate.getRate());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
                exchangeRate.setId(generatedKeys.getInt(1));
            }
            return exchangeRate;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public ExchangeRate update(ExchangeRate exchangeRate) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
            preparedStatement.setDouble(3, exchangeRate.getRate());
            preparedStatement.setInt(4, exchangeRate.getId());
            preparedStatement.executeUpdate();
            return exchangeRate;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private Optional<ExchangeRate> findOne(ResultSet resultSet) throws SQLException {
        ExchangeRate exchangeRate = null;
        if(resultSet.next()){
            exchangeRate = buildExchangeRate(resultSet);
        }
        return Optional.ofNullable(exchangeRate);
    }
    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        Currency baseCurrency = new Currency(
                resultSet.getInt("base_currency_id"),
                resultSet.getString("base_currency_code"),
                resultSet.getString("base_currency_full_name"),
                resultSet.getString("base_currency_sign")
        );
        Currency targetCurrency = new Currency(
                resultSet.getInt("target_currency_id"),
                resultSet.getString("target_currency_code"),
                resultSet.getString("target_currency_full_name"),
                resultSet.getString("target_currency_sign")
        );
        return new ExchangeRate(
                resultSet.getInt("id"),
                baseCurrency,
                targetCurrency,
                resultSet.getDouble("rate")
        );
    }
    public static ExchangeRateDao getInstance(){
        return INSTANCE;
    }
}
