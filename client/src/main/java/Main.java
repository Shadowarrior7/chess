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
    private static WebSocketFascade webSocket;
    private static PrintBoard printBoard;
    private static boolean observe;

    public static void main(String[] args) throws Exception {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("Welcome to a pretty ok chess app. Type Help to get stated\n");
        whoAmI = "Logged Out";
        exit = false;
        loginFlag = false;
        joinFlag = false;
        observe = false;
        serverFacade = new ServerFacade("http://localhost:8080");
//        webSocket= new WebSocketFascade("http://localhost:8080");

        while (!exit) {
            if(!loginFlag) {
                preLogin(args);
            }
            if (!loginFlag) {
                return;
            }
            loggedIn(args);
            if(observe){
                observe();
            }
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

    public static void loggedIn(String[] args) throws Exception {
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
                printBoard = new PrintBoard(args, serverFacade, token, color);
                try{
                    int i = Integer.parseInt(splitString[1]);
                    String readID = printBoard.findID(i);
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
                printBoard = new PrintBoard(args, serverFacade, token, color);
                try {
                    int i = Integer.parseInt(splitString[1]);
                    String realId = printBoard.findID(i);
                    if (realId.isEmpty()){
                        throw new Exception("");
                    }
                    parseError = false;
                    loop = false;
                    observe = true;
                    gameIDG = Integer.parseInt(realId);
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

        printBoard = new PrintBoard(args, serverFacade, token, color);
        try {
            webSocket = new WebSocketFascade("ws://localhost:8080/ws", printBoard);
            webSocket.connect(token, gameIDG);
        } catch (Exception e){
            return;
        }
        //printBoard.printBoard(gameIDG);
        webSocket.setUserName(whoAmI);
        System.out.println("You are in a game");
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        while (loop){
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("leave")){
                Collection<GameData> games = serverFacade.listGames(token);
                for (GameData game : games){
                    extracted(game);
                }
                joinFlag = false;
                loop = false;
            }
            if (input.equals("help")){
                System.out.println("move <piece position> <destination> - put put inputs in coordinate form");
                System.out.println("redraw");
                System.out.println("leave");
                System.out.println("resign");
                System.out.println("highlight <piece position>");
                System.out.println("help");
            }
            if(input.equals("redraw")){
                printBoard.printBoard(gameIDG);
            }
            if(input.equals("resign")){
                try {
                    webSocket.resign(token, gameIDG);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");
            if(splitString[0].equals("move")){
                ChessPosition toMove = convertPosition(splitString[1]);
                ChessPosition dest = convertPosition(splitString[2]);
                ChessMove move = null;
                if(toMove == null || dest == null){
                    System.out.println("not valid move syntax");
                }
                else {
                    GameData myGame = null;
                    Collection<GameData> games = serverFacade.listGames(token);
                    myGame = getGameData(games, myGame);
                    assert myGame != null;
                    //System.out.println("my color: "+ color);
                    //System.out.println("piece color: "+ myGame.game().getBoard().getPiece(toMove).getTeamColor());
                    if(!myGame.game().getBoard().getPiece(toMove).getTeamColor().toString().equals(color)){
                        System.out.println("the piece you are trying to move is not yours");
                        continue;
                    }
                    try {
                        ChessGame oldGame = myGame.game();
                        ChessGame newGame = myGame.game();
                        move = new ChessMove(toMove, dest, promotionPiece(toMove, dest, myGame.game()));
                        newGame.makeMove(move);
                        GameData oldGameData = new GameData(myGame.gameID(), myGame.whiteUsername(),
                                myGame.blackUsername(), myGame.gameName(), oldGame);
                        GameData newGameData = new GameData(myGame.gameID(), myGame.whiteUsername(),
                                myGame.blackUsername(), myGame.gameName(), newGame);
                        //serverFacade.makeMove(token, oldGameData, newGameData);
                        webSocket.makeMove(gameIDG, token, move);
                        //printBoard.printBoard(gameIDG);
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }
            highLight(splitString);
        }
    }

    private static GameData getGameData(Collection<GameData> games, GameData myGame) {
        for (GameData game : games) {
            if (game.gameID() == gameIDG){
                myGame = game;
            }
        }
        return myGame;
    }

    public static ChessPiece.PieceType promotionPiece(ChessPosition toMove, ChessPosition dest, ChessGame game)
            throws Exception {
        Scanner scanner = new Scanner(System.in);
        if(!game.getBoard().getPiece(toMove).getPieceType().equals(ChessPiece.PieceType.PAWN)){
            return null;
        }
        if (color.equals("white") && dest.getRow() == 8){
            System.out.println("you can promote this pawn, please give a piece type to promote to. ex( queen ): ");
            String input = scanner.nextLine().trim().toLowerCase();
            return parsePiece(input);
        }
        else if (color.equals("black") && dest.getRow() == 1) {
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
    public static ChessPosition convertPosition(String position) {
        int finalColumn = 0;
        int finalRow = 0;
        if (position.length() != 2) {
            return null;
        }
        String column = position.substring(0, position.length() / 2);
        String row = position.substring(position.length() / 2);

        if (Character.isLetter(column.charAt(0)) && Character.isDigit(row.charAt(0))) {

            char firstChar = Character.toLowerCase(column.charAt(0));
            int number = Integer.parseInt(row);

            if (firstChar >= 'a' && firstChar <= 'h' && number >= 1 && number <= 8) {
                finalColumn = firstChar - 'a' + 1;
                finalRow = number;
                return new ChessPosition(finalRow, finalColumn);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static void extracted(GameData game) throws Exception {
        if(game.gameID() == gameIDG){
            if(game.blackUsername() != null && game.blackUsername().equals(whoAmI)){
                ChessGame newGame = new ChessGame();
                ChessBoard newBoard = new ChessBoard();
                newGame.setTeamTurn(game.game().getTeamTurn());
                newBoard.setSquares(game.game().copyBoard(game.game().getBoard()));
                newGame.setBoard(newBoard);
                GameData updated = new GameData(game.gameID(), game.whiteUsername(), null,
                        game.gameName(), newGame);
                //serverFacade.makeMove(token, game, updated);
                webSocket.leave(token, gameIDG);
            }
            if(game.whiteUsername() != null && game.whiteUsername().equals(whoAmI)){
                GameData updated = new GameData(game.gameID(), null, game.blackUsername(),
                        game.gameName(), game.game());
                //serverFacade.makeMove(token, game, updated);
                webSocket.leave(token, gameIDG);
            }
            if(observe){
                webSocket.leave(token, gameIDG);
            }
        }
    }

    public static void observe() throws Exception {
        Collection<GameData> games = serverFacade.listGames(token);
        GameData myGame = null;
        getGameData(games, myGame);
        webSocket = new WebSocketFascade("ws://localhost:8080/ws", printBoard);
        webSocket.connect(token, gameIDG);
        webSocket.setUserName(whoAmI);

        System.out.println("You are observing a game");
        Scanner scanner = new Scanner(System.in);
        boolean loop = true;
        while (loop){
            System.out.println("(" + whoAmI + ") >>>> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if(input.equals("help")){
            System.out.println("help");
            System.out.println("highlight <Piece position>");
            System.out.println("leave");
            }
            if (input.equals("leave")){
                Collection<GameData> games1 = serverFacade.listGames(token);
                for (GameData game : games1){
                    extracted(game);
                }
                observe = false;
                loop = false;
            }
            String[] splitString = input.toLowerCase(Locale.ROOT).split(" ");
            highLight(splitString);
        }



    }

    public static void highLight(String[] splitString) throws Exception {
        if(splitString[0].equals("highlight")){
            ChessPosition piecePos = convertPosition(splitString[1]);
            if(splitString[1].length() != 2 || piecePos == null){
                System.out.println("invalid syntax for piece position");
                return;
            }
            GameData myGame = null;
            Collection<GameData> games = serverFacade.listGames(token);
            myGame = getGameData(games, myGame);
            printBoard.printBoardWithHighlights(myGame.gameID(), piecePos);
        }
    }
}