package server;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import model.AuthData;
import model.UserData;
import passoff.model.TestJoinRequest;
import service.AuthService;
import service.UserService;
import spark.*;
import service.UserService.*;
import service.GameService;

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
                System.out.println(result);
                return result;
            }catch (GenericException e){
                response.status(e.code);
                return serializer.toJson(e);
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
                var json = serializer.toJson(e);
                System.out.println(json);
                return json;
            }
        });

        Spark.delete("/session", (request, response) -> { //LOGOUT
            response.type("application/json");
            try {
                AuthData logoutRequest = serializer.fromJson(request.body(), AuthData.class);
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
            }
        });

        Spark.get("/game", (request, response) -> { //LIST GAMES
            response.type("application/json");
            try {
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
            }
        });

        Spark.post("/game", (request, response) -> { //CREATE GAME
            response.type("application/json");
            try {
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
            }
        });

        Spark.put("/game", (request, response) -> { //JOIN GAME
            response.type("application/json");
            try {
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
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
        if (user != null){
            throw new GenericException("Error: already taken", 403);
        }
        userService.registerUser(registerRequest);
        AuthData result = authService.addAuth(registerRequest.username());
        System.out.println(result);
        return serializer.toJson(result);
    }

    public String login(String username, String password) {
        var serializer = new Gson();
        UserData user = userService.getUser(username);
        if (user == null){
            System.out.println("user not found");
            throw new GenericException("Error: user not found", 500);
        }
        if(!password.equals(userService.getPassword(username))){
            System.out.println("password does not match");
            throw new GenericException("Error: unauthorized", 401);
        }

        return serializer.toJson(authService.addAuth(username));

    }

    public void clear(){
        userService.clear();
        gameService.clear();
        authService.clear();
    }
}
