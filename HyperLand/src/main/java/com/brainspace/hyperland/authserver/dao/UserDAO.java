package com.brainspace.hyperland.authserver.dao;


import com.brainspace.hyperland.authserver.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDAO  {
    User findByUsername(String username);
    public List findRoleByUsername(String username);
}