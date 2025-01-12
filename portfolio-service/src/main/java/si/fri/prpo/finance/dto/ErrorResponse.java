package si.fri.prpo.finance.dto;

import javax.json.bind.annotation.JsonbProperty;

public class ErrorResponse {
    @JsonbProperty("message")
    private String message;
    
    @JsonbProperty("status")
    private int status;

    // Default constructor required for JSON-B
    public ErrorResponse() {
    }

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}