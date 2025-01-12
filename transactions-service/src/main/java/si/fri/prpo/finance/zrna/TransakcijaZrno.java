package si.fri.prpo.finance.zrna;

import si.fri.prpo.finance.entitete.Transakcija;
import si.fri.prpo.finance.exceptions.ValidationException;
import si.fri.prpo.finance.clients.PortfolioServiceAPI;
import javax.ws.rs.core.Response;
import si.fri.prpo.finance.dto.TransactionRequest;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.logging.Logger;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@ApplicationScoped
public class TransakcijaZrno {

    @Inject
    @RestClient
    private PortfolioServiceAPI portfolioService;

    @PersistenceContext(unitName = "kumuluzee-samples-jpa")
    private EntityManager em;

    private static final Logger LOG = Logger.getLogger(TransakcijaZrno.class.getName());

    @Transactional
    public Transakcija createTransakcija(Transakcija transakcija) {
        try {
            LOG.info("=== Starting Transaction Process ===");
            LOG.info("Sender Portfolio: " + transakcija.getSenderId() + ", Receiver Portfolio: " + transakcija.getReceiverId());
            
            // 1. Check if sender has sufficient funds
            Response balanceResponse = portfolioService.canTransfer(
                transakcija.getSenderId().intValue(),
                BigDecimal.valueOf(transakcija.getAmount())
            );
            
            if (balanceResponse.getStatus() != 200) {
                LOG.warning("Insufficient funds check failed. Status: " + balanceResponse.getStatus());
                transakcija.setStatus(Transakcija.TransactionStatus.FAILED);
                transakcija.setDescription("Transaction failed: Insufficient funds in sender's portfolio");
                em.persist(transakcija);
                throw new ValidationException("Cannot create transaction: Insufficient funds in sender's portfolio");
            }
            LOG.info("Sufficient funds confirmed");

            // 2. Process transaction and get user information
            Response transactionResponse = portfolioService.processTransaction(
                new TransactionRequest(
                    transakcija.getSenderId().intValue(),
                    transakcija.getReceiverId().intValue(),
                    transakcija.getAmount().doubleValue(),
                    transakcija.getDescription()    
                )
            );

            if (transactionResponse.getStatus() != 200) {
                LOG.warning("Transaction processing failed: " + transactionResponse.getStatus());
                transakcija.setStatus(Transakcija.TransactionStatus.FAILED);
                em.persist(transakcija);
                throw new ValidationException("Failed to process transaction");
            }

            // 3. Create transaction record
            transakcija.setCreatedAt(new Date().toString());
            transakcija.setStatus(Transakcija.TransactionStatus.COMPLETED);
            em.persist(transakcija);
            
            LOG.info("Transaction completed successfully. ID: " + transakcija.getId());
            return transakcija;
            
        } catch (Exception e) {
            LOG.severe("Transaction creation failed: " + e.getMessage());
            throw new ValidationException(e.getMessage());
        }
    }

    public List<Transakcija> getTransakcije(
            Double minAmount, Double maxAmount, 
            Date startDate, Date endDate,
            String status, String type,
            String sortBy, String sortOrder,
            Integer limit) {
                
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Transakcija> query = cb.createQuery(Transakcija.class);
        Root<Transakcija> root = query.from(Transakcija.class);
        List<Predicate> predicates = new ArrayList<>();

        // Add filters
        if (minAmount != null) predicates.add(cb.ge(root.get("amount"), minAmount));
        if (maxAmount != null) predicates.add(cb.le(root.get("amount"), maxAmount));
        if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        if (status != null) predicates.add(cb.equal(root.get("status"), status));
        if (type != null) predicates.add(cb.equal(root.get("type"), type));

        query.where(predicates.toArray(new Predicate[0]));
        
        // Add sorting
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            query.orderBy(cb.asc(root.get(sortBy)));
        } else {
            query.orderBy(cb.desc(root.get(sortBy)));
        }

        TypedQuery<Transakcija> typedQuery = em.createQuery(query);
        if (limit != null) typedQuery.setMaxResults(limit);
        
        return typedQuery.getResultList();
    }

    public List<Transakcija> getTransactionHistory(Long portfolioId, Integer limit) {
        LOG.info("Fetching transaction history for portfolio: " + portfolioId);
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Transakcija> query = cb.createQuery(Transakcija.class);
        Root<Transakcija> root = query.from(Transakcija.class);
        
        // Find transactions where portfolio is either sender or receiver
        Predicate senderPredicate = cb.equal(root.get("senderId"), portfolioId);
        Predicate receiverPredicate = cb.equal(root.get("receiverId"), portfolioId);
        query.where(cb.or(senderPredicate, receiverPredicate));
        
        // Order by creation date, newest first
        query.orderBy(cb.desc(root.get("createdAt")));
        
        TypedQuery<Transakcija> typedQuery = em.createQuery(query);
        if (limit != null) {
            typedQuery.setMaxResults(limit);
        }
        
        return typedQuery.getResultList();
    }
    // Get transaction by ID
    public Transakcija getTransakcija(Integer id) {
        return em.find(Transakcija.class, id);
    }

    public List<Transakcija> getFilteredTransactions(
            Long portfolioId, String startDate, String endDate, 
            String type, Double minAmount, Double maxAmount,
            Integer limit, Integer offset) {
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Transakcija> query = cb.createQuery(Transakcija.class);
        Root<Transakcija> root = query.from(Transakcija.class);
        List<Predicate> predicates = new ArrayList<>();

        // Base portfolio filter
        if ("INCOMING".equals(type)) {
            predicates.add(cb.equal(root.get("receiverId"), portfolioId));
        } else if ("OUTGOING".equals(type)) {
            predicates.add(cb.equal(root.get("senderId"), portfolioId));
        } else {
            predicates.add(cb.or(
                cb.equal(root.get("senderId"), portfolioId),
                cb.equal(root.get("receiverId"), portfolioId)
            ));
        }

        // Add date filters
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        // Add amount filters
        if (minAmount != null) {
            predicates.add(cb.ge(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            predicates.add(cb.le(root.get("amount"), maxAmount));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<Transakcija> typedQuery = em.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

        return typedQuery.getResultList();
    }

    public Map<String, Object> getTransactionSummary(Long portfolioId, String startDate, String endDate) {
        List<Transakcija> transactions = getFilteredTransactions(portfolioId, startDate, endDate, null, null, null, Integer.MAX_VALUE, 0);
        
        double totalIncoming = 0.0;
        double totalOutgoing = 0.0;
        int incomingCount = 0;
        int outgoingCount = 0;
        
        for (Transakcija t : transactions) {
            if (t.getReceiverId().equals(portfolioId)) {
                totalIncoming += t.getAmount();
                incomingCount++;
            } else {
                totalOutgoing += t.getAmount();
                outgoingCount++;
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncoming", totalIncoming);
        summary.put("totalOutgoing", totalOutgoing);
        summary.put("netFlow", totalIncoming - totalOutgoing);
        summary.put("incomingCount", incomingCount);
        summary.put("outgoingCount", outgoingCount);
        summary.put("totalTransactions", transactions.size());
        
        return summary;
    }
    //call procces transaction
    public Response processTransaction(TransactionRequest request) {
        return portfolioService.processTransaction(request);
    }
    //get all transaction of a portfolio
    public List<Transakcija> getTransakcijeByPortfolioId(Long portfolioId) {
        return em.createQuery("SELECT t FROM Transakcija t WHERE t.senderId = :portfolioId OR t.receiverId = :portfolioId", Transakcija.class)
                .setParameter("portfolioId", portfolioId)
                .getResultList();
    }
}
