package server;

import com.google.gson.JsonNull;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        //make a db endpoint
        Spark.delete("/db", (request, response) -> {
            response.type("application/json");
            try {
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(401);
                return "{error:Error processing DELETE request: " + e.getMessage() + "}";
            }
        });

        Spark.post("/user", (request, response) ->{
            response.type("application/json");
            try{
                response.status(200);
                return "{}";
            }catch (Exception e){
                response.status(401);
                return "{error:Error processing POST request: " + e.getMessage() + "}";
            }
        });

        Spark.post("/session", (request, response) -> {
            response.type("application/json");
            try {
                response.status(200);
                return "{}";
            } catch (Exception e) {
                response.status(401);
                return "{error:Error processing POST request: " + e.getMessage() + "}";
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
}
