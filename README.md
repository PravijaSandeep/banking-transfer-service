
# Transfer Service

The Transfer Service is a Spring Boot application designed to handle money transfers between bank accounts. The service supports both intra-bank and inter-bank transfers and includes robust error handling, validation, and logging.



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
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

transfer.source.bank.name=BANK_A
transfer.source.bank.code=A0001

logging.level.com.exercise.banking=DEBUG
logging.file.name=logs/app.log

```


## Installation

## 1. Clone the Repository

```bash
  git clone https://github.com/PravijaSandeep/banking-transfer-service.git
  cd transfer-service
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
docker build -t transfer-service
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
    "transactionId": "56f8bcc0-468d-4a9c-89a7-8a10f7bd4f37",
    "status": "SUCCESS",
    "balance": 900.00,
    "amount": 100,
    "transferType": "IntraBankTransfer",
    "timestamp": "2024-08-11T18:26:21.551"
}

• 400 Bad Request: Insufficient funds

{
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "BAD_REQUEST",
    "message": "Insufficient funds in account",
    "timestamp": "2024-08-11T18:25:39.900"
}

• 400 Bad Request: Invalid input

{
    "status": "BAD_REQUEST",
    "message": "Validation error",
    "timestamp": "2024-08-11T18:25:04.137",
    "validationErrors": {
        "payeeBankName": "Payee bank name is required"
    }
}

• 404 Not Found: Account or Payee not found

{
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "NOT_FOUND",
    "message": "Payee not registered",
    "timestamp": "2024-08-11T18:26:50.243"
}

• 500 Internal Server Error: Unexpected error

{
	"requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred",
    "timestamp": "2024-08-11T13:14:04.738955"
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


## Roadmap

- Implement custom logic for transaction id generation.
- Integrate with real account database and read accounts. Currently it is using a small preloaded configutaion for accounts.

- Integrate with a real payment gateway for external transfers.



