package websocket.messages;

public class Notification extends ServerMessage{
    public Notification(ServerMessageType type, String message) {
        super(type);
    }
}
