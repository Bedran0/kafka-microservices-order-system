package com.example.stok_servisi;

public class OrderResult {

    private String orderId;
    private String product;
    private int quantity;

    private String status;
    private String reason;

    public OrderResult() {
    }

    public OrderResult(String orderId, String product, int quantity, String status, String reason) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.status = status;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}