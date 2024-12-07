

import chess.ChessGame;

import model.GameData;

import javax.management.Notification;

public interface NotificationHandler {
    void notify(Notification notification);
//    default void printMessage(String message){
//        System.out.println(message);
//    }
//    default void updateGame(GameData game){
//
//    }

}
