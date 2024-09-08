package com.kkimleang.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
@ToString
@Entity
@Table(name = "_permission", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unq_name")})
public class Permission implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;


    public Permission() {}
    public Permission(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}
