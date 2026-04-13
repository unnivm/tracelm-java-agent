package org.usbtechno.collector;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserAccount extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be 120 characters or fewer")
    @Column(nullable = false, length = 120)
    public String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 190, message = "email must be 190 characters or fewer")
    @Column(nullable = false, unique = true, length = 190)
    public String email;

    @Column(name = "password_hash", nullable = false, length = 512)
    public String passwordHash;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @PrePersist
    void prePersist() {
        email = email == null ? null : email.trim().toLowerCase();
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
