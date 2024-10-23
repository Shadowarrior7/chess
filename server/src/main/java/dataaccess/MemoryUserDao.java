package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.Collection;

public class MemoryUserDao {
    Collection<UserData> users;

    //dataAccess
    UserData getUser(String username){
        for (UserData user: users){
            if (user.username().equals(username)){
                return user;
            }
        }
        return null;
    }

    void addUser(String username, String password, String email){
        users.add(new UserData(username, password, email));
    }

    String getUserPassword(String username){
        for (UserData user: users){
            if (user.username().equals(username)){
                return user.password();
            }
        }
        return null;
    }

    void clear(){
        users.clear();
    }
}
