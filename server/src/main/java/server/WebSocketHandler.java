package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
//import model.MakeMoveModel;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.AuthService;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.MakeMove;

//import javax.websocket.OnMessage;
import java.util.Set;

@WebSocket
public class WebSocketHandler {
    private AuthService authService;
    private GameService gameService;
    private WebSocketSession webSocketSession;
    public WebSocketHandler(AuthService authService2, GameService gameService2, WebSocketSession webSocketSession2){
        webSocketSession = webSocketSession2;
        authService = authService2;
        gameService = gameService2;

    }

    @OnWebSocketConnect
    public void onConnect(Session session){
        webSocketSession.addSessionToGame(0,session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg){
        try{
            Gson serializer = new Gson();
            UserGameCommand command = serializer.fromJson(msg, UserGameCommand.class);
            //validate auth first
            if(authService.getAuthenByToken(command.getAuthToken()) == null){
                Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                        "Error, the users auth token is bad"));
                session.getRemote().sendString(serializer.toJson(error));
                return;
            }
            String username = authService.getAuthenByToken(command.getAuthToken()).username();
            webSocketSession.addSessionToGame(command.getGameID(), session);

            switch (command.getCommandType()){
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, serializer.fromJson(msg, MakeMove.class));
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resignGame(session, username, command);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //WS service
    public void connect(Session session, String username, UserGameCommand command) throws Exception {
        Gson serializer = new Gson();
        String color;
        AuthData authData = authService.getAuthenByToken(command.getAuthToken());
        if(authData == null){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, the users auth token is bad"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        GameData gameData = gameService.getGame(command.getGameID().toString());
        if(gameData == null){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, the game you are trying to join does not exist"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        if(gameData.game().gameOver()){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, this game is over"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        if(gameData.whiteUsername() !=null && gameData.whiteUsername().equals(username)){
            color = "white";
        }
        else if (gameData.blackUsername() !=null && gameData.blackUsername().equals(username)){
            color = "black";
        }
        else{
            color = "observer";
        }
        String messageSending = username + " joined as " + color;
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageSending);
        String messageSerialized = serializer.toJson(notification);

        sendMessage(session, serializer.toJson(loadGame));
        broadcastMessage(command.getGameID(), messageSerialized, session);

    }
    public void makeMove(Session session, String username, MakeMove command) throws Exception {
        Gson serializer = new Gson();
        AuthData authData = authService.getAuthenByToken(command.getAuthToken());
        if(authData == null){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, the users auth token is bad"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        GameData gameData = gameService.getGame(command.getGameID().toString());
        if(gameData == null){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, the game you are trying to join does not exist"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        System.out.println("making a move");
        if(gameData.game().gameOver()){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, the game is over, please leave, you are getting annoying"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        if(isObserver(command.getGameID(), username)){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, you are an observer"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        GameData newGame = null;

        String turn = gameData.game().getTeamTurn().toString().toLowerCase();
        //System.out.println("the turn color is: " + turn);

        String userColor = getUserColor(gameData, username);
        if(!turn.equals(userColor)){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String("Error, not your piece"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }

        try {
            ChessGame game = new ChessGame();
            game.setTeamTurn(gameData.game().getTeamTurn());
            System.out.println("game turn: " + gameData.game().getTeamTurn());
            //game.setBoard(gameData.game().copyBoard(gameData.game().getBoard()));
            game.getBoard().setSquares(gameData.game().copyBoard(gameData.game().getBoard()));
            newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game);
            newGame.game().makeMove(command.move());
            System.out.println("new game turn: "+ newGame.game().getTeamTurn());
        } catch(Exception e){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error: " + e.getMessage()));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        if (newGame.game().isInCheckmate(ChessGame.TeamColor.BLACK)){
            Notification notfiy = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, getUsername(gameData, "black") + " is in check mate");
            broadcastMessage(command.getGameID(), serializer.toJson(notfiy), session);
            sendMessage(session, serializer.toJson(notfiy));
            newGame.game().setGameOver(true);
        } else if (newGame.game().isInCheck(ChessGame.TeamColor.BLACK)){
            Notification notfiy = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, getUsername(gameData, "black") + " is in check");
            broadcastMessage(command.getGameID(), serializer.toJson(notfiy), session);
            sendMessage(session, serializer.toJson(notfiy));
        }
        if (newGame.game().isInCheckmate(ChessGame.TeamColor.WHITE)){
            Notification notfiy = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, getUsername(gameData, "white") + " is in check mate");
            broadcastMessage(command.getGameID(), serializer.toJson(notfiy), session);
            sendMessage(session, serializer.toJson(notfiy));
            newGame.game().setGameOver(true);
        }else if (newGame.game().isInCheck(ChessGame.TeamColor.WHITE)){
            Notification notfiy = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, getUsername(gameData, "white") + " is in check");
            sendMessage(session, serializer.toJson(notfiy));
            broadcastMessage(command.getGameID(), serializer.toJson(notfiy), session);

        }
        gameService.updateGame(newGame, gameData);
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, newGame);
        sendMessage(session, serializer.toJson(loadGame));
        broadcastMessage(command.getGameID(), serializer.toJson(loadGame), session);

        String msg = username + " made a move: " + convertToChessNotation(command.move().getStartPosition())
                + " to " + convertToChessNotation(command.move().getEndPosition());
        Notification move = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, msg);
        broadcastMessage(command.getGameID(), serializer.toJson(move), session);
        System.out.println("move made");
    }

    public static String getUsername(GameData gameData, String color){
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();
        color = color.toLowerCase();
        if (color.equals("white")){
            return white;
        }
        if (color.equals("black")){
            return black;
        }
        return null;
    }

    public static String convertToChessNotation(ChessPosition position) {
        int row = position.getRow();
        int column = position.getColumn();
        if (row < 1 || row > 8 || column < 1 || column > 8) {
            return null;
        }
        char columnLetter = (char) ('a' + (column - 1));
        return String.valueOf(columnLetter) + row;
    }

    public void leaveGame(Session session, String username, UserGameCommand command) throws Exception{
        Gson serializer = new Gson();
        AuthData authData = authService.getAuthenByToken(command.getAuthToken());
        if(authData == null){
            System.out.println("user cannot connect leave the game because their authdata is incorrect");
            return;
        }
        GameData gameData = gameService.getGame(command.getGameID().toString());
        if(gameData == null){
            System.out.println("the game you are trying to leave does not exist");
        }
        webSocketSession.removeSessionFromGame(command.getGameID(), session);

        //finds game with username
        GameData oldGame = gameService.getGame(String.valueOf(command.getGameID()));
        GameData newGame = null;
        if(oldGame.blackUsername() != null && oldGame.blackUsername().equals(username)){
            ChessGame game = new ChessGame();
            game.setBoard(gameData.game().getBoard());
            game.setTeamTurn(oldGame.game().getTeamTurn());
            newGame = new GameData(command.getGameID(), gameData.whiteUsername(), null,
                    gameData.gameName(), game);
            gameService.updateGame(newGame, oldGame);
            webSocketSession.removeSessionFromGame(command.getGameID(), session);
        }
        else if(oldGame.whiteUsername() != null && oldGame.whiteUsername().equals(username)){
            ChessGame game = new ChessGame();
            game.setBoard(gameData.game().getBoard());
            game.setTeamTurn(oldGame.game().getTeamTurn());
            newGame = new GameData(command.getGameID(), null, gameData.blackUsername(),
                    gameData.gameName(), game);
            gameService.updateGame(newGame, oldGame);
            webSocketSession.removeSessionFromGame(command.getGameID(), session);
        }
        else {
            webSocketSession.removeSessionFromGame(command.getGameID(), session);
        }

        String messageSending = username + " has left the game.";
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageSending);
        String messageSerialized = serializer.toJson(notification);

        broadcastMessage(command.getGameID(), messageSerialized, session);

    }
    public void resignGame(Session session, String username, UserGameCommand command) throws Exception{
        System.out.println("resigned game");
        Gson serializer = new Gson();
        AuthData authData = authService.getAuthenByToken(command.getAuthToken());
        if(authData == null){
            System.out.println("user cannot resign because their authdata is incorrect");
            return;
        }
        GameData gameData = gameService.getGame(command.getGameID().toString());
        if(gameData == null){
            System.out.println("the game you are trying to resign from does not exist");
        }
        if(gameData.game().gameOver()){
            System.out.println("game over");
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, the game is over"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        if(isObserver(command.getGameID(), username)){
            Error error = new Error(ServerMessage.ServerMessageType.ERROR, new String(
                    "Error, you are an observer"));
            session.getRemote().sendString(serializer.toJson(error));
            return;
        }
        GameData oldGame = gameData;
        ChessGame game = new ChessGame();
        game.setBoard(gameData.game().getBoard());
        game.setTeamTurn(oldGame.game().getTeamTurn());
        game.setGameOver(true);
        GameData newGame = new GameData(command.getGameID(), gameData.whiteUsername(), gameData.blackUsername(),
                gameData.gameName(), game);
        gameService.updateGame(newGame, oldGame);

        String messageSending = username + " has resigned.";

        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageSending);
        String messageSerialized = serializer.toJson(notification);

        sendMessage(session, messageSerialized);
        broadcastMessage(command.getGameID(), messageSerialized, session);

    }

    public void sendMessage(Session session, String message) throws Exception{
        session.getRemote().sendString(message);
    }

    public void broadcastMessage(int gameID, String message, Session dontSend) throws Exception{
        Set<Session> sessions = webSocketSession.getSessionForGame(gameID);
        for(Session session : sessions){
            if (!session.equals(dontSend)){
                session.getRemote().sendString(message);
            }
        }
    }

    public boolean isObserver(int gameID, String username) throws Exception{
        GameData game = gameService.getGame(String.valueOf(gameID));
        if (game.blackUsername() == null){
            if (game.whiteUsername().equals(username)){
                return false;
            }
        }
        if (game.whiteUsername() == null){
            if (game.blackUsername().equals(username)){
                return false;
            }
        }
        if(game.blackUsername().equals(username) || game.whiteUsername().equals(username)){
            return false;
        }
        System.out.println("user is an observer");
        return true;
    }

    public String getUserColor(GameData game, String username){
        if(game.whiteUsername() !=null && game.whiteUsername().equals(username)){
            return "white";
        }
        if(game.blackUsername() !=null && game.blackUsername().equals(username)){
            return "black";
        }
        return null;
    }
}
