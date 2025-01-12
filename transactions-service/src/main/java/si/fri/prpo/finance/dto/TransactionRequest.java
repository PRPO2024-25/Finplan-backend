package si.fri.prpo.finance.dto;

public class TransactionRequest {
    private Integer senderId;
    private Integer receiverId;
    private Double amount;
    private String description;

    // Default constructor for JSON deserialization
    public TransactionRequest() {}

    public TransactionRequest(Integer senderId, Integer receiverId, Double amount, String description) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and setters
    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }
    
    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}