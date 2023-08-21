package com.pasquale.currencyExchange.dao;

import com.pasquale.currencyExchange.entity.Currency;
import com.pasquale.currencyExchange.exception.DaoException;
import com.pasquale.currencyExchange.util.ConnectionManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao implements Dao<Integer, Currency>{

    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String FIND_ALL_SQL = """
            SELECT *
            FROM currency
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE id=?
            """;
    private static final String FIND_BY_CODE_SQL = FIND_ALL_SQL + """
            WHERE code=?
            """;
    private static final String SAVE_SQL = """
            INSERT INTO currency(code, full_name, sign)
            VALUES (?, ?, ?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE currency
            SET code = ?, full_name = ?, sign = ?
            WHERE id = ?
            """;
    private CurrencyDao(){}
    @Override
    public List<Currency> findAll() throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while(resultSet.next()){
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Optional<Currency> findById(Integer id) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);
            var resultSet = preparedStatement.executeQuery();
            return findOne(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<Currency> findByCode(String code) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, code);
            var resultSet = preparedStatement.executeQuery();
            return findOne(resultSet);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public Currency update(Currency currency) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.setInt(4, currency.getId());
            preparedStatement.executeUpdate();
            return currency;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
    @Override
    public Currency save(Currency currency) throws DaoException {
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
                currency.setId(generatedKeys.getInt(1));
            }
            return currency;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public static CurrencyDao getInstance(){
        return INSTANCE;
    }

    private Optional<Currency> findOne(ResultSet resultSet) throws SQLException, DaoException {
        Currency currency = null;
        if(resultSet.next()){
            currency = buildCurrency(resultSet);
        }
        return Optional.ofNullable(currency);
    }

    private Currency buildCurrency(ResultSet resultSet) throws DaoException {
        try {
            return new Currency(
                    resultSet.getObject("id", Integer.class),
                    resultSet.getObject("code", String.class),
                    resultSet.getObject("full_name", String.class),
                    resultSet.getObject("sign", String.class)

            );
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
