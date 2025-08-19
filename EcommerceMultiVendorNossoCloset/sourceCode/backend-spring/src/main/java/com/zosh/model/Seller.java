package com.zosh.model;

import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sellerName;

    private String mobile;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Embedded
    private BusinessDetails businessDetails = new BusinessDetails();

    @Embedded
    private BankDetails bankDetails = new BankDetails();

    @OneToOne(cascade = CascadeType.ALL)
    private Address pickupAddress = new Address();

    private String GSTIN;

    @Column(columnDefinition = "SMALLINT")
    private USER_ROLE role;

    private boolean isEmailVerified = false;

    @Column(columnDefinition = "SMALLINT")
    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;

}
