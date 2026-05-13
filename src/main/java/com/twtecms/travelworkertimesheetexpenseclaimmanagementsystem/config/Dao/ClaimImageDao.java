package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimImageDao extends JpaRepository<ClaimImage, Long> {
    List<ClaimImage> findByClaimId(Long claimId);

    Optional<ClaimImage> findByClaimIdAndImageId(Long claimId, Long imageId);
}
