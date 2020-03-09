package com.xdc.wxshopdemo.service;

import com.xdc.wxshopdemo.UserDao;
import com.xdc.wxshopdemo.generate.User;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUserIfNotExist(String tel) {
        User user = new User();
        user.setTel(tel);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        try {
            userDao.insertUser(user);
        } catch (PersistenceException e) {
            return userDao.getUserByTel(tel);
        }

        return user;
    }
}
