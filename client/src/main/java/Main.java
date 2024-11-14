import chess.*;
import ui.*;
import model.AuthData;
import model.GameData;
import model.JoinGame;
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
    private static boolean joinFlag;
    private static String color;
    private static int gameIDG;


    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("Welcome to a pretty ok chess app. Type Help to get stated\n");
        whoAmI = "Logged Out";
        exit = false;
        loginFlag = false;
        joinFlag = false;
        serverFacade = new ServerFacade("http://localhost:8080");
        while (!exit) {
            if(!loginFlag) {
                preLogin(args);
            }
            if (!loginFlag) {
                return;
            }
            loggedIn(args);
            if (!joinFlag){
                continue;
            }
            if(joinFlag) {
                game(args);
            }
        }
    }

    public static void preLogin(String[] args){
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine().trim();

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
                    token = serverFacade.register(new UserData(splitString[1], splitString[2], splitString[3])).authToken();
                    loginFlag = true;
                    loop = false;
                    whoAmI = splitString[1];

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
            color = "WHITE";
            //printBoard(1237);
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine().trim();

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
                exit = true;
                loop = false;
            }
            if(input.toLowerCase(Locale.ROOT).equals("list")){
                try{
                    System.out.println("not here");
                    Collection<GameData> games = serverFacade.listGames(token);
                    System.out.println("here?");
                    listGames(games);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            if (input.toLowerCase(Locale.ROOT).equals("logout")){
                try {
                    serverFacade.logout(token);
                    loginFlag = false;
                    token = null;
                    whoAmI = "Logged Out";
                    loop = false;
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }

            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");
            if (splitString[0].equals("create")){
                try{
                    GameData game = new GameData(1, null, null, splitString[1], null);
                    String json = serverFacade.createGame(token, game);
                    String number = json.replaceAll("[^0-9]", "");
                    System.out.println("Here is your game ID = "+ number);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }

            if (splitString[0].equals("join")){
                try{
                    JoinGame jGame = new JoinGame(splitString[2].toUpperCase(Locale.ROOT), splitString[1]);

                    serverFacade.joinGame(token, jGame);
                    color = splitString[2].toUpperCase();
                    gameIDG = Integer.parseInt(splitString[1]);
                    joinFlag = true;
                    loop = false;

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            if (splitString[0].equals("observe")){
                printBoard(Integer.parseInt(splitString[1]));
            }
        }
    }

    private static void listGames(Collection<GameData> games){
        int i = 0;
        for (GameData game : games) {
            ++i;
            System.out.println(i + ": ID= " +game.gameID() + ", game name= " + game.gameName() + ", white player = " + game.whiteUsername() +
                    ", black player = " + game.blackUsername());
        }
    }

    public static void game(String[] args){
        printBoard(gameIDG);
        System.out.println("You are in game: "+ gameIDG);
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        while (loop){
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("quit")){
                joinFlag = false;
                loop = false;
            }
            if (input.equals("help")){
                System.out.println("move <piece position> <destination> - put put inputs in coordinate form");
                System.out.println("refresh");
                System.out.println("quit");
                System.out.println("help");
            }
            if(input.equals("refresh")){
                printBoard(gameIDG);
            }
        }
    }

    public static void printBoard(int gameID){
        try {
            Collection<GameData> games = serverFacade.listGames(token);
            GameData theGame = null;
            for (GameData game : games){
                if(game.gameID() == gameID){
                    theGame = game;
                    break;
                }
            }

            assert theGame != null;
            ChessBoard theBoard = theGame.game().getBoard();
            String blue = EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
            String black = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
            String white = EscapeSequences.SET_BG_COLOR_WHITE;

            if(color.equals("BLACK")) {
                System.out.println("\n" + EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.SET_TEXT_BOLD + "  a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "1" + black + helper(1, 1, theBoard) + white + helper(1, 2, theBoard) + black + helper(1, 3, theBoard) + white + helper(1, 4, theBoard) + black + helper(1, 5, theBoard) + white + helper(1, 6, theBoard) + black + helper(1, 7, theBoard) + white + helper(1, 8, theBoard) + blue + "1" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "2" + white + helper(2, 1, theBoard) + black + helper(2, 2, theBoard) + white + helper(2, 3, theBoard) + black + helper(2, 4, theBoard) + white + helper(2, 5, theBoard) + black + helper(2, 6, theBoard) + white + helper(2, 7, theBoard) + black + helper(2, 8, theBoard) + blue + "2" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "3" + black + helper(3, 1, theBoard) + white + helper(3, 2, theBoard) + black + helper(3, 3, theBoard) + white + helper(3, 4, theBoard) + black + helper(3, 5, theBoard) + white + helper(3, 6, theBoard) + black + helper(3, 7, theBoard) + white + helper(3, 8, theBoard) + blue + "3" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "4" + white + helper(4, 1, theBoard) + black + helper(4, 2, theBoard) + white + helper(4, 3, theBoard) + black + helper(4, 4, theBoard) + white + helper(4, 5, theBoard) + black + helper(4, 6, theBoard) + white + helper(4, 7, theBoard) + black + helper(4, 8, theBoard) + blue + "4" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "5" + black + helper(5, 1, theBoard) + white + helper(5, 2, theBoard) + black + helper(5, 3, theBoard) + white + helper(5, 4, theBoard) + black + helper(5, 5, theBoard) + white + helper(5, 6, theBoard) + black + helper(5, 7, theBoard) + white + helper(5, 8, theBoard) + blue + "5" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "6" + white + helper(6, 1, theBoard) + black + helper(6, 2, theBoard) + white + helper(6, 3, theBoard) + black + helper(6, 4, theBoard) + white + helper(6, 5, theBoard) + black + helper(6, 6, theBoard) + white + helper(6, 7, theBoard) + black + helper(6, 8, theBoard) + blue + "6" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "7" + black + helper(7, 1, theBoard) + white + helper(7, 2, theBoard) + black + helper(7, 3, theBoard) + white + helper(7, 4, theBoard) + black + helper(7, 5, theBoard) + white + helper(7, 6, theBoard) + black + helper(7, 7, theBoard) + white + helper(7, 8, theBoard) + blue + "7" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "8" + white + helper(8, 1, theBoard) + black + helper(8, 2, theBoard) + white + helper(8, 3, theBoard) + black + helper(8, 4, theBoard) + white + helper(8, 5, theBoard) + black + helper(8, 6, theBoard) + white + helper(8, 7, theBoard) + black + helper(8, 8, theBoard) + blue + "8" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.SET_TEXT_BOLD + "  a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
            }
            else {
                System.out.println("\n" + EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.SET_TEXT_BOLD + "  h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "8" + black + helper(8, 1, theBoard) + white + helper(8, 2, theBoard) + black + helper(8, 3, theBoard) + white + helper(8, 4, theBoard) + black + helper(8, 5, theBoard) + white + helper(8, 6, theBoard) + black + helper(8, 7, theBoard) + white + helper(8, 8, theBoard) + blue + "8" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "7" + white + helper(7, 1, theBoard) + black + helper(7, 2, theBoard) + white + helper(7, 3, theBoard) + black + helper(7, 4, theBoard) + white + helper(7, 5, theBoard) + black + helper(7, 6, theBoard) + white + helper(7, 7, theBoard) + black + helper(7, 8, theBoard) + blue + "7" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "6" + black + helper(6, 1, theBoard) + white + helper(6, 2, theBoard) + black + helper(6, 3, theBoard) + white + helper(6, 4, theBoard) + black + helper(6, 5, theBoard) + white + helper(6, 6, theBoard) + black + helper(6, 7, theBoard) + white + helper(6, 8, theBoard) + blue + "6" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "5" + white + helper(5, 1, theBoard) + black + helper(5, 2, theBoard) + white + helper(5, 3, theBoard) + black + helper(5, 4, theBoard) + white + helper(5, 5, theBoard) + black + helper(5, 6, theBoard) + white + helper(5, 7, theBoard) + black + helper(5, 8, theBoard) + blue + "5" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "4" + black + helper(4, 1, theBoard) + white + helper(4, 2, theBoard) + black + helper(4, 3, theBoard) + white + helper(4, 4, theBoard) + black + helper(4, 5, theBoard) + white + helper(4, 6, theBoard) + black + helper(4, 7, theBoard) + white + helper(4, 8, theBoard) + blue + "4" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "3" + white + helper(3, 1, theBoard) + black + helper(3, 2, theBoard) + white + helper(3, 3, theBoard) + black + helper(3, 4, theBoard) + white + helper(3, 5, theBoard) + black + helper(3, 6, theBoard) + white + helper(3, 7, theBoard) + black + helper(3, 8, theBoard) + blue + "3" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "2" + black + helper(2, 1, theBoard) + white + helper(2, 2, theBoard) + black + helper(2, 3, theBoard) + white + helper(2, 4, theBoard) + black + helper(2, 5, theBoard) + white + helper(2, 6, theBoard) + black + helper(2, 7, theBoard) + white + helper(2, 8, theBoard) + blue + "2" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "1" + white + helper(1, 1, theBoard) + black + helper(1, 2, theBoard) + white + helper(1, 3, theBoard) + black + helper(1, 4, theBoard) + white + helper(1, 5, theBoard) + black + helper(1, 6, theBoard) + white + helper(1, 7, theBoard) + black + helper(1, 8, theBoard) + blue + "1" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK + EscapeSequences.SET_TEXT_BOLD + "  h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static String helper(int i, int j, ChessBoard board){
        ChessPiece[][] squares = board.getSquares();
        if (squares[i][j] == null){
            return "   ";
        }

        ChessPiece piece = squares[i][j];
        if (piece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
            return helperWhite(piece);
        }
        if (piece.getTeamColor().equals(ChessGame.TeamColor.BLACK)){
            return helperBlack(piece);
        }
        return "   ";
    }

    public static String helperBlack(ChessPiece piece){
        ChessPiece.PieceType type = piece.getPieceType();
        if (type.equals(ChessPiece.PieceType.KING)){
            return EscapeSequences.BLACK_KING;
        }
        if (type.equals(ChessPiece.PieceType.QUEEN)){
            return EscapeSequences.BLACK_QUEEN;
        }
        if (type.equals(ChessPiece.PieceType.BISHOP)){
            return EscapeSequences.BLACK_BISHOP;
        }
        if (type.equals(ChessPiece.PieceType.KNIGHT)){
            return EscapeSequences.BLACK_KNIGHT;
        }
        if (type.equals(ChessPiece.PieceType.ROOK)){
            return EscapeSequences.BLACK_ROOK;
        }
        if (type.equals(ChessPiece.PieceType.PAWN)){
            return EscapeSequences.BLACK_PAWN;
        }
        return "   ";
    }
    public static String helperWhite(ChessPiece piece){
        ChessPiece.PieceType type = piece.getPieceType();
        if (type.equals(ChessPiece.PieceType.KING)){
            return EscapeSequences.WHITE_KING;
        }
        if (type.equals(ChessPiece.PieceType.QUEEN)){
            return EscapeSequences.WHITE_QUEEN;
        }
        if (type.equals(ChessPiece.PieceType.BISHOP)){
            return EscapeSequences.WHITE_BISHOP;
        }
        if (type.equals(ChessPiece.PieceType.KNIGHT)){
            return EscapeSequences.WHITE_KNIGHT;
        }
        if (type.equals(ChessPiece.PieceType.ROOK)){
            return EscapeSequences.WHITE_ROOK;
        }
        if (type.equals(ChessPiece.PieceType.PAWN)){
            return EscapeSequences.WHITE_PAWN;
        }
        return "   ";
    }
}