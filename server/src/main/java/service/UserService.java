package service;

import dataaccess.DataAccessException;
import dataaccess.SQLUserDao;
import model.UserData;
import dataaccess.MemoryUserDao;

import java.util.Collection;

public class UserService {
    private SQLUserDao usersDao;

    public UserService() {
        usersDao = new SQLUserDao();
    }

    SQLUserDao getUsersDao(){
        return usersDao;
    }

    void setUsersDao(SQLUserDao users){
        usersDao = users;
    }
    public void registerUser(UserData registerRequest) throws DataAccessException {
        usersDao.addUser(registerRequest.username(), registerRequest.password(), registerRequest.email());
    }

    public UserData getUser(String username) throws DataAccessException {
        var result = usersDao.getUser(username);
        //System.out.println(result);
        return result;
    }

    public String getPassword(String username) throws DataAccessException {
        return usersDao.getUserPassword(username);
    }

    public void clear() throws DataAccessException {
        usersDao.clear();
    }
}
