package com.bforbank.testapp.view.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(name = "\"instant_payment_metrics\"")
public class InstantPaymentMetricEntity {

    @Id
    @Column("\"accountId\"")
    private String accountId;

    @Column("\"customerId\"")
    private String customerId;

    @Column("\"nbOfInstantPayment\"")
    private Long nbOfInstantPayment;

    @Column("\"lastPaymentTimestamp\"")
    private Instant lastPaymentTimestamp;
}
