package com.lagou.dao;

import com.lagou.pojo.User;

import java.util.List;

public interface IUserDao {

    //查询所有用户
    public List<User> findAll() throws Exception;


    //根据条件进行用户查询
    public User findByCondition(User user) throws Exception;

    //增加用户
    public void add(User user);
    //更新用户
    public void update(User user);
    //删除用户
    public void delete(User user);
}
