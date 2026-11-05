package com.example.starter.user.entity;

import com.example.starter.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Fine-grained authority, e.g. PRODUCT_READ. Used in {@code @PreAuthorize("hasAuthority('PRODUCT_READ')")}. */
@Getter
@Setter
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 255)
    private String description;
}
