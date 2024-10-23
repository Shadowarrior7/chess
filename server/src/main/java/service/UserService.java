package service;

import model.UserData;
import dataaccess.MemoryUserDao;

import java.util.Collection;

public class UserService {
    private MemoryUserDao usersDao;

    public UserService(){
        usersDao = new MemoryUserDao();
    }

    MemoryUserDao getUsersDao(){
        return usersDao;
    }

    void updateUsersDao(MemoryUserDao users){
        usersDao = users;
    }
    public void registerUser(UserData registerRequest){
        usersDao.addUser(registerRequest.username(), registerRequest.password(), registerRequest.email());
    }

    public UserData getUser(String username){
        return usersDao.getUser(username);
    }
}
