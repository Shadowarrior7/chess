import chess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;

import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    public static String whoAmI;
    public static ServerFacade serverFacade;
    private static boolean loginFlag;
    private static String token;
    private static boolean exit;


    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("Welcome to a pretty ok chess app. Type Help to get stated\n");
        whoAmI = "Logged Out";
        exit = false;
        loginFlag = false;
        serverFacade = new ServerFacade("http://localhost:8080");
        while (!exit) {
            preLogin(args);
            if (!loginFlag) {
                return;
            }
            loggedIn(args);
        }
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
                exit = true;
                loop = false;
            }
            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");
            if(splitString[0].equals("register")){
                try{
                    serverFacade.register(new UserData(splitString[1], splitString[2], splitString[3]));
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            if(splitString[0].equals("login")){
                try {
                    token = serverFacade.login(new UserData(splitString[1], splitString[2], null)).authToken();
                    loop = false;
                    loginFlag = true;
                    whoAmI = splitString[1];
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static void loggedIn(String[] args){
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        System.out.println("Logged in as: " + whoAmI + "! Type Help to view options");
        while (loop) {
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine();

            if(input.toLowerCase(Locale.ROOT).equals("help")){
                System.out.println("create <Name>");
                System.out.println("List");
                System.out.println("join <id> [WHITE/BLACK]");
                System.out.println("observe <id>");
                System.out.println("logout");
                System.out.println("quit");
                System.out.println("help");
            }
            if(input.toLowerCase(Locale.ROOT).equals("quit")){
                loop = false;
            }
            if(input.toLowerCase(Locale.ROOT).equals("list")){
                try{
                    Collection<GameData> games = serverFacade.listGames(token);
                    System.out.println(games);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }

            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");

        }
    }
}