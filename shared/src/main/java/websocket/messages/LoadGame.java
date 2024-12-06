package websocket.messages;

import chess.ChessGame;

public class LoadGame extends ServerMessage{
    private ChessGame game;
    public LoadGame(ServerMessageType type, ChessGame game1) {
        super(type);
        game = game1;
    }
}
