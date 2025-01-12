package si.fri.prpo.finance.dto;

import si.fri.prpo.finance.entitete.Transakcija;

public class TransactionHistoryResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Double amount;
    private String description;
    private String status;
    private String createdAt;
    private String type;  // "INCOMING" or "OUTGOING"
    private Double balanceAfterTransaction;  // Optional: if you track this

    public TransactionHistoryResponse(Transakcija transaction, Long portfolioId) {
        this.id = transaction.getId().longValue();
        this.senderId = transaction.getSenderId();
        this.receiverId = transaction.getReceiverId();
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.status = transaction.getStatus().toString();
        this.createdAt = transaction.getCreatedAt();
        
        // Determine if this was an incoming or outgoing transaction
        this.type = portfolioId.equals(transaction.getReceiverId()) ? "INCOMING" : "OUTGOING";
    }

    // Getters and setters
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(Double balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
