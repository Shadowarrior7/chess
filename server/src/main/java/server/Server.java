package server;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import model.UserData;
import passoff.model.TestJoinRequest;
import service.UserService;
import spark.*;
import service.UserService.*;
import service.GameService;

public class Server {
    private UserService userService;
    private GameService gameService;

    public Server(){
        userService = new UserService();
        gameService = new GameService();
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
                System.out.println(request.body());
                var result = register(register_request);
                if (result.equals("user already exists")){
                    response.status(403);
                    return "{ \"message\": \"Error: already taken\" }";
                }
                var json = serializer.toJson(result);
                response.status(200);
                return json;
            }catch (Exception e){
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
            }
        });

        Spark.post("/session", (request, response) -> { //LOGIN
            response.type("application/json");
            try {
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(500);
                return "{ message: Error: (" + e.getMessage() + ") }";
            }
        });

        Spark.delete("/session", (request, response) -> { //LOGOUT
            response.type("application/json");
            try {
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
    public String register(UserData registerRequest){
        UserData user = userService.getUser(registerRequest.username());
        if (user != null){
            return "user already exists";
        }
        userService.registerUser(registerRequest);

        return "";
    }
}
