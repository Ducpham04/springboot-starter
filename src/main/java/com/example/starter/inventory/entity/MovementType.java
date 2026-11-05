package com.example.starter.inventory.entity;

public enum MovementType {
    /** Opening stock entered when the product was created. */
    INITIAL,
    /** Goods received (stock receipt). */
    RECEIPT,
    /** Sold in an order. */
    SALE,
    /** Order cancelled, stock returned. */
    SALE_CANCEL,
    /** Stock count correction. */
    ADJUSTMENT
}
