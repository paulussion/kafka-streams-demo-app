package com.bforbank.testapp.view.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstantPaymentMetricRepository extends CrudRepository<InstantPaymentMetricEntity, String> {
}
