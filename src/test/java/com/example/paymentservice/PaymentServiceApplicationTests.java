package com.example.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "plaid.client-id=test-client-id",
        "plaid.secret=test-secret",
        "plaid.environment=sandbox",
        "plaid.base-url=https://sandbox.plaid.com"
})
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring Boot application context loads successfully
    }
}


