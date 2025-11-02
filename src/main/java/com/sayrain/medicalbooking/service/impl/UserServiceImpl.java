package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.model.User;
import com.sayrain.medicalbooking.repository.UserRepository;
import com.sayrain.medicalbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    // 根据ID查找用户
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
