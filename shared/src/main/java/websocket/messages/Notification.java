package websocket.messages;

public class Notification extends ServerMessage{
    private String message;
    public Notification(ServerMessageType type, String message1) {
        super(type);
        message = message1;
    }

    public String getMessage(){
        return message;
    }
}
