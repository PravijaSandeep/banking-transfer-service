
# Transfer Service

The Transfer Service is a Spring Boot application designed to handle money transfers between bank accounts. The service supports both intra-bank and inter-bank transfers and includes robust error handling, validation, and logging. This works with preloaded account configuration (see Limitation section below). 



## Features

- Intra-bank and Inter-bank Transfers: Supports transfers within the same bank as well as between different banks.
- Error Handling: Global exception handling for common errors like account not found, insufficient funds, and unregistered payees.
- Validation: Comprehensive input validation for all transfer requests.
- OpenAPI Documentation: Provides API documentation using OpenAPI (Swagger).
- Docker Support: Can be containerised using Docker for easy deployment.
- Localization: Error messages can be localized using message properties.





## Technologies Used

• Spring Boot 3.2.8

• Spring Data JPA

• H2 Database (for testing)

• Lombok

• OpenAPI

• Docker

• JUnit 5



## Prerequisites

• Java 17

• Maven 3.x

• Docker (for containerization)





## Application Properties

Application-specific properties can be set in src/main/resources/application.properties:

```bash
# application.properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

transfer.source.bank.name=BANK_A
transfer.source.bank.code=A00001

logging.level.com.exercise.banking=DEBUG
logging.file.name=logs/app.log

```


## Installation

## 1. Clone the Repository

```bash
  git clone https://github.com/PravijaSandeep/banking-transfer-service.git
  cd banking-transfer-service
```

## 2. Build the Application

```bash
mvn clean install
```

## 3. Run the Application or use Docker

```bash
mvn spring-boot:run
```

## 3. (Optional) If running using Docker, build Docker Image 

```bash
docker build -t transfer-service .
 ```

## 4. (Optional) Run Docker Container 

```bash
docker run -p 8080:8080 transfer-service
```




## API Reference

#### Transfer money from payer acount to payee account

```http
  POST /api/transfers
  Consumes: application/json
  Produces: application/json


  Request Body:
  {
    "requestId": "123e4567-e89b-12d3-a456-426614174000",
    "payerAccNumber": "ACC001",
    "payeeAccNumber": "OACC001",
    "payeeBankName": "BANK_B",
    "payeeBankCode": "B0001",
    "amount": 1000.00
 }


Responses:

• 200 OK: Transfer successful

{
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "transactionId": "8559a0c0-a1e6-4543-9395-a1bf093bda3f",
    "status": "SUCCESS",
    "balance": 800.00,
    "amount": 100,
    "transferType": "IntraBankTransfer",
    "timestamp": "2024-08-12T08:35:12.245057Z"
}

• 400 Bad Request: Insufficient funds

{
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "BAD_REQUEST",
    "message": "Insufficient funds in account",
    "timestamp": "2024-08-12T08:36:05.149401Z"
}

• 400 Bad Request: Invalid input

{
    "status": "BAD_REQUEST",
    "message": "Validation failed for input data",
    "timestamp": "2024-08-12T08:33:49.016691Z",
    "validationErrors": {
        "payeeBankCode": "Payee bank code is required"
    }
}

• 404 Not Found: Account or Payee not found

{
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "NOT_FOUND",
    "message": "Payee not registered",
    "timestamp": "2024-08-12T08:34:29.315428Z"
}

• 500 Internal Server Error: Unexpected error

{
	"requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred",
    "timestamp": "2024-08-12T08:34:29.315428Z"
}

````





## OpenAPI Documentation

After running the application, you can access the API documentation [here:](http://localhost:8080/swagger-ui/index.html)


## Localization

The error response messages stored in src/main/resources/messages.properties and can be localized by adding additional messages_{locale}.properties files.


## Running Tests

To run tests, run the following command

```bash
  mvn test
```

Generate Test Coverage Report

```bash
mvn jacoco:report
```


## Limitations

This project facilitates the transfer of money between predefined accounts. Any account or payee outside of this predefined set will not be recognized and the transfer will fail.


### Predefined accounts

Account 1: 
    Account Holder : Person1,
    Account Num : 123456,
    Bank code : A00001,
    Bank Name : BANK_A,
    Payees : Payee1 and Payee 2

Account 2: 
    Account Holder : Person2,
    Account Num : 789123,
    Bank code : A00001,
    Bank Name : BANK_A,
    Payees : None

Account 2: 
    Account Holder : Person3,
    Account Num : 978654,
    Bank code : A00001,
    Bank Name : BANK_A,
    Payees : Payee 3
    

### Predefined Payees

Payee 1: 
    Name : Person1-Payee1,
    Account Num : 978654,
    Type : INTRA_BANK Payee,
    Bank : BANK_A,
    Bank Code : A00001

Payee 2: 
    Name : Person1-Payee2,
    Account Num : 654321,
    Type : INTER_BANK Payee,
    Bank : BANK_B,
    Bank code : B00001

Payee 3:
    Name : Person3-Payee1,
    Account Num : 654321,
    Type : INTER_BANK Payee,
    Bank : BANK_B,
    Bank code : B00001





## Future Tasks

- Implement custom logic for transaction id generation.
- Integrate with real account database and read accounts. Currently it is using a small pre-loaded configuration for accounts.
- Integrate with a real payment gateway for external transfers.



