package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class SQLAuthDao {

    public SQLAuthDao(){

    }
    //the functions here are the dataAccess layer
    public static String generateToken(){
        return UUID.randomUUID().toString();
    }
    public String addAuthData(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection() ) {
            String statement = "INSERT INTO auth (username, token) VALUES (?, ?)";
            try (var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, username);
                String token = generateToken();
                preparedStatement.setString(2, token);

                preparedStatement.executeUpdate(); // tells db that this can be excuted
                return token;
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    public void deleteAuthData(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            String statement = "SELECT * FROM auth WHERE token = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, authToken);
                var result = preparedStatement.executeQuery();
                if(!result.next()){
                    return;
                }
                statement = "DELETE FROM auth WHERE token = ?";
                var preparedStatement2 = conn.prepareStatement(statement);
                preparedStatement2.setString(1, authToken);
                preparedStatement2.executeUpdate();
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    public AuthData getAuthData(String authToken) throws DataAccessException {
        String username = null;
        String token = null;
        try (var conn = DatabaseManager.getConnection()){
            String statement = "SELECT * FROM auth WHERE token = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, authToken);
                var result = preparedStatement.executeQuery();
                if(!result.next()){
                    return null;
                }
                username = result.getString("username");
                token = result.getString("token");
                return new AuthData(token, username);
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            String statement = "DELETE FROM auth";
            var preStatement = conn.prepareStatement(statement);
            preStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }
}
