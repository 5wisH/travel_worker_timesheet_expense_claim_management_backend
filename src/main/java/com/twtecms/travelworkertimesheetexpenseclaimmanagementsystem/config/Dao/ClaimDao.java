package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimDao extends JpaRepository<Claim, Long> {
    boolean existsByUserIdAndClaimReference(Long userId, String claimReference);

    List<Claim> findByUserIdOrderByClaimDateDesc(Long userId);

    long countByUserId(Long userId);
}
