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

    void setUsersDao(MemoryUserDao users){
        usersDao = users;
    }
    public void registerUser(UserData registerRequest){
        usersDao.addUser(registerRequest.username(), registerRequest.password(), registerRequest.email());
    }

    public UserData getUser(String username){
        var result = usersDao.getUser(username);
        System.out.println(result);
        return result;
    }

    public String getPassword(String username){
        return usersDao.getUserPassword(username);
    }

    public void clear(){
        usersDao.clear();
    }
}
