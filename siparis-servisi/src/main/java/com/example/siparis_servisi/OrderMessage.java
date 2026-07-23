package com.example.siparis_servisi;

import java.util.ArrayList;
import java.util.List;

public class OrderMessage {

    private String orderId;
    private String customerName;
    private List<ItemMessage> items = new ArrayList<>();

    public OrderMessage() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<ItemMessage> getItems() {
        return items;
    }

    public void setItems(List<ItemMessage> items) {
        this.items = items;
    }

    public static class ItemMessage {
        private String product;
        private int quantity;

        public ItemMessage() {
        }

        public ItemMessage(String product, int quantity) {
            this.product = product;
            this.quantity = quantity;
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
    }
}
