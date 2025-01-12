package si.fri.prpo.finance.zrna;

import si.fri.prpo.finance.entitete.User;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Arrays;

@ApplicationScoped
@Transactional
public class UserService {

    @PersistenceContext(unitName = "kumuluzee-samples-jpa")
    private EntityManager em;

    public User getUser(Long userId) {
        return em.find(User.class, userId);
    }

    public List<User> getUsers() {
        return em.createNamedQuery("User.findUsers", User.class)
                .getResultList();
    }

    @Transactional
    public void addUser(User user) {
        // Validate all required fields
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Check if username already exists
        User existing = findByUsername(user.getUsername());
        if (existing != null) {
            throw new IllegalStateException("Username already exists");
        }

        // Validate email format
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        em.persist(user);
        em.flush();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public User findByUsername(String username) {
        try {
            return em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = em.find(User.class, id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        return em.merge(existingUser);
    }

    @Transactional
    public boolean deleteUser(Long id) {
        User existingUser = em.find(User.class, id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        em.remove(existingUser);
        return true;
    }

    @Transactional
    public User loginUser(String username, String password) {
        User user = findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    @Transactional
    public List<User> paginateUsers(int page, int size) {
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Transactional
    public int countUsers() {
        TypedQuery<Long> query = em.createNamedQuery("User.countUsers", Long.class);
        return query.getSingleResult().intValue();
    }

    @Transactional
    public List<User> searchUsers(String query) {
        return em.createNamedQuery("User.searchUsers", User.class)
                .setParameter("query", query)
                .getResultList();
    }

    @Transactional
    public List<User> sortUsers(String sortField) {
        if (!isValidSortField(sortField)) {
            throw new IllegalArgumentException("Invalid sort field");
        }
        String queryString = "SELECT u FROM User u ORDER BY u." + sortField;
        return em.createQuery(queryString, User.class).getResultList();
    }

    private boolean isValidSortField(String sortField) {
        return Arrays.asList("username", "firstName", "lastName", "email").contains(sortField);
    }

    @Transactional
    public List<User> filterUsers(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            throw new IllegalArgumentException("Filter parameter cannot be null or empty.");
        }
        return em.createNamedQuery("User.filterUsers", User.class)
                .setParameter("filter", "%" + filter + "%")
                .getResultList();
    }
}
