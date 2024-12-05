package websocket.messages;

import chess.ChessGame;

public class LoadGame extends ServerMessage{
    public LoadGame(ServerMessageType type, ChessGame game) {
        super(type);
    }
}
