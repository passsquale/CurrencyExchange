# Currency Exchange
REST API for describing currencies and exchange rates. Allows you to view and edit lists of currencies and exchange rates, and calculate the conversion of arbitrary amounts from one currency to another.
For educational purposes, the project was written without frameworks.
- Java (JavaEE)
- SQLite
- JdbcTemplate
- Servlets
# Currencies


GET `/currencies`

Response example:
```json
[
    {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },   
    {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
]
```
HTTP response status codes:
  - Successful - 200
  - Server error - 500

GET `/currency/EUR`

Response example:
```json
{
   "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```
HTTP response status codes:
  - Successful - 200
  - The currency code is missing in the address - 400
  - Currency not found - 404
  - Server error - 500
    
POST `/currencies`

The data is transmitted in the request body as form fields (x-www-form-urlencoded). Form fields - `name`, `code`, `sign`.

Response example:
```json
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```
HTTP response status codes:
  - Successful - 200
  - The required form field is missing - 400
  - The currency with this code already exists - 409
  - Server error - 500

# Exchange rates
GET `/exchangeRates`

Response example:
```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```
HTTP response status codes:
  - Successful - 200
  - Server error - 500

GET `/exchangeRate/USDRUB`

Response example:
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
HTTP response status codes:
  - Successful - 200
  - The currency codes of the pair are missing in the address - 400
  - The exchange rate for the pair was not found - 404
  - Server error - 500

POST `/exchangeRate/USDRUB`

Adding a new exchange rate to the database. The data is transmitted in the request body as form fields (x-www-form-urlencoded).
Form fields - `baseCurrencyCode`, `targetCurrencyCode`, `rate`.

Response example:
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
HTTP response status codes:
  - Successful - 200
  - The required form field is missing - 400
  - A currency pair with this code already exists - 409
  - Server error - 500

PATCH `/exchangeRate/USDRUB`

Response example:
```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
HTTP response status codes:
  - Successful - 200
  - The required form field is missing - 400
  - The currency pair is missing in the database - 404
  - Server error - 500

# Currency exchange
GET `/exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`

example: `/exchange?from=USD&to=AUD&amount=10`

Response example:
```json
{
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 1.45,
    "amount": 10.00
    "convertedAmount": 14.50
}
```
