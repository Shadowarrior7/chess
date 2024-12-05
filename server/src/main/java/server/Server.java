package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;
import model.GameData;
import model.UserData;
import model.JoinGame;
import model.makeMove;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.mindrot.jbcrypt.BCrypt;
import service.AuthService;
import service.UserService;
import spark.*;
import service.GameService;
import websocket.commands.Connect;
import websocket.commands.UserGameCommand;
import org.eclipse.jetty.websocket.api.Session;


import java.util.Collection;
import java.util.Map;

public class Server {
    private UserService userService;
    private GameService gameService;
    private AuthService authService;
    private WebSocketSession webSocketSession;
    private WebSocketHandler webSocketHandler;

    public Server() {
        userService = new UserService();
        gameService = new GameService();
        authService = new AuthService();
        webSocketSession = new WebSocketSession();
        webSocketHandler = new WebSocketHandler(authService, gameService, webSocketSession);

    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        //make a db endpoint
        var serializer = new Gson();
        sparkDelete();

        sparkPost(serializer);

        sparkPost2(serializer);

        sparkDelete(serializer);

        Spark.webSocket("/ws", webSocketHandler);

        Spark.get("/game", (request, response) -> { //LIST GAMES
            response.type("application/json");
            try {
                String listGamesRequest = serializer.fromJson(request.headers("Authorization"), String.class);
                Collection<GameData> games = listGames(listGamesRequest);
                System.out.println("games"+ games);
                String gamesJson = serializer.toJson(games);
                String newGames = "{ games: " + gamesJson + "}";
                //String newGames = "{" + gamesJson + "}";
                if (newGames.isEmpty()){
                    response.status(200);
                    System.out.println("newGames is empty");
                    return "{}";
                }
                System.out.println("gamesJson: " + newGames);

                response.status(200);
                return newGames;
            } catch (GenericException e) {
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });

        Spark.post("/game", (request, response) -> { //CREATE GAME
            response.type("application/json");
            try {
                String createGameRequest = serializer.fromJson(request.headers("Authorization"), String.class);
                System.out.println("Auth: " + createGameRequest);
                GameData name = serializer.fromJson(request.body(), GameData.class);
                System.out.println("Creating game: " + name);
                int gameID2 = createGame(createGameRequest, name.gameName());
                //String gameID = String.valueOf(gameID2);
                //var result = serializer.toJson(new GameData(gameID2, "", "", "", new ChessGame()));
                String result = "{ \"gameID\": " + gameID2 + " }";
                System.out.println("\nResult: "+ result);
                response.status(200);
                return result;
            } catch (GenericException e) {
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });

        Spark.put("/game", (request, response) -> { //JOIN GAME
            response.type("application/json");
            try {

                JoinGame joinGameRequest = serializer.fromJson(request.body(), JoinGame.class);
                if(request.headers() == null){
                    response.status(400);
                    return "{ \"message\": \"Error: bad request\" }";
                }
                //String token = serializer.fromJson(request.headers("Authorization"), String.class);
                String token = request.headers("Authorization");

                System.out.println("GameData: "+ joinGameRequest);
                joinGame(joinGameRequest.playerColor(), joinGameRequest.gameID(), token);
                response.status(200);
                return "{}";
            } catch (GenericException e) {
                System.out.println("error here in join game");
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });

        Spark.put("/makeMove", (request, response) -> {
            response.type("application/json");
            try {
                makeMove makeMoveRequest = serializer.fromJson(request.body(), makeMove.class);
                if(request.headers() == null){
                    response.status(400);
                    return "{ \"message\": \"Error: bad request\" }";
                }
                String token = request.headers("Authorization");
                makeMove(token, makeMoveRequest.oldGame(), makeMoveRequest.newGame());
                response.status(200);
                return "{}";
            }
            catch (GenericException e) {
                System.out.println("error when making the move");
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });



        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void sparkDelete(Gson serializer) {
        Spark.delete("/session", (request, response) -> { //LOGOUT
            response.type("application/json");
            try {
                System.out.println("og request: " + request.headers("Authorization"));
                String logoutRequest = serializer.fromJson(request.headers("Authorization"), String.class);
                System.out.println("logout: "+ logoutRequest);
                logout(logoutRequest);
                response.status(200);
                return "{}";
            } catch (GenericException e) {
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void sparkPost2(Gson serializer) {
        Spark.post("/session", (request, response) -> { //LOGIN
            response.type("application/json");
            try {
                UserData loginRequest = serializer.fromJson(request.body(), UserData.class);
                //System.out.println(loginRequest);
                String result = login(loginRequest.username(), loginRequest.password());
                response.status(200);
                System.out.println("success");
                return result;
            } catch (GenericException e) {
                System.out.println("error here in login");
                response.status(e.code);
                System.out.println("error: " + e);
                var json = serializer.toJson(Map.of("message", e.getMessage()));
                System.out.println("login error: "+ json);
                return json;
            }
        });
    }

    private void sparkPost(Gson serializer) {
        Spark.post("/user", (request, response) ->{ //REGISTER NEW USER
            response.type("application/json");
            try{
                UserData registerRequest = serializer.fromJson(request.body(), UserData.class);
                //System.out.println(request.body());
                var result = register(registerRequest);
                response.status(200);
                System.out.println("Register result: "+ result);
                return result;
            }catch (GenericException e){
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void sparkDelete() {
        Spark.delete("/db", (request, response) -> { //CLEAR APPLICATION
            response.type("application/json");
            try {
                clear();
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
            }
        });
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
    public String register(UserData registerRequest) throws GenericException, DataAccessException {
        new DatabaseManager();
        var serializer = new Gson();
        UserData user = userService.getUser(registerRequest.username());
        System.out.println("here" + user);
        if (user != null){
            throw new GenericException("Error: already taken", 403);
        }
        if((registerRequest.username() == null) || (registerRequest.password() == null) || (registerRequest.email() == null)){
            throw new GenericException("Error: Bad Request", 400);

        }
        userService.registerUser(registerRequest);
        AuthData result = authService.addAuth(registerRequest.username());
        System.out.println("Register result 2: "+ result);
        return serializer.toJson(result);
    }

    public String login(String username, String password) throws DataAccessException {
        new DatabaseManager();
        var serializer = new Gson();
        UserData user = userService.getUser(username);
        if (user == null){
            System.out.println("user not found");
            throw new GenericException("Error: user not found", 401);
        }

        String pass = userService.getPassword(username);
        if(!BCrypt.checkpw(password, pass)){
            System.out.println("password does not match");
            throw new GenericException("Error: unauthorized", 401);
        }

        return serializer.toJson(authService.addAuth(username));

    }

    public void logout(String token) throws DataAccessException {
        new DatabaseManager();
        System.out.println("request: "+ token);
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("logout authData: "+ authData);
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        authService.deleteAuthData(authData);
    }

    public Collection<GameData> listGames(String token) throws DataAccessException {
        new DatabaseManager();
        System.out.println("request: "+ token);
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("list games authdata: "+ authData);
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        return gameService.getAllGames();
    }

    public int createGame(String token, String name) throws DataAccessException {
        new DatabaseManager();
        System.out.println("request: "+ token);
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("create games authdata: "+ authData);
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        if(name == null){
            throw new GenericException("Error: bad request", 400);
        }
        int gameID = gameService.createGame(name);
        return gameID;
    }

    public void joinGame(String playerColor, String gameID, String token) throws DataAccessException {
        new DatabaseManager();
        System.out.println("joining game...");
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("got authdata");
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        System.out.println("authData is not null");
        if(gameID == null){
            throw new GenericException("Error: bad request", 400);
        }
        GameData game = gameService.getGame(gameID);
        System.out.println("This is the Game: " + game);
        System.out.println("Trying to join game as: " +playerColor);
        if(playerColor == null){
            throw new GenericException("Error: bad request", 400);
        }
        if (playerColor.equals("WHITE")){
            if (game.whiteUsername() == null || game.whiteUsername().isEmpty()){
                String blackName;
                extra(game, authData);
            }
            else {
                System.out.println("White already taken");
                throw new GenericException("Error: already taken", 403);
            }
        }
        if (playerColor.equals("BLACK")){
            if (game.blackUsername() == null || game.blackUsername().isEmpty()){
                String whiteName;
                if (game.whiteUsername() == null || game.whiteUsername().isEmpty()){
                    whiteName = null;
                }
                else {
                    whiteName = game.whiteUsername();
                }
                gameService.updateGame(new GameData(game.gameID(), whiteName, authData.username(), game.gameName(), game.game()), game);
                System.out.println("joined as black");
            }
            else {
                System.out.println("Black already taken");
                throw new GenericException("Error: already taken", 403);
            }
        }
    }

    public void makeMove(String token, GameData oldGame, GameData newGame) throws DataAccessException {
        new DatabaseManager();
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("got authdata");
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        if(oldGame == null || newGame == null){
            throw new GenericException("Error: bad request", 400);
        }
        newGame.game().changeTurn();
        gameService.updateGame(newGame, oldGame);
        System.out.println("move has been made successfully");
    }

    private void extra(GameData game, AuthData authData) throws DataAccessException {
        String blackName;
        if(game.blackUsername() == null || game.blackUsername().isEmpty()){
            blackName = null;
        }
        else{
            blackName = game.blackUsername();
        }
        gameService.updateGame(new GameData(game.gameID(), authData.username(), blackName, game.gameName(), game.game()), game);
        System.out.println("joined as white");
    }

    public void clear() throws DataAccessException {
        new DatabaseManager();
        userService.clear();
        gameService.clear();
        authService.clear();
    }
}
