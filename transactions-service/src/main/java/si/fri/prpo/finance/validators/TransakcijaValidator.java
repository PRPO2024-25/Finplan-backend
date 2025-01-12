package si.fri.prpo.finance.validators;

import si.fri.prpo.finance.entitete.Transakcija;
import si.fri.prpo.finance.exceptions.ValidationException;

public class TransakcijaValidator {
    
    public static void validateTransakcija(Transakcija transakcija) {
        if (transakcija == null) {
            throw new ValidationException("Transaction cannot be null");
        }
        
        // Validate sender
        if (transakcija.getSenderId() == null || transakcija.getSenderId() < 0) {
            throw new ValidationException("Invalid sender ID");
        }
        
        // Validate receiver
        if (transakcija.getReceiverId() == null || transakcija.getReceiverId() < 0) {
            throw new ValidationException("Invalid receiver ID");
        }
        
        // Comment out or remove the sender != receiver validation for deposits/withdrawals
        // if (transakcija.getSenderId().equals(transakcija.getReceiverId())) {
        //     throw new ValidationException("Sender and receiver cannot be the same");
        // }
        
        // Validate amount
        if (transakcija.getAmount() == null) {
            throw new ValidationException("Amount cannot be null");
        }
        
        // Allow negative amounts for withdrawals
        // if (transakcija.getAmount() <= 0) {
        //     throw new ValidationException("Amount must be positive");
        // }
        
        if (Math.abs(transakcija.getAmount()) > 1000000) { // Example maximum limit
            throw new ValidationException("Amount exceeds maximum limit of 1,000,000");
        }

        // Validate description if present
        if (transakcija.getDescription() != null && 
            transakcija.getDescription().length() > 255) {
            throw new ValidationException("Description exceeds maximum length of 255 characters");
        }
    }

    public static void validateAmountRange(Double minAmount, Double maxAmount) {
        if (minAmount != null && maxAmount != null) {
            if (minAmount > maxAmount) {
                throw new ValidationException("Minimum amount cannot be greater than maximum amount");
            }
            if (minAmount < 0 || maxAmount < 0) {
                throw new ValidationException("Amount cannot be negative");
            }
        }
    }
}
