package websocket.messages;

public class Error extends ServerMessage{
    private String errorMessage;
    public Error(ServerMessageType type, String errorMessage1) {
        super(type);
        errorMessage = errorMessage1;
    }
}
