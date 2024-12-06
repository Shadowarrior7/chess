package server;


import org.eclipse.jetty.websocket.api.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WebSocketSession {
    private Map<Integer, Set<Session>> sessionMap = new HashMap<>();

    public WebSocketSession() {
        this.sessionMap = new HashMap<>();
    }

    public void addSessionToGame(int gameID, Session session){
        Set<Session> sessions = sessionMap.get(gameID);
        if(sessions == null){
            Set<Session> sessions1 = new java.util.HashSet<>(Set.of());
            sessions1.add(session);
            sessionMap.put(gameID,sessions1);
            return;
        }
        sessions.add(session);
    }
    public void removeSessionFromGame(int gameID, Session session){
        Set<Session> sessions = sessionMap.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
        }
    }
    public Set<Session>getSessionForGame(int gameID){
        Set<Session> sessions = sessionMap.get(gameID);
        if(sessions == null){
            return null;
        }
        return sessions;
    }

}
