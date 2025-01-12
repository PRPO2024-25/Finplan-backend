package si.fri.prpo.finance.exceptions;

public class PortfolioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PortfolioException(String message) {
        super(message);
    }

    public PortfolioException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getErrorCode() {
        return 400;
    }
} 