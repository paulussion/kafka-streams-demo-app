package com.bforbank.testapp.view.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "accounts")
public class CustomerWithAccounts {

    @Id
    public String id;
    public String firstName;
    public String lastName;
    public List<Account> accounts;
}
