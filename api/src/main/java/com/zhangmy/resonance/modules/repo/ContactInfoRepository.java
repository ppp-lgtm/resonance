package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
    List<ContactInfo> findByAdminIdOrderBySortOrderAscIdAsc(Long adminId);

    List<ContactInfo> findByAdminIdAndVisibleTrueOrderBySortOrderAscIdAsc(Long adminId);
}
