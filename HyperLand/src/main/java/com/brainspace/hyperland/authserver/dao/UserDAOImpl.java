package com.brainspace.hyperland.authserver.dao;

import com.brainspace.hyperland.authserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Transactional
@Repository
public class UserDAOImpl implements  UserDAO{
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public User findByUsername(String username){
        User user = null;
        try {
            String query = "SELECT username ,password from user where username = '"+username+"'";
            System.out.println("this.jdbcTemplate -- " + this.jdbcTemplate);
           List userList = this.jdbcTemplate.queryForList(query);
           for(Object userObj : userList){
               Map userMap = (Map)userObj;
               user = new User ();
               user.setUsername((String)userMap.get("username"));
               user.setPassword((String)userMap.get("password"));
               user.setRoles(new String[]{"ROLE_ADMIN"});
           }
           return user;
        } catch (Exception e) {
            throw e;
        }

    }

    public List findRoleByUsername(String username){

        try {
            String query = "SELECT username ,role from user_roles where username = '"+username+"'";
            System.out.println("findRoleByUsername -- query" + query);
            List roleList = this.jdbcTemplate.queryForList(query);

            return roleList;
        } catch (Exception e) {
            throw e;
        }

    }
}
