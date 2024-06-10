package com.bforbank.testapp.mock.data.api;

import com.bforbank.demo.Amount;
import com.bforbank.demo.Transaction;
import com.bforbank.demo.TransactionType;
import com.bforbank.testapp.mock.data.producers.KafkaProducers;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
public class FakeTransactionsController {

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();
    private static final List<Type> TYPE_VALUES = List.of(Type.values());
    private static final int TYPE_SIZE = TYPE_VALUES.size();

    private static Type randomType() {
        return TYPE_VALUES.get(RANDOM.nextInt(TYPE_SIZE));
    }

    @Autowired
    private KafkaProducers kafkaProducers;

    enum Type {
        SEPA_TRANSFER,
        INSTANT_PAYMENT_TRANSFER,
        CARD_PAYMENT,
        DEBIT,
        CHECK
    }

    record FakeTransaction(String customerId, String accountId, Type type) {

        Transaction toAvro() {
            Amount amount = new Amount(new BigDecimal(FAKER.commerce().price()), FAKER.money().currencyCode());
            return Transaction.newBuilder()
                    .setCustomerId(customerId)
                    .setLabel(FAKER.commerce().vendor())
                    .setAmount(amount)
                    .setType(TransactionType.valueOf(type.name()))
                    .setTimestamp(Instant.now())
                    .build();
        }
    }

    public record FakeTransactionsRequest(Integer nbOfCustomers, Integer nbOfTransactions, Long waitIntervalMillis) {

        List<FakeTransaction> toFakeTransactionList() {
            List<FakeTransaction> fakeTransactions = new ArrayList<>();
            List<String> customerIds = new ArrayList<>();
            List<String> accountIds = new ArrayList<>();
            for (int i = 0; i < nbOfCustomers; i++) {
                customerIds.add(UUID.randomUUID().toString());
                accountIds.add(UUID.randomUUID().toString());
            }
            for (int i = 0; i < nbOfTransactions; i++) {
                int randomIndex = RANDOM.nextInt(nbOfCustomers);
                String customerId = customerIds.get(randomIndex);
                String accountId = accountIds.get(randomIndex);
                Type type = i % 2 == 0 ? Type.INSTANT_PAYMENT_TRANSFER : randomType();
                fakeTransactions.add(new FakeTransaction(customerId, accountId, type));
            }

            return fakeTransactions;
        }
    }

    @PostMapping("/fake-transactions")
    public ResponseEntity<Void> generateFakeTransactions(
            @RequestBody FakeTransactionsRequest fakeTransactionsRequest
    ) throws InterruptedException {

        List<FakeTransaction> transactions = fakeTransactionsRequest.toFakeTransactionList();
        for (FakeTransaction fakeTransaction : transactions) {
            kafkaProducers.publishTransaction(fakeTransaction.accountId(), fakeTransaction.toAvro());
            Thread.sleep(Optional.ofNullable(fakeTransactionsRequest.waitIntervalMillis).orElse(0L));
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
