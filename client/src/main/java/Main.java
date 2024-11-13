import chess.*;
import server.ServerFacade;

import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static String whoAmI;


    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("Welcome to a pretty ok chess app. Type Help to get stated\n");
        whoAmI = "Logged Out";
        preLogin(args);
    }

    public static void preLogin(String[] args){
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine();

            if(input.toLowerCase(Locale.ROOT).equals("help")){
                System.out.println("register <Username> <Password> <Email> - creates an account");
                System.out.println("login <Username> <Password>");
                System.out.println("quit");
                System.out.println("help");
            }
            if(input.toLowerCase(Locale.ROOT).equals("quit")){
                loop = false;
            }
            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");
            if(splitString[0].equals("register")){

            }
        }
    }
}