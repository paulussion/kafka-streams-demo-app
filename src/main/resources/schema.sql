DROP TABLE IF EXISTS instant_payment_metrics;
CREATE TABLE instant_payment_metrics
(
    customerId VARCHAR(255) PRIMARY KEY,
    accountId VARCHAR(255) NOT NULL,
    nbOfInstantPayment INT,
    lastPaymentTimestamp TIMESTAMP
);