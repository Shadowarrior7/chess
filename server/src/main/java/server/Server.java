package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.mysql.cj.conf.ConnectionUrlParser;
import model.AuthData;
import model.GameData;
import model.UserData;
import model.joinGame;
import org.slf4j.helpers.FormattingTuple;
import passoff.model.TestJoinRequest;
import service.AuthService;
import service.UserService;
import spark.*;
import service.UserService.*;
import service.GameService;

import java.io.Reader;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Map;

public class Server {
    private UserService userService;
    private GameService gameService;
    private AuthService authService;

    public Server(){
        userService = new UserService();
        gameService = new GameService();
        authService = new AuthService();
    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        //make a db endpoint
        var serializer = new Gson();
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

        Spark.post("/user", (request, response) ->{ //REGISTER NEW USER
            response.type("application/json");
            try{
                UserData register_request = serializer.fromJson(request.body(), UserData.class);
                //System.out.println(request.body());
                var result = register(register_request);
                response.status(200);
                System.out.println("Register result: "+ result);
                return result;
            }catch (GenericException e){
                response.status(e.code);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });

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

        Spark.get("/game", (request, response) -> { //LIST GAMES
            response.type("application/json");
            try {
                String listGamesRequest = serializer.fromJson(request.headers("Authorization"), String.class);
                Collection<GameData> games = listGames(listGamesRequest);
                System.out.println("games"+ games);
                String games_json = serializer.toJson(games);
                String new_games =games_json.substring(1, games_json.length()-1);
                //String new_games = "{" + games_json + "}";
                if (new_games.isEmpty()){
                    response.status(200);
                    System.out.println("new_games is empty");
                    return "{}";
                }
                System.out.println("games_json: " + new_games);

                response.status(200);
                return new_games;
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
                var result = serializer.toJson(new GameData(gameID2, "", "", "", new ChessGame()));
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

                joinGame joinGameRequest = serializer.fromJson(request.body(), joinGame.class);
                String token = serializer.fromJson(request.headers("Authorization"), String.class);

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



        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
    public String register(UserData registerRequest) throws GenericException {
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

    public String login(String username, String password) {
        var serializer = new Gson();
        UserData user = userService.getUser(username);
        if (user == null){
            System.out.println("user not found");
            throw new GenericException("Error: user not found", 401);
        }
        if(!password.equals(userService.getPassword(username))){
            System.out.println("password does not match");
            throw new GenericException("Error: unauthorized", 401);
        }

        return serializer.toJson(authService.addAuth(username));

    }

    public void logout(String token){
        System.out.println("request: "+ token);
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("logout authData: "+ authData);
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        authService.deleteAuthData(authData);
    }

    public Collection<GameData> listGames(String token){
        System.out.println("request: "+ token);
        AuthData authData = authService.getAuthenByToken(token);
        System.out.println("list games authdata: "+ authData);
        if (authData == null){
            throw new GenericException("Error: Unauthorized", 401);
        }
        return gameService.getAllGames();
    }

    public int createGame(String token, String name){
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

    public void joinGame(String playerColor, String gameID, String token){
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
            if (game.whiteUsername().isEmpty()){
                gameService.updateGame(new GameData(game.gameID(), authData.username(), game.blackUsername(), game.gameName(), game.game()), game);
                System.out.println("joined as white");
            }
            else {
                System.out.println("White already taken");
                throw new GenericException("Error: already taken", 403);
            }
        }
        if (playerColor.equals("BLACK")){
            if (game.blackUsername().isEmpty()){
                gameService.updateGame(new GameData(game.gameID(), game.whiteUsername(), authData.username(), game.gameName(), game.game()), game);
                System.out.println("joined as black");
            }
            else {
                System.out.println("Black already taken");
                throw new GenericException("Error: already taken", 403);
            }
        }
    }
    public void clear(){
        userService.clear();
        gameService.clear();
        authService.clear();
    }
}
