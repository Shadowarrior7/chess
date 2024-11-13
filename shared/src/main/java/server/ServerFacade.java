package server;

import com.google.gson.Gson;
import model.*;
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
        return this.makeRequest("POST", path, user, AuthData.class );
    }

    public AuthData login(UserData user) throws Exception {
        var path = "/session";
        return this.makeRequest("POST", path, user, AuthData.class);
    }

    public void logout(String token) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, token, null);
    }

    public Collection<GameData> listGames(String token) throws Exception {
        var path = "/game";
        return this.makeRequest("GET", path, token, GameData.class);
    }

    public int createGame(String Token)
    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
        try{
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            var status = http.getResponseCode();
            if(!(status == 200)){
                System.out.println("Error: " + status);
                return null;
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
