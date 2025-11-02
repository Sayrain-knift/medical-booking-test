package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.model.User;
import java.util.List;

public interface UserService {
    User getUserById(Long id);
    List<User> getAllUsers();
}