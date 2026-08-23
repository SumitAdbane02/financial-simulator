package com.tradingapp.financialsimulator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
/**
 * Represents a user of the application.
 * This class is a JPA entity, meaning it maps to a table in the database.
 * It also implements Spring Security's UserDetails interface to integrate with the security context.
 */
@Data // A Lombok annotation that bundles @ToString, @EqualsAndHashCode, @Getter, @Setter, and @RequiredArgsConstructor.
@Builder // A Lombok annotation that provides the builder pattern for object creation.
@NoArgsConstructor // Lombok: generates a no-arguments constructor.
@AllArgsConstructor // Lombok: generates a constructor with all arguments.
@Entity // Specifies that this class is a JPA entity and will be mapped to a database table.
@Table(name = "users") // Explicitly names the database table "users". It's a good practice to use plural names for tables and to avoid potential SQL keyword conflicts (e.g., "user").
public class User implements UserDetails {

    @Id // Marks this field as the primary key of the table.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the way the ID is generated. IDENTITY is a good choice for PostgreSQL, as it relies on the database's auto-incrementing column feature.
    private Long id;

    @Column(nullable = false, unique = true) // Defines a column that cannot be null and must have unique values.
    private String username;

    @Column(nullable = false, unique = true) // The user's email must also be unique and non-null.
    private String email;

    @Column(nullable = false) // The password field must not be null. We will store the BCRYPT HASHED password here, never the plain text.
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING) // Tells JPA to persist the enum as a String (e.g., "USER" or "ADMIN") rather than its ordinal value (0 or 1), which is more readable.
    @Column(nullable = false)
    private Role role;
    // highlight-start
    // This is the "non-owning" side of the relationship.
    // 'mappedBy = "user"' tells JPA that the mapping for this relationship
    // is already defined by the 'user' field in the 'Portfolio' class.
    // This prevents JPA from creating a redundant foreign key column in the 'users' table.
    //
    // 'cascade = CascadeType.ALL' is a critical setting. It means that any persistence
    // operation (create, update, delete) performed on a User will be "cascaded"
    // to the associated Portfolio. For example, when we save a new User, its
    // associated Portfolio will be saved automatically.
    //
    // 'fetch = FetchType.LAZY' is a performance optimization. It means the Portfolio
    // data will not be loaded from the database until it is explicitly accessed
    // (e.g., via user.getPortfolio()). This prevents loading unnecessary data.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Portfolio portfolio;

    // --- UserDetails Interface Implementation ---
    // These methods are required by Spring Security to handle authentication and authorization.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This method must return a collection of authorities (roles) granted to the user.
        // We wrap our single 'role' in a list and map it to a SimpleGrantedAuthority, which Spring Security understands.
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        // Spring Security will call this method to get the hashed password for comparison.
        return password;
    }

    @Override
    public String getUsername() {
        // Spring Security uses this as the unique identifier for the user.
        return username;
    }

    // The following methods can be used to implement more fine-grained account control
    // (e.g., locking accounts after failed login attempts or requiring email verification).
    // For now, we will keep them simple and always return 'true'.

    @Override
    public boolean isAccountNonExpired() {
        return true; // Indicates that the user's account has not expired.
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Indicates that the user is not locked out.
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Indicates that the user's credentials (password) have not expired.
    }

    @Override
    public boolean isEnabled() {
        return true; // Indicates that the user's account is enabled.
    }
}
