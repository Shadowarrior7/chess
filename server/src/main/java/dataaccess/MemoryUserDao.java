package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryUserDao {
    Collection<UserData> users;

    //dataAccess
    public MemoryUserDao(){
        users = new ArrayList<UserData>();
    }
    public  UserData getUser(String username){
        for (UserData user: users){
            if (user.username().equals(username)){
                //System.out.println("user found" + user+":");
                return user;
            }
        }
        System.out.println("error finding user");
        return null;
    }

    public void addUser(String username, String password, String email){
        users.add(new UserData(username, password, email));
    }

    public String getUserPassword(String username){
        for (UserData user: users){
            if (user.username().equals(username)){
                return user.password();
            }
        }
        return null;
    }

    public void clear(){
        users.clear();
    }
}
