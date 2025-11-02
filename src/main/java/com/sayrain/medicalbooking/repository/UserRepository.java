package com.sayrain.medicalbooking.repository;

import com.sayrain.medicalbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 检查用户名是否已存在
    Boolean existsByUsername(String username);
}
