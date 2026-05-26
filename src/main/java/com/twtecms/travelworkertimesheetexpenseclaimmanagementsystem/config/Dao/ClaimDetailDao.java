package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimDetailDao extends JpaRepository<ClaimDetail, Long> {
    List<ClaimDetail> findByClaimId(Long claimId);

    Optional<ClaimDetail> findByClaimIdAndClaimDetailId(Long claimId, Long claimDetailId);

    void deleteByClaimId(Long claimId);
}
