package si.fri.prpo.finance.entitete;

import javax.persistence.*;
import java.math.BigDecimal;
import javax.json.bind.annotation.JsonbPropertyOrder;

@Entity
@Table(name = "transakcije")
@NamedQueries({
        @NamedQuery(name = "Transakcija.getAll", query = "SELECT o FROM Transakcija o"),
        @NamedQuery(name = "Transakcija.getById", query = "SELECT o FROM Transakcija o WHERE o.id = :id"),
        @NamedQuery(name = "Transakcija.getBySenderId", 
                   query = "SELECT o FROM Transakcija o WHERE o.senderId = :senderId"),
        @NamedQuery(name = "Transakcija.getByReceiverId", 
                   query = "SELECT o FROM Transakcija o WHERE o.receiverId = :receiverId"),
        @NamedQuery(
            name = "Transakcija.findByPortfolioId",
            query = "SELECT t FROM Transakcija t WHERE t.senderId = :portfolioId OR t.receiverId = :portfolioId ORDER BY t.createdAt DESC"
        )
})
@JsonbPropertyOrder({
    "id",
    "senderId",
    "receiverId",
    "amount",
    "description",
    "status",
    "createdAt"
})
public class Transakcija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;  // Changed to Long to match Customer.id type

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;  // Changed to Long to match Customer.id type

    @Column(nullable = false)
    private Double amount;

    @Column(name = "created_at")
    private String createdAt;

    @Column(length = 255)
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "stock_symbol")
    private String stockSymbol;

    @Column(name = "price")
    private BigDecimal price;

    // Enum for transaction status
    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED
    }

    // Constructors
    public Transakcija() {
    }

    public Transakcija(Long senderId, Long receiverId, Double amount) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // toString
    @Override
    public String toString() {
        return "Transakcija{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", amount=" + amount +
                ", createdAt='" + createdAt + '\'' +
                ", status=" + status +
                '}';
    }
}
