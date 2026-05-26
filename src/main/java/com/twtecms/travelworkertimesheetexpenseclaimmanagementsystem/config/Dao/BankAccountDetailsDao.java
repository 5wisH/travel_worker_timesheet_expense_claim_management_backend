package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.BankAccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountDetailsDao extends JpaRepository<BankAccountDetails, Long> {
    Optional<BankAccountDetails> findByClaimId(Long claimId);

    void deleteByClaimId(Long claimId);
}
