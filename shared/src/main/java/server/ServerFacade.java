package server;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.*;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.io.*;
import java.net.*;
import java.util.Collection;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url){
        serverUrl = url;
    }

    public AuthData register(UserData user) throws Exception {
        var path = "/user";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

    public AuthData login(UserData user) throws Exception {
        var path = "/session";
        return this.makeRequest("POST", path, user, AuthData.class, null);
    }

    public void logout(String token) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, token, null, token);
    }

    public Collection<GameData> listGames(String token) throws Exception {
        var path = "/game";
        var json = this.makeRequest("GET", path, null, JsonObject.class, token);

        var json2 = json.getAsJsonArray("games");
        Type gameDataType = new TypeToken<Collection<GameData>>() {}.getType();
        return new Gson().fromJson(json2, gameDataType);
    }

    public String createGame(String token, GameData name) throws Exception {
        var path = "/game";
        var id = this.makeRequest("POST", path, name, JsonObject.class, token);
        return id.toString();
    }

    public void joinGame(String token, JoinGame game) throws Exception {
        var path = "/game";
        this.makeRequest("PUT", path, game, null, token);
    }

//    public void makeMove(String token, GameData oldGame, GameData newGame) throws Exception {
//        var path = "/makeMove";
//        MakeMoveModel move = new MakeMoveModel(oldGame, newGame);
//        this.makeRequest("PUT", path, move, null, token);
//    }
    private  <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String token) throws Exception {
        try{
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            if (token != null){
                http.setRequestProperty("Authorization", token);
            }
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            var status = http.getResponseCode();
            if(!(status == 200)){
                //System.out.println("Error: " + status);
                throw new Exception(String.valueOf(status));
            }
            return readBody(http, responseClass);
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException{
        if (request != null){
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()){
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException{
        T response = null;
        if (http.getContentLength() < 0){
            try (InputStream respBody = http.getInputStream()){
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null){
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
}
