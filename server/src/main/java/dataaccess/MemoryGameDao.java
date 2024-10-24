package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameDao {
    Collection<GameData> games;

    public MemoryGameDao(){
        games = new ArrayList<GameData>();
    }
    //DataAccess
    public Collection<GameData> getGameDataList(){
        System.out.println("games in mem:" + games);
        return games;
    }

    public int createGame(GameData newGame){
        games.add(newGame);
        return newGame.gameID();
    }

    public GameData getGame(int gameID){
        for (GameData game: games){
            if (game.gameID() == gameID){
                return game;
            }
        }
        return null;
    }

    public void updateGame(GameData updatedGame, GameData oldGame){
        for (GameData game: games){
            if (game.equals(oldGame)){
                games.remove(game);
                games.add(updatedGame);
            }
        }
    }


    public void clear(){
        games.clear();
    }
}
