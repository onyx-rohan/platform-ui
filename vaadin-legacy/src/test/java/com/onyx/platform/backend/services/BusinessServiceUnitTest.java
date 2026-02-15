package com.onyx.platform.backend.services;

import com.onyx.platform.backend.models.Business;
import com.onyx.platform.backend.models.BusinessType;
import com.onyx.platform.backend.models.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class BusinessServiceUnitTest {
    @Inject
    BusinessService businessService;

    private final String TEST_EMAIL = "testemail@onyx-software.com";
    private final String TEST_PASSWORD = "TEST_PASSWORD";
    private final String TEST_PRODUCT_NAME = "TEST_PRODUCT";
    private final String TEST_PRODUCT_DESCRIPTION = "TEST_DESCRIPTION";
    private final String TEST_BUSINESS = "TEST_BUSINESS";
    private final String TEST_PHONE = "1234567890";
    private final String TEST_VAT_NUMBER = "TVN12345";
    private final Long TEST_BUSINESS_TYPE_ID = 1L;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    @Transactional
    void tearDown() {
        Business.deleteAll();
    }

    @Test
    @Transactional
    void createNewBusiness() {
        businessService.create(Business.builder()
                .name(TEST_BUSINESS)
                .phone(TEST_PHONE)
                .vatNumber(TEST_VAT_NUMBER)
                .type(BusinessType.findById(TEST_BUSINESS_TYPE_ID))
                .build());

        Business business = Business.findById(1L);
        assertEquals("TEST_BUSINESS", business.getName());
        assertEquals("TVN12345", business.getVatNumber());
        assertEquals("TEST_CARD", business.getCardToken());
        assertEquals("TEST_CUSTOMER", business.getCustomerToken());

        User owner = business.getOwner();
        assertNotNull(owner);
    }
}
