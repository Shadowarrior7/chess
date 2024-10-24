package service;

import dataaccess.MemoryAuthDao;
import model.AuthData;

public class AuthService {
    public MemoryAuthDao authDao;

    public AuthService(){
        authDao = new MemoryAuthDao();
    }

    public MemoryAuthDao getAuthDao(){
        return authDao;
    }

    public void setAuthDao(MemoryAuthDao newAuth){
        authDao = newAuth;
    }
     public AuthData addAuth(String username){
        var token = authDao.addAuthData(username);
        return authDao.getAuthData(token);
     }

     public AuthData getAuthenByToken(String token){
        return authDao.getAuthData(token);
     }

     public void deleteAuthData(AuthData data){
        authDao.deleteAuthData(data.authToken());
     }

     public void clear(){
        authDao.clear();
     }


}
