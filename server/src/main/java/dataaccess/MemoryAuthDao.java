package dataaccess;

import model.AuthData;

import java.util.Collection;
import java.util.UUID;

public class MemoryAuthDao {
    private Collection<AuthData> authData;

    //the functions here are the dataAccess layer
    public static String generateToken(){
        return UUID.randomUUID().toString();
    }
    void addAuthData(String username){
        var authToken = generateToken();
        int userFound = 0;
        for (AuthData auth: authData){
            if (auth.username().equals(username)){
                authData.remove(auth);
                authData.add(new AuthData(authToken, username));
                userFound = 1;
                break;
            }
        }
        if(userFound == 0){
            authData.add(new AuthData(authToken, username));
        }
    }

    void deleteAuthData(String authToken){
        for (AuthData auth: authData){
            if (auth.authToken().equals(authToken)){
                authData.remove(auth);
                break;
            }
        }
    }

    AuthData getAuthDate(String authToken){
        for (AuthData auth: authData){
            if (auth.authToken().equals(authToken)){
                return auth;
            }
        }
        return null;
    }

    void clear(){
        authData.clear();
    }
}
