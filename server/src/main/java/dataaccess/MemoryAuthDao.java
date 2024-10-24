package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MemoryAuthDao {
    private Collection<AuthData> authData;

    public MemoryAuthDao(){
        authData = new ArrayList<AuthData>();
    }
    //the functions here are the dataAccess layer
    public static String generateToken(){
        return UUID.randomUUID().toString();
    }
    public String addAuthData(String username){
        var authToken = generateToken();
        int userFound = 0;
        if(userFound == 0){
            authData.add(new AuthData(authToken, username));
        }
        return authToken;
    }

    public void deleteAuthData(String authToken){
        for (AuthData auth: authData){
            if (auth.authToken().equals(authToken)){
                authData.remove(auth);
                break;
            }
        }
    }

    public AuthData getAuthData(String authToken){
        for (AuthData auth: authData){
            if (auth.authToken().equals(authToken)){
                return auth;
            }
        }
        return null;
    }

    public void clear(){
        authData.clear();
    }
}
