
import com.google.gson.Gson;

import javax.management.Notification;
import javax.websocket.*;
import java.net.URI;

public class WebSocketFascade {
    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFascade(String url, NotificationHandler notificationHandler) throws Exception{
        try{
            url = url.replace("http", "ws");
            URI socketURL = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURL);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                    public void onMessage(String message){
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @OnMessage
    private void onMessage(String message){
        Gson seralizer = new Gson();
        seralizer.fromJson(message, )
    }
}
