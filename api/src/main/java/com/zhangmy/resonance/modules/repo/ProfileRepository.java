package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByAdminId(Long adminId);
}
