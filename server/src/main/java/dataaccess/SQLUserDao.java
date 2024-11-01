package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class SQLUserDao {

    public SQLUserDao() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
    }

    public  UserData getUser(String username){
        for (UserData user: users){
            if (user.username().equals(username)){
                System.out.println("user found" + user+":");
                return user;
            }
        }
        System.out.println("error finding user");
        return null;
    }

    public void addUser(String username, String password, String email) throws DataAccessException {
        //user.add(new UserData(username, password, email));
        try (var conn = DatabaseManager.getConnection() ) {
            String statement = "INSERT INTO users (username, password, email) VALUES (?, ? ,?)";
            try (var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, username);
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.setString(3, email);

                preparedStatement.executeUpdate(); // tells db that this can be excuted
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    public String getUserPassword(String username) throws DataAccessException {
        String returnVal = null;
        try (var conn = DatabaseManager.getConnection()){
            String statement = "USE chess";
            var preStatement = conn.prepareStatement(statement);
            preStatement.execute();

            statement = "SELECT * FROM users WHERE username = ?";
            try(var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, username);
                var result = preparedStatement.executeQuery();
                returnVal = result.getString("username");
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return returnVal;
    }

    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            String statement = "DELETE FROM users";
            var preStatement = conn.prepareStatement(statement);
            preStatement.execute();
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }
}
