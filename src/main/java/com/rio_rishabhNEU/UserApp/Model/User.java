package com.rio_rishabhNEU.UserApp.Model;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import java.time.LocalDateTime;
//
//@Data
//@Entity
//@Table(name = "Users", uniqueConstraints = {@UniqueConstraint(columnNames = "email"),})
//@EntityListeners(AuditingEntityListener.class)
//@NoArgsConstructor
//public class UserModel {
//
//
//    @Id
//    @Column(unique = true, nullable = false)
//    private String email;
//
//    @Column(unique = true, nullable = false)
//    private String password;
//
//    @Column(unique = true, nullable = false)
//    private String firstName;
//
//    @Column(unique = true, nullable = false)
//    private String lastName;
//
//
//    @CreatedDate
//    @Column(unique = true, nullable = false)
//    private LocalDateTime accountCreated;
//
//
//    @LastModifiedDate
//    @Column(nullable = false)
//    private LocalDateTime accountUpdated;
//
//    }

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    private UUID id;

    @Column(name = "first_name")
    @JsonProperty("first_name")
    private String firstName;

    @Column(name = "last_name")
    @JsonProperty("last_name")
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "account_created")
    @JsonProperty("account_created")
    private LocalDateTime accountCreated;

    @Column(name = "account_updated")
    @JsonProperty("account_updated")
    private LocalDateTime accountUpdated;
    }