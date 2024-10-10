package com.kkimleang.authservice.model;

import com.kkimleang.authservice.enumeration.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Table(name = "_users", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"}, name = "unq_email")})
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "verification_code")
    private String verificationCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Token> tokens;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(name = "_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
