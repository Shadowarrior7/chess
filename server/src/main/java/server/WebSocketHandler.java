package server;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import service.AuthService;
import service.GameService;
import websocket.commands.*;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.Set;


public class WebSocketHandler {
    private AuthService authService;
    private GameService gameService;
    private WebSocketSession webSocketSession;
    public WebSocketHandler(AuthService authService2, GameService gameService2, WebSocketSession webSocketSession2){
        webSocketSession = webSocketSession2;
        authService = authService2;
        gameService = gameService2;

    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg){
        try{
            Gson serializer = new Gson();
            UserGameCommand command = serializer.fromJson(msg, UserGameCommand.class);
            //validate auth first
            String username = authService.getAuthenByToken(command.getAuthToken()).username();
            webSocketSession.addSessionToGame(command.getGameID(), session);

            switch (command.getCommandType()){
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, command);
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resignGame(session, username, command);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session){

    }

    @OnWebSocketClose
    public void onClose(Session session){
        session.close();
    }

    @OnWebSocketError
    public void onError(Throwable throwable){

    }

    //WS service
    public void connect(Session session, String username, UserGameCommand command) throws Exception {
        //Connect message = new Connect(command.getCommandType(), command.getAuthToken(), command.getGameID());
        Gson serializer = new Gson();
        String color;

        AuthData authData = authService.getAuthenByToken(command.getAuthToken());
        if(authData == null){
            System.out.println("user cannot connect to socket because their authdata is incorrect");
            return;
        }
        GameData gameData = gameService.getGame(command.getGameID().toString());
        if(gameData == null){
            System.out.println("the game you are trying to connect to does not exist");
            return;
        }
        if(gameData.whiteUsername().equals(username)){
            color = "white";
        }
        else if (gameData.blackUsername().equals(username)){
            color = "black";
        }
        else{
            color = "observer";
        }
        String messageSending = username + "joined as " + color;
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageSending);
        String messageSerialized = serializer.toJson(notification);

        Set<Session> sessions = webSocketSession.getSessionForGame(command.getGameID());
        for (Session session1: sessions){
            session1.getRemote().sendString(messageSerialized);
            session1.getRemote().sendString(serializer.toJson(loadGame));
        }

    }
    public void makeMove(Session session, String username, UserGameCommand command) throws Exception {
        Gson serializer = new Gson();
        AuthData authData = authService.getAuthenByToken(command.getAuthToken());
        if(authData == null){
            System.out.println("user cannot connect to socket because their authdata is incorrect");
            return;
        }
        GameData gameData = gameService.getGame(command.getGameID().toString());
        if(gameData == null){
            System.out.println("the game you are trying to make a move on does not exist");
            return;
        }
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());

        Set<Session> sessions = webSocketSession.getSessionForGame(command.getGameID());
        for (Session session1: sessions){
            session1.getRemote().sendString(serializer.toJson(loadGame));
        }
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
        LoadGame loadGame = new LoadGame(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());

        String messageSending = username + " has left the game.";
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageSending);
        String messageSerialized = serializer.toJson(notification);

        Set<Session> sessions = webSocketSession.getSessionForGame(command.getGameID());
        for (Session session1: sessions){
            session1.getRemote().sendString(messageSerialized);
            session1.getRemote().sendString(serializer.toJson(loadGame));
        }

    }
    public void resignGame(Session session, String username, UserGameCommand command) throws Exception{
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
        String messageSending = username + " has resigned.";
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, messageSending);
        String messageSerialized = serializer.toJson(notification);

        Set<Session> sessions = webSocketSession.getSessionForGame(command.getGameID());
        for (Session session1: sessions){
            session1.getRemote().sendString(messageSerialized);
        }

    }
}
