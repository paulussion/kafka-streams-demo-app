package com.bforbank.testapp.view.repository;

import com.bforbank.testapp.view.entity.CustomerWithAccounts;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<CustomerWithAccounts, String> {
}
