package si.fri.prpo.finance.entitete;

import javax.persistence.*;
import java.util.Date;
import java.math.BigDecimal;
import java.io.Serializable;
import javax.json.bind.annotation.JsonbDateFormat;
import java.util.List;


@Entity
@Table(name = "portfolios")
@NamedQueries({
    @NamedQuery(name = "Portfolio.getAll", query = "SELECT p FROM Portfolio p"),
    @NamedQuery(name = "Portfolio.getByUserId", query = "SELECT p FROM Portfolio p WHERE p.userId = :userId")
})
public class Portfolio implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cash_balance", nullable = false)
    private BigDecimal cashBalance;

    @JsonbDateFormat("yyyy-MM-dd")
    @Column(name = "created_at")
    private Date createdAt;

    //stock list
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Stock> stocks;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }
}
