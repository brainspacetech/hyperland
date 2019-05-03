package com.brainspace.hyperland.authserver;

import com.brainspace.hyperland.authserver.model.User;

import java.util.List;

public interface UserService {

    User save(User user);
    List<User> findAll();
    void delete(long id);
}