
# Transfer Service

The Transfer Service is a Spring Boot application designed to handle money transfers between bank accounts. The service supports both intra-bank and inter-bank transfers and includes robust error handling, validation, and logging. This works with preloaded account configuration (see Limitation section below). 



## Features

- Intra-bank and Inter-bank Transfers: Supports transfers within the same bank as well as between different banks.
- Error Handling: Global exception handling for common errors like account not found, insufficient funds, and unregistered payees.
- Validation: Comprehensive input validation for all transfer requests.
- OpenAPI Documentation: Provides API documentation using OpenAPI (Swagger).
- Docker Support: Can be containerized using Docker for easy deployment.
- Localization: Error messages can be localized using message properties.
- API Versioning: API is versioned, to ensure backward compatibility while allowing for future improvements (Implemented with URI versioning)
- Idempotent API: To avoid duplicate processing, the transfer service API is made idempotent. It will return the old transaction if the request-id was processed earlier. The response will indicate whether it's a duplicate transaction or not.





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

## Design

This is the sequence diagram that illustrates the money transfer between intra-bank accounts:


![bLH1Ri8m4Bpx5IjEGO92UyoXed2gAXL4w0FSU8Ei9hQxTf3uVUsGW0bfHM_nxinuPhtAmZfEhgvAndEd2Jui4ZE-97Dfk78msX5Nja1QQUL8boNI2BY5ot4OdY4TP8uPbXY46qvt-9FH8fllQ-1BdkjQkIqQRQMNEJRGg3gWwrD9hNgKNeUna1gcpr6e53RhR0SpRkJiy3XphKVBBf1](https://github.com/user-attachments/assets/b273bc1d-713f-487b-9b63-3c2b11a112ce)


## Installation

## 1. Clone the Repository

```bash
  git clone https://github.com/PravijaSandeep/banking-transfer-service.git
```

```bash
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

### Transfer money from payer account to payee account

#### Available Versions

##### Version 1 (v1)


```http
  POST /api/v1/transfers
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
  "transactionId": "6e4e876e-f0f5-4c0c-b88f-1d672171814e",
  "status": "SUCCESS",
  "balance": 900,
  "amount": 100,
  "currency": "GBP",
  "transferType": "IntraBankTransfer",
  "timestamp": "2024-08-13T16:17:59.489450Z",
  "duplicate": false
}

• 200 OK: Transfer successful, for a duplicate transaction.

{
  "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "transactionId": "6e4e876e-f0f5-4c0c-b88f-1d672171814e",
  "status": "SUCCESS",
  "balance": 900,
  "amount": 100,
  "currency": "GBP",
  "transferType": "IntraBankTransfer",
  "timestamp": "2024-08-13T16:18:52.627107Z",
  "duplicate": true
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

• 404 Not Found: The requested resource was not found

{
    "status": "NOT_FOUND",
    "message": "The requested resource was not found",
    "timestamp": "2024-08-12T20:19:51.985907Z"
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


## API Health and Information

This application provides endpoints for monitoring the health status and retrieving application information through Spring Boot Actuator.

### Health Check Endpoint

The health check endpoint is used to verify the overall status of the application. It checks various indicators like database connectivity, disk space, and other custom health indicators.

- **URL:** `/actuator/health`
- **Method:** `GET`
- **Response Example:**

```json
  {
    "status": "UP"
  }
```

### Information Endpoint

The information endpoint provides details about the application, such as the version, description, and any other custom information provided by the InfoContributor.

- **URL:** `/actuator/info`
- **Method:** `GET`
- **Response Example:**

```json
 {
  "application": {
    "app": "Banking Transfer Service",
    "version": "1.0.0",
    "description": "API for transferring money between accounts"
  }
}

```


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

## Database

H2 in-memory database is used for development and testing. The database schema is automatically created at runtime based on the JPA entities.

## Error Handling

The application uses a centralized exception handling mechanism through a global exception handler. The error responses are standardized and returned in JSON format.
Example error response:

```
• 404 Not Found: Account or Payee not found

{
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "NOT_FOUND",
    "message": "Payee not registered",
    "timestamp": "2024-08-12T08:34:29.315428Z"
}
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

- Implement user authentication and secure message processing.
- Implement custom logic for transaction ID generation.
- Implement audit trails functionality for all requests.
- Integrate with real account database and read accounts. Currently, it is using a small pre-loaded configuration for accounts.
- Integrate with a real payment gateway for external transfers.



