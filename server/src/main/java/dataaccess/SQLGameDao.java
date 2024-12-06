package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDao {

    public SQLGameDao() {

    }
    //DataAccess
    public Collection<GameData> getGameDataList() throws DataAccessException {
        Collection<GameData> gamesList = new ArrayList<GameData>();
        try(var conn = DatabaseManager.getConnection()){
            String statement = "SELECT * FROM games";
            var preStatement = conn.prepareStatement(statement);
            var result = preStatement.executeQuery();
            while(result.next()){
                GameData singleGame = new Gson().fromJson(result.getString("game"), GameData.class);
                gamesList.add(singleGame);
            }
            return gamesList;
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    public int createGame(GameData newGame) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var game2 = new Gson().toJson(newGame, GameData.class);
            String statement = "INSERT INTO games (game) VALUES (?)";
            try (var preparedStatement = conn.prepareStatement(statement)){
                preparedStatement.setString(1, game2);

                preparedStatement.executeUpdate(); // tells db that this can be excuted
            }
        } catch(SQLException e){
            throw new DataAccessException(e.getMessage());
        }
        return newGame.gameID();
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            String statement = "SELECT * FROM games";
            var preStatement = conn.prepareStatement(statement);
            var result = preStatement.executeQuery();
            while(result.next()){
                GameData singleGame = new Gson().fromJson(result.getString("game"), GameData.class);
                if (singleGame.gameID() == gameID){
                    return singleGame;
                }
            }
            return null;
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    public void updateGame(GameData updatedGame, GameData oldGame) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            String updatedGameJson = new Gson().toJson(updatedGame, GameData.class);
            String oldGameJson = new Gson().toJson(oldGame, GameData.class);
            String statement = "SELECT * FROM games WHERE game = ?";
            var preStatement = conn.prepareStatement(statement);
            preStatement.setString(1, oldGameJson);
            var result = preStatement.executeQuery();
            if (!result.next()){
                System.out.println("old game does not exist");
                statement= "INSERT INTO games (game) VALUES (?)";
                var preparedStatement = conn.prepareStatement(statement);
                preparedStatement.setString(1, updatedGameJson);
                preparedStatement.executeUpdate();
                return;
            }
            statement = "DELETE FROM games WHERE game = ?";
            var preparedStatement = conn.prepareStatement(statement);
            preparedStatement.setString(1, oldGameJson);
            preparedStatement.executeUpdate();

            statement= "INSERT INTO games (game) VALUES (?)";
            var preparedStatement2 = conn.prepareStatement(statement);
            preparedStatement2.setString(1, updatedGameJson);
            preparedStatement2.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }


    public void clear() throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()){
            String statement = "DELETE FROM games";
            var preStatement = conn.prepareStatement(statement);
            preStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

}
