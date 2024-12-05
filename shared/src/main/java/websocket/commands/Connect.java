package websocket.commands;

public class Connect extends UserGameCommand{
    public Connect(CommandType commandType, String authToken, Integer gameID) {
        super(commandType, authToken, gameID);
    }
}
