package com.tuliomssantos.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "CustomUser")
public class UserModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "UserID")
    private UUID id;

    @Column(name = "Name")
    private String name;

    @NotNull(message = "Email cannot be null")
    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "Source")
    @Enumerated(EnumType.STRING)
    private RegistrationSource source;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public RegistrationSource getSource() {
        return source;
    }

    public void setSource(RegistrationSource source) {
        this.source = source;
    }


}
