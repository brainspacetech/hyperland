package com.brainspace.hyperland.authserver;

import com.brainspace.hyperland.authserver.dao.UserDAO;
import com.brainspace.hyperland.authserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private UserDAO userDao;

    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userDao.findByUsername(userId);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), getAuthority(userId));
    }

    private List<SimpleGrantedAuthority> getAuthority(String userId) {
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        List roleList = userDao.findRoleByUsername(userId);

        for (Object roleObj : roleList) {
            Map roleMap = (Map)roleObj;
            list.add(new SimpleGrantedAuthority( (String)roleMap.get("role")));
        }


        return list;
    }

    public List<User> findAll() {
       /* List<User> list = new ArrayList<>();
        userDao.findAll().iterator().forEachRemaining(list::add);
        return list;*/
        return null;
    }

    @Override
    public void delete(long id) {
        // userDao.delete(id);
    }

    @Override
    public User save(User user) {
        //return userDao.save(user);
        return null;
    }
}