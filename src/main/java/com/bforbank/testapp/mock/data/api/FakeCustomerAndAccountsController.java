package com.bforbank.testapp.mock.data.api;

import com.bforbank.demo.*;
import com.bforbank.testapp.mock.data.producers.KafkaProducers;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

@RestController
public class FakeCustomerAndAccountsController {

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();
    private static final List<AccountType> ACCOUNT_TYPE_VALUES =
            Arrays.stream(AccountType.values()).filter(accountType -> AccountType.UNKNOWN != accountType).toList();
    private static final int ACCOUNT_TYPE_SIZE = ACCOUNT_TYPE_VALUES.size();
    private static final List<AccountStatus> ACCOUNT_STATUS_VALUES =
            Arrays.stream(AccountStatus.values()).filter(accountStatus -> AccountStatus.UNKNOWN != accountStatus).toList();
    private static final int ACCOUNT_STATUS_SIZE = ACCOUNT_STATUS_VALUES.size();

    private static AccountType randomAccountType() {
        return ACCOUNT_TYPE_VALUES.get(RANDOM.nextInt(ACCOUNT_TYPE_SIZE));
    }

    private static AccountStatus randomAccountStatus() {
        return ACCOUNT_STATUS_VALUES.get(RANDOM.nextInt(ACCOUNT_STATUS_SIZE));
    }

    private List<String> customers;

    @Autowired
    private KafkaProducers kafkaProducers;

    public record FakeAccount(String accountId, AccountType accountType) {

        Account toAvro() {
            Amount amount = new Amount(new BigDecimal(FAKER.commerce().price()), FAKER.money().currencyCode());
            return Account.newBuilder()
                    .setAccountId(accountId)
                    .setBalance(amount)
                    .setType(accountType)
                    .setStatus(randomAccountStatus())
                    .build();
        }
    }

    public record FakeCustomer(String customerId) {

        Customer toAvro() {
            return Customer.newBuilder()
                    .setId(customerId)
                    .setFirstName(FAKER.name().firstName())
                    .setLastName(FAKER.name().lastName())
                    .build();
        }
    }

    @PostMapping("/fake-customers")
    public ResponseEntity<Void> generateFakeCustomers() throws InterruptedException {
        customers = IntStream.range(0, 10).mapToObj(_ -> UUID.randomUUID().toString()).toList();
        for (String customerId : customers) {
            FakeCustomer fakeCustomer = new FakeCustomer(customerId);
            kafkaProducers.publishCustomer(customerId, fakeCustomer.toAvro());
            Thread.sleep(200L);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/fake-accounts")
    public ResponseEntity<Void> generateFakeAccounts() throws InterruptedException {
        List<String> accounts = IntStream.range(0, 10).mapToObj(_ -> UUID.randomUUID().toString()).toList();
        for (int i = 0; i < accounts.size(); i++) {
            var accountId = accounts.get(i);
            var accountType = i % 2 == 0 ? AccountType.CURRENT_ACCOUNT : randomAccountType();
            String customerId = customers.get(RANDOM.nextInt(10));
            FakeAccount fakeAccount = new FakeAccount(accountId, accountType);
            kafkaProducers.publishAccount(customerId, fakeAccount.toAvro());
            Thread.sleep(200L);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
