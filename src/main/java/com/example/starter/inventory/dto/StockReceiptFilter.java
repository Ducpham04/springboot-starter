package com.example.starter.inventory.dto;

import java.time.LocalDate;

public record StockReceiptFilter(String keyword, LocalDate from, LocalDate to) {
}
