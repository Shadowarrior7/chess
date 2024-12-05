package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMove extends UserGameCommand{
    public MakeMove(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        super(commandType, authToken, gameID);
    }
}
