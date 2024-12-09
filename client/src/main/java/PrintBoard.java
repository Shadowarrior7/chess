import chess.*;
import model.GameData;
import server.ServerFacade;
import ui.EscapeSequences;

import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;


public class PrintBoard {
    private static ServerFacade serverFacade;
    private static String token;
    private static String color;

    public PrintBoard(String[] args, ServerFacade serverFacade, String token, String color) throws Exception {
        this.serverFacade = serverFacade;
        this.token = token;
        this.color = color;
    }

    public static void printBoard(int gameID) {
        try {
            Collection<GameData> games = serverFacade.listGames(token);
            GameData theGame = null;
            for (GameData game : games) {
                if (game.gameID() == gameID) {
                    theGame = game;
                    break;
                }
            }
            assert theGame != null;

            ChessBoard theBoard = theGame.game().getBoard();
            String blue = EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
            String black = EscapeSequences.SET_BG_COLOR_WHITE;
            String white = EscapeSequences.SET_BG_COLOR_DARK_GREEN;

            // Print file (column) headers
            if (color.equals("BLACK")) {
                System.out.println("\n" + blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR);
            } else {
                System.out.println("\n" + blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR);
            }

            // Control rank order based on player color
            int startRank = color.equals("BLACK") ? 1 : 8;
            int endRank = color.equals("BLACK") ? 8 : 1;
            int rankStep = color.equals("BLACK") ? 1 : -1;

            for (int i = startRank; i != endRank + rankStep; i += rankStep) {
                StringBuilder row = new StringBuilder(blue + i + " ");

                for (int j = 1; j <= 8; j++) {
                    boolean isWhiteSquare = (i + j) % 2 == 0;

                    int column = color.equals("BLACK") ? 9 - j : j;

                    String squareColor = isWhiteSquare ? white : black;
                    extracted(row.append(squareColor), helper(i, column, theBoard));
                }

                extracted(row.append(blue).append(" ").append(i), EscapeSequences.RESET_BG_COLOR);
                System.out.println(row);
            }
            if (color.equals("BLACK")) {
                System.out.println(blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                System.out.println(blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void extracted(StringBuilder row, String i) {
        row.append(i);
    }

    public static void printBoardWithHighlights(int gameID, ChessPosition pos) {
        try {
            // 1. Get the game data for the specified gameID
            Collection<GameData> games = serverFacade.listGames(token);
            GameData theGame = null;
            for (GameData game : games) {
                if (game.gameID() == gameID) {
                    theGame = game;
                    break;
                }
            }
            assert theGame != null;


            Collection<ChessMove> validMoves = theGame.game().validMoves(pos);

            ChessBoard theBoard = theGame.game().getBoard();
            String blue = EscapeSequences.SET_BG_COLOR_BLUE + EscapeSequences.SET_TEXT_BOLD;
            String black = EscapeSequences.SET_BG_COLOR_WHITE;
            String white = EscapeSequences.SET_BG_COLOR_DARK_GREEN;
            String highlight = EscapeSequences.SET_BG_COLOR_GREEN;
            String pieceHighlight = EscapeSequences.SET_BG_COLOR_RED;


            if (color.equals("BLACK")) {
                System.out.println("\n" + blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR);
            } else {
                System.out.println("\n" + blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR);
            }

            int startRank = color.equals("BLACK") ? 1 : 8;
            int endRank = color.equals("BLACK") ? 8 : 1;
            int rankStep = color.equals("BLACK") ? 1 : -1;

            for (int i = startRank; i != endRank + rankStep; i += rankStep) {
                StringBuilder row = new StringBuilder(blue + i + " ");

                for (int j = 1; j <= 8; j++) {
                    boolean isWhiteSquare = (i + j) % 2 == 0;

                    int column = color.equals("BLACK") ? 9 - j : j;//flips board

                    boolean isPiecePosition = (pos.getRow() == i && pos.getColumn() == column);

                    boolean isValidMove = false;
                    for (ChessMove move : validMoves) {
                        ChessPosition targetPosition = move.getEndPosition();
                        if (targetPosition.getRow() == i && targetPosition.getColumn() == column) {
                            isValidMove = true;
                            break;
                        }
                    }

                    String squareColor;
                    if (isPiecePosition) {
                        squareColor = pieceHighlight;
                    } else if (isValidMove) {
                        squareColor = highlight;
                    } else {
                        squareColor = isWhiteSquare ? white : black;
                    }
                    extracted(row.append(squareColor), helper(i, column, theBoard));
                }

                extracted(row.append(blue).append(" ").append(i), EscapeSequences.RESET_BG_COLOR);
                System.out.println(row);
            }
            if (color.equals("BLACK")) {
                System.out.println(blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   h  g  f  e  d  c  b  a  " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                System.out.println(blue + EscapeSequences.SET_TEXT_COLOR_BLACK +
                        "   a  b  c  d  e  f  g  h  " + EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (Exception e) {
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
