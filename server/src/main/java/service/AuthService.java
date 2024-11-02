package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDao;
import dataaccess.SQLAuthDao;
import model.AuthData;

public class AuthService {
    public SQLAuthDao authDao;

    public AuthService(){
        authDao = new SQLAuthDao();
    }

    public SQLAuthDao getAuthDao(){
        return authDao;
    }

    public void setAuthDao(SQLAuthDao newAuth){
        authDao = newAuth;
    }
     public AuthData addAuth(String username) throws DataAccessException {
        var token = authDao.addAuthData(username);
        return authDao.getAuthData(token);
     }

     public AuthData getAuthenByToken(String token) throws DataAccessException {
        return authDao.getAuthData(token);
     }

     public void deleteAuthData(AuthData data) throws DataAccessException {
        authDao.deleteAuthData(data.authToken());
     }

     public void clear() throws DataAccessException {
        authDao.clear();
     }


}
