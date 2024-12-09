package websocket.messages;

import chess.ChessGame;
import model.GameData;

public class LoadGame extends ServerMessage{
    private GameData game;
    public LoadGame(ServerMessageType type, GameData game1) {
        super(type);
        game = game1;
    }

    public GameData getGame(){
        return game;
    }
}
