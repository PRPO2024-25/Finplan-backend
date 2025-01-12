package si.fri.prpo.finance.entitete;

import javax.persistence.*;
import java.io.Serializable;




@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(
                name = "User.findUsers",
                query = "SELECT u FROM User u"
        ),
        @NamedQuery(
                name = "User.findByLastName",
                query = "SELECT u FROM User u WHERE LOWER(u.lastName) = LOWER(:lastName)"
        ),
        @NamedQuery(
                name = "User.search",
                query = "SELECT u FROM User u WHERE " +
                        "LOWER(u.firstName) LIKE LOWER(:searchTerm) OR " +
                        "LOWER(u.lastName) LIKE LOWER(:searchTerm)"
        ),
        @NamedQuery(
                name = "User.findByUsername",
                query = "SELECT u FROM User u WHERE u.username = :username"
        ),
        @NamedQuery(name = "User.countUsers", query = "SELECT COUNT(u) FROM User u"),
        @NamedQuery(name = "User.filterUsers", query = "SELECT u FROM User u WHERE u.username LIKE :filter OR u.firstName LIKE :filter OR u.lastName LIKE :filter")
})
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username")
    private String username;
    
    @Column(name = "firstname")
    private String firstName;
    
    @Column(name = "lastname")
    private String lastName;

    @Column(name = "email")
    private String email;
    
    @Column(name = "password")
    private String password; 
    


    @Override
    public String toString() {
        return "Uporabnik{" +
                "id=" + id +
                ", priimek=" + lastName +
                ", ime='" + firstName + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
    public User() {
    }
}