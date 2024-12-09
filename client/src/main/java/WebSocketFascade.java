
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import model.GameData;
import ui.EscapeSequences;
import websocket.commands.*;
import websocket.messages.ServerMessage;

//import javax.management.Notification;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import websocket.messages.LoadGame;
import websocket.messages.Error;
import websocket.messages.ServerMessage;
import websocket.messages.Notification;
//import org.eclipse.jetty.websocket.api.Session;

public class WebSocketFascade extends Endpoint {
    Session session;
    static String userName;
    public PrintBoard printer;

    public WebSocketFascade(String url, PrintBoard printer) throws Exception {
        try {
            this.printer=printer;
            userName = null;
            //url = url.replace("http", "ws");
            URI socketURL = new URI(url);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURL);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    //System.out.println("revieved a message");
                   ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    handleMessage(notification, message);
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private void sendMessage(String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }

    private void handleMessage(ServerMessage serverMsg, String message) {
        ServerMessage.ServerMessageType svrMessage = serverMsg.getServerMessageType();
        if (svrMessage.equals(ServerMessage.ServerMessageType.NOTIFICATION)) {
            Notification notify = new Gson().fromJson(message, Notification.class);
            printNotification(notify.getMessage());
        } else if (svrMessage.equals(ServerMessage.ServerMessageType.ERROR)) {
            Error error = new Gson().fromJson(message, Error.class);
            printNotification(error.getMessage());
        } else if (svrMessage.equals(ServerMessage.ServerMessageType.LOAD_GAME)) {
            LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
            printer.printBoard(loadGame.getGame().gameID());
        }
    }

    public void printNotification(String message) {
        System.out.println(message);
    }

    public void makeMove(int gameID, String token, ChessMove move) throws Exception {
        Gson serializer = new Gson();
        MakeMove moveRequest = new MakeMove(UserGameCommand.CommandType.MAKE_MOVE, token, gameID, move);
        sendMessage(serializer.toJson(moveRequest));
    }

    public void connect(String token, int gameID) throws Exception {
        Gson serializer = new Gson();
        Connect connectRequest = new Connect(UserGameCommand.CommandType.CONNECT, token, gameID);
        sendMessage(serializer.toJson(connectRequest));
    }

    public void leave(String token, int gameID) throws Exception {
        Gson serializer = new Gson();
        Leave leaveRequest = new Leave(UserGameCommand.CommandType.LEAVE, token, gameID);
        sendMessage(serializer.toJson(leaveRequest));
    }

    public void resign(String token, int gameID) throws Exception {
        Gson serializer = new Gson();
        Resign resignRequest = new Resign(UserGameCommand.CommandType.RESIGN, token, gameID);
        sendMessage(serializer.toJson(resignRequest));
    }
}



