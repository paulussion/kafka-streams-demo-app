package com.bforbank.testapp.view.controller;

import com.bforbank.testapp.view.entity.CustomerWithAccounts;
import com.bforbank.testapp.view.repository.CustomerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ViewController {

    private final CustomerRepository customerRepository;

    public ViewController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    public List<CustomerWithAccounts> getCustomers() {
        return customerRepository.findAll();
    }
}
