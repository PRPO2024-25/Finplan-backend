package si.fri.prpo.finance.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private Integer toPortfolioId;
    private BigDecimal amount;
    private String description;

    // Getters and Setters
    public Integer getToPortfolioId() {
        return toPortfolioId;
    }

    public void setToPortfolioId(Integer toPortfolioId) {
        this.toPortfolioId = toPortfolioId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}