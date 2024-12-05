import chess.*;
import ui.*;

import model.GameData;
import model.JoinGame;
import model.UserData;
import server.ServerFacade;

import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;

public class Main{
    public static String whoAmI;
    public static ServerFacade serverFacade;
    private static boolean loginFlag;
    private static String token;
    private static boolean exit;
    private static boolean joinFlag;
    private static String color;
    private static int gameIDG;
    private static ChessGame game;


    public static void main(String[] args) throws Exception {
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
                    if (e.getMessage().equals("403")){
                        System.out.println("this user is already taken");
                    }
                }
            }
            if(splitString[0].equals("login")){
                try {
                    token = serverFacade.login(new UserData(splitString[1], splitString[2], null)).authToken();
                    loop = false;
                    loginFlag = true;
                    whoAmI = splitString[1];
                } catch (Exception e){
                    if(e.getMessage().equals("401")){
                        System.out.println("unauthorized");
                    }
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
            if(input.toLowerCase(Locale.ROOT).equals("list")){
                try{
                    Collection<GameData> games = serverFacade.listGames(token);
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
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }

            if (splitString[0].equals("join")){
                boolean parseError = true;
                boolean hmm;
                try{
                    int i = Integer.parseInt(splitString[1]);
                    String readID = findID(i);
                    if(readID.isEmpty()){
                        throw new Exception("");
                    }
                    parseError = false;
                    if(splitString[2].toUpperCase().equals("WHITE") || splitString[2].toUpperCase().equals("BLACK")){
                        hmm = false;
                    } else {
                        System.out.println("must choose either white or black");
                        throw new Exception("");
                    }
                    JoinGame jGame = new JoinGame(splitString[2].toUpperCase(Locale.ROOT), readID);

                    serverFacade.joinGame(token, jGame);
                    color = splitString[2].toUpperCase();
                    gameIDG = Integer.parseInt(readID);
                    joinFlag = true;
                    loop = false;

                } catch (Exception e) {
                    if(parseError){
                        System.out.println("not a valid ID");
                    }
                    if(e.getMessage().equals("403")){
                        System.out.println("spot already taken");
                    }
                }
            }

            if (splitString[0].equals("observe")){
                boolean parseError = true;
                try {
                    int i = Integer.parseInt(splitString[1]);
                    String realId = findID(i);
                    if (realId.isEmpty()){
                        throw new Exception("");
                    }
                    parseError = false;
                    printBoard(Integer.parseInt(realId));
                }catch (Exception e){
                    if(parseError){
                        System.out.println("not a valid ID");
                    }
                }
            }
        }
    }

    private static void listGames(Collection<GameData> games){
        int i = 0;
        for (GameData game : games) {
            ++i;
            System.out.println(i + ": game name= " + game.gameName() + ", white player = " + game.whiteUsername() +
                    ", black player = " + game.blackUsername());
        }
    }

    public static void game(String[] args) throws Exception {
        printBoard(gameIDG);
        System.out.println("You are in a game");
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        while (loop){
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("leave")){
                //color = null;
                Collection<GameData> games = serverFacade.listGames(token);
                for (GameData game : games){
                    if(game.gameID() == gameIDG){
                        if(game.blackUsername() != null && game.blackUsername().equals(whoAmI)){
                            ChessGame newGame = new ChessGame();
                            ChessBoard newBoard = new ChessBoard();
                            newBoard.setSquares(game.game().copyBoard(game.game().getBoard()));
                            newGame.setBoard(newBoard);
                            GameData updated = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), newGame);
                            serverFacade.makeMove(token, game, updated);
                        }
                        if(game.whiteUsername() != null && game.whiteUsername().equals(whoAmI)){
                            GameData updated = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
                            serverFacade.makeMove(token, game, updated);
                        }
                    }
                }
                joinFlag = false;
                loop = false;
            }
            if (input.equals("help")){
                System.out.println("move <piece position> <destination> - put put inputs in coordinate form");
                System.out.println("redraw chess board");
                System.out.println("leave");
                System.out.println("help");
            }
            if(input.equals("redraw chess board")){
                printBoard(gameIDG);
            }
            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");
            if(splitString[0].equals("move")){
                ChessPosition toMove = convertPosition(splitString[1]);
                ChessPosition dest = convertPosition(splitString[2]);
                if(toMove == null || dest == null){
                    System.out.println("not valid move syntax");
                }
                else {
                    GameData myGame = null;
                    Collection<GameData> games = serverFacade.listGames(token);
                    for (GameData game : games) {
                        if (game.gameID() == gameIDG){
                            myGame = game;
                        }
                    }
                    assert myGame != null;
                    ChessBoard board = myGame.game().getBoard();
                    if(board.getPiece(toMove) == null){
                        System.out.println("looks like you selected a piece to move that does not exist");
                        continue;
                    }
                    if(!board.getPiece(toMove).getTeamColor().toString().equals(color)){
                        System.out.println("the piece you are trying to move is not yours");
                        //System.out.println(board.getPiece(toMove).getTeamColor().toString());
                        continue;
                    }
                    if(!myGame.game().getTeamTurn().toString().equals(color)){
                        System.out.println("not your turn");
                        System.out.println("it is " + myGame.game().getTeamTurn().toString() + "'s turn");
                        continue;
                    }
                    try {
                        ChessGame newGame = new ChessGame();
                        ChessBoard newBoard = new ChessBoard();
                        newBoard.setSquares(myGame.game().copyBoard(myGame.game().getBoard()));
                        newGame.setBoard(newBoard);
                        //newGame.changeTurn();
                        newGame.makeMove(new ChessMove(toMove, dest, promotionPiece(toMove, dest, myGame.game())));
                        GameData newGameData = new GameData(myGame.gameID(), myGame.whiteUsername(), myGame.blackUsername(), myGame.gameName(), newGame);
                        serverFacade.makeMove(token, myGame, newGameData);
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    printBoard(gameIDG);
                }
            }
        }
    }

    public static ChessPiece.PieceType promotionPiece(ChessPosition toMove, ChessPosition dest, ChessGame game) throws Exception {
        Scanner scanner = new Scanner(System.in);
        if(!game.getBoard().getPiece(toMove).getPieceType().equals(ChessPiece.PieceType.PAWN)){
            return null;
        }
        if (color.equals("WHITE") && dest.getRow() == 8){
            System.out.println("you can promote this pawn, please give a piece type to promote to. ex( queen ): ");
            String input = scanner.nextLine().trim().toLowerCase();
            return parsePiece(input);
        }
        else if (color.equals("BLACK") && dest.getRow() == 1) {
            System.out.println("you can promote this pawn, please give a piece type to promote to. ex( queen ): ");
            String input = scanner.nextLine().trim().toLowerCase();
            return parsePiece(input);
        }
        else {
            return null;
        }
    }

    public static ChessPiece.PieceType parsePiece(String input) throws Exception {
        input = input.toLowerCase(Locale.ROOT);
        if (input.equals("queen")){
            return ChessPiece.PieceType.QUEEN;
        }
        else if (input.equals("rook")){
            return ChessPiece.PieceType.ROOK;
        }
        else if (input.equals("bishop")){
            return ChessPiece.PieceType.BISHOP;
        }
        else if (input.equals("knight")){
            return ChessPiece.PieceType.KNIGHT;
        }
        else if (input.equals("pawn")){
            return ChessPiece.PieceType.PAWN;
        }
        else {
            //System.out.println("not a valid piece to promote too");
            throw new Exception("not a valid piece to promote too");
        }
    }

    public static ChessPosition convertPosition(String position){
        int finalColumn = 0;
        int finalRow = 0;
        if(position.length() != 2){
            return null;
        }
        String column = position.substring(0, position.length() /2);
        String row = position.substring(position.length()/2);

        if (Character.isLetter(column.charAt(0)) && Character.isDigit(row.charAt(0))) {

            char firstChar = Character.toLowerCase(column.charAt(0));
            int number = Integer.parseInt(row);

            if (firstChar >= 'a' && firstChar <= 'h' && number >= 1 && number <= 8){
                finalColumn = firstChar - 'a' +1;
                finalRow = number;
                return new ChessPosition(finalRow, finalColumn);
            }
            else {
                return null;
            }
        } else {
            return null;
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
            String black = EscapeSequences.SET_BG_COLOR_WHITE;
            String white = EscapeSequences.SET_BG_COLOR_DARK_GREEN;

            if(color.equals("BLACK")) {
                System.out.println("\n" + EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        EscapeSequences.SET_TEXT_BOLD + "  h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR);
                blackBoard(blue, white, theBoard, black);
            }
            else {
                System.out.println("\n" + EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        EscapeSequences.SET_TEXT_BOLD + "  a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "8" + black + helper(8, 1, theBoard) + white + helper(8, 2, theBoard) +
                        black + helper(8, 3, theBoard) + white + helper(8, 4, theBoard) + black +
                        helper(8, 5, theBoard) + white + helper(8, 6, theBoard) + black +
                        helper(8, 7, theBoard) + white + helper(8, 8, theBoard) + blue +
                        "8" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "7" + white + helper(7, 1, theBoard) + black + helper(7, 2, theBoard) +
                        white + helper(7, 3, theBoard) + black + helper(7, 4, theBoard) + white +
                        helper(7, 5, theBoard) + black + helper(7, 6, theBoard) + white +
                        helper(7, 7, theBoard) + black + helper(7, 8, theBoard) + blue +
                        "7" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "6" + black + helper(6, 1, theBoard) + white + helper(6, 2, theBoard) +
                        black + helper(6, 3, theBoard) + white + helper(6, 4, theBoard) + black +
                        helper(6, 5, theBoard) + white + helper(6, 6, theBoard) + black +
                        helper(6, 7, theBoard) + white + helper(6, 8, theBoard) + blue +
                        "6" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "5" + white + helper(5, 1, theBoard) + black + helper(5, 2, theBoard) +
                        white + helper(5, 3, theBoard) + black + helper(5, 4, theBoard) + white +
                        helper(5, 5, theBoard) + black + helper(5, 6, theBoard) + white +
                        helper(5, 7, theBoard) + black + helper(5, 8, theBoard) + blue +
                        "5" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "4" + black + helper(4, 1, theBoard) + white + helper(4, 2, theBoard) +
                        black + helper(4, 3, theBoard) + white + helper(4, 4, theBoard) + black +
                        helper(4, 5, theBoard) + white + helper(4, 6, theBoard) + black +
                        helper(4, 7, theBoard) + white + helper(4, 8, theBoard) + blue +
                        "4" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "3" + white + helper(3, 1, theBoard) + black + helper(3, 2, theBoard) +
                        white + helper(3, 3, theBoard) + black + helper(3, 4, theBoard) + white +
                        helper(3, 5, theBoard) + black + helper(3, 6, theBoard) + white +
                        helper(3, 7, theBoard) + black + helper(3, 8, theBoard) + blue +
                        "3" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "2" + black + helper(2, 1, theBoard) + white + helper(2, 2, theBoard) +
                        black + helper(2, 3, theBoard) + white + helper(2, 4, theBoard) + black +
                        helper(2, 5, theBoard) + white + helper(2, 6, theBoard) + black +
                        helper(2, 7, theBoard) + white + helper(2, 8, theBoard) + blue +
                        "2" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(blue + "1" + white + helper(1, 1, theBoard) + black + helper(1, 2, theBoard) +
                        white + helper(1, 3, theBoard) + black + helper(1, 4, theBoard) + white +
                        helper(1, 5, theBoard) + black + helper(1, 6, theBoard) + white +
                        helper(1, 7, theBoard) + black + helper(1, 8, theBoard) + blue +
                        "1" + EscapeSequences.RESET_BG_COLOR);
                System.out.println(EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        EscapeSequences.SET_TEXT_BOLD + "  a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private static void blackBoard(String blue, String white, ChessBoard theBoard, String black) {
        System.out.println(blue + "1" + white + helper(1, 8, theBoard) + black + helper(1, 7, theBoard) +
                white + helper(1, 6, theBoard) + black + helper(1, 5, theBoard) + white +
                helper(1, 4, theBoard) + black + helper(1, 3, theBoard) + white +
                helper(1, 2, theBoard) + black + helper(1, 1, theBoard) + blue +
                "1" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "2" + black + helper(2, 8, theBoard) + white + helper(2, 7, theBoard) +
                black + helper(2, 6, theBoard) + white + helper(2, 5, theBoard) + black +
                helper(2, 4, theBoard) + white + helper(2, 3, theBoard) + black +
                helper(2, 2, theBoard) + white + helper(2, 1, theBoard) + blue +
                "2" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "3" + white + helper(3, 8, theBoard) + black + helper(3, 7, theBoard) +
                white + helper(3, 6, theBoard) + black + helper(3, 5, theBoard) + white +
                helper(3, 4, theBoard) + black + helper(3, 3, theBoard) + white +
                helper(3, 2, theBoard) + black + helper(3, 1, theBoard) + blue +
                "3" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "4" + black + helper(4, 8, theBoard) + white + helper(4, 7, theBoard) +
                black + helper(4, 6, theBoard) + white + helper(4, 5, theBoard) + black +
                helper(4, 4, theBoard) + white + helper(4, 3, theBoard) + black +
                helper(4, 2, theBoard) + white + helper(4, 1, theBoard) + blue +
                "4" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "5" + white + helper(5, 8, theBoard) + black + helper(5, 7, theBoard) +
                white + helper(5, 6, theBoard) + black + helper(5, 5, theBoard) + white +
                helper(5, 4, theBoard) + black + helper(5, 3, theBoard) + white +
                helper(5, 2, theBoard) + black + helper(5, 1, theBoard) + blue +
                "5" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "6" + black + helper(6, 8, theBoard) + white + helper(6, 7, theBoard) +
                black + helper(6, 6, theBoard) + white + helper(6, 5, theBoard) + black +
                helper(6, 4, theBoard) + white + helper(6, 3, theBoard) + black +
                helper(6, 2, theBoard) + white + helper(6, 1, theBoard) + blue +
                "6" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "7" + white + helper(7, 8, theBoard) + black + helper(7, 7, theBoard) +
                white + helper(7, 6, theBoard) + black + helper(7, 5, theBoard) + white +
                helper(7, 4, theBoard) + black + helper(7, 3, theBoard) + white +
                helper(7, 2, theBoard) + black + helper(7, 1, theBoard) + blue +
                "7" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(blue + "8" + black + helper(8, 8, theBoard) + white + helper(8, 7, theBoard) +
                black + helper(8, 6, theBoard) + white + helper(8, 5, theBoard) + black +
                helper(8, 4, theBoard) + white + helper(8, 3, theBoard) + black +
                helper(8, 2, theBoard) + white + helper(8, 1, theBoard) + blue +
                "8" + EscapeSequences.RESET_BG_COLOR);
        System.out.println(EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_TEXT_BOLD + "  h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR +
                EscapeSequences.RESET_TEXT_COLOR);
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

    public static String findID(int id){
        String realID = "";
        try {

            Collection<GameData> games = serverFacade.listGames(token);
            int i = 0;
            for (GameData game : games) {
                ++i;
                if(i == id){
                    realID = String.valueOf(game.gameID());
                }
            }
            return realID;

        }catch(Exception e) {
            System.out.println("error in findID");
            System.out.println(e.getMessage());
        }
        return realID;
    }
}