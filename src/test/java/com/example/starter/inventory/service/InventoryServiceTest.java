package com.example.starter.inventory.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceTest {

    @Test
    void movingAverageWeighsExistingAndReceivedStock() {
        assertThat(InventoryService.movingAverage(10, new BigDecimal("1000"), 10, new BigDecimal("2000")))
                .isEqualByComparingTo("1500");
        assertThat(InventoryService.movingAverage(30, new BigDecimal("1000"), 10, new BigDecimal("2000")))
                .isEqualByComparingTo("1250");
    }

    @Test
    void movingAverageUsesNewCostWhenNoStockLeft() {
        assertThat(InventoryService.movingAverage(0, new BigDecimal("1000"), 5, new BigDecimal("1800")))
                .isEqualByComparingTo("1800");
    }

    @Test
    void movingAverageRoundsToTwoDecimals() {
        assertThat(InventoryService.movingAverage(1, new BigDecimal("1000"), 2, new BigDecimal("1000.01")))
                .isEqualByComparingTo("1000.01");
        assertThat(InventoryService.movingAverage(2, new BigDecimal("1"), 1, new BigDecimal("2")))
                .isEqualByComparingTo("1.33");
    }
}
