package com.nhnacademy.bookstore.entity.auth;

import com.nhnacademy.bookstore.entity.memberAuth.MemberAuth;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 1, max = 50)
    private String name;

    @OneToMany(mappedBy = "auth",cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberAuth> memberAuthSet;

}
