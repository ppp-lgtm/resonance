package com.zhangmy.resonance.modules.repo;

import com.zhangmy.resonance.modules.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);

    /** 管理员表总记录数（单账号机制：0=未初始化，1=已存在） */
    long count();
}
