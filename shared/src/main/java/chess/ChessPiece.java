package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
        //throw new RuntimeException("Not implemented");
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return Enum.valueOf(PieceType.class, type.name());

        //throw new RuntimeException("Not implemented");
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType pieceType = getPieceType();

        //return new ArrayList<>();
        if (pieceType.equals(PieceType.KING)) {
            int[][] king_moves = {{1,0},{-1,0},{0,1},{0,-1},{-1,1},{1,1},{-1,-1},{1,-1}};
            return check_for_moves(king_moves, board, myPosition, false);
        }
        else if (pieceType.equals(PieceType.QUEEN)) {
            int[][] queen_moves = {{1,0},{-1,0},{0,1},{0,-1},{-1,1},{1,1},{-1,-1},{1,-1}};
            return check_for_moves(queen_moves, board, myPosition, true);
        }
        else if (pieceType.equals(PieceType.BISHOP)) {
            int[][] bishop_moves = {{1,-1},{-1,1},{1,1},{-1,-1}};
            return check_for_moves(bishop_moves, board, myPosition, true);
        }
        else if (pieceType.equals(PieceType.KNIGHT)) {
            int[][] knight_moves = {{2,1},{2,-1},{1,-2},{1,2},{-1,-2},{-2,-1},{-1,2},{-2,1}};
            return check_for_moves(knight_moves, board, myPosition, false);
        }
        else if (pieceType.equals(PieceType.ROOK)) {
            int[][] rook_moves = {{1,0},{-1,0},{0,1},{0,-1}};
            return check_for_moves(rook_moves, board, myPosition, true);
        }
        else if (pieceType.equals(PieceType.PAWN)) {
            //black move, black right, black left
            int[][] pawn_moves_black = {{-1,0},{-1,-1},{-1,1}};
            //white move, white right, white left
            int[][] pawn_moves_white = {{1,0},{1,1},{1,-1}};

            if (ChessGame.TeamColor.BLACK.equals(getTeamColor())) {
                return check_for_moves(pawn_moves_black, board, myPosition, false);
            }
            else if (ChessGame.TeamColor.WHITE.equals(getTeamColor())) {
                return check_for_moves(pawn_moves_white, board, myPosition, false);
            }
        }
        //return null;moves
        throw new RuntimeException("pieceMoves");
    }

    public Collection<ChessMove> check_for_moves(int[][] possibilities, ChessBoard board, ChessPosition myPosition, Boolean iterative) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor my_color = getTeamColor();
        if (iterative) {
            for (int[] move : possibilities){
                //set cord for current position
                int current_row = myPosition.getRow();
                int current_column = myPosition.getColumn();

                while(true){
                    //add the move to position
                    current_row += move[0];
                    current_column += move[1];

                    //checks if piece is off the board
                    System.out.println(current_row + " " + current_column);
                    if (current_row < 1 || current_row > 8 || current_column < 1 || current_column > 8){
                        System.out.println("out of bounds");
                        break;
                    }
                    //check to see if there is a piece there
                    ChessPosition new_position = new ChessPosition(current_row, current_column);
                    //ChessPosition check = new ChessPosition(current_row-1, current_column-1);
                    System.out.println(new_position);
                    if (board.getPiece(new_position) != null){
                        System.out.println("piece there");
                        ChessGame.TeamColor piece_color = board.getPiece(new_position).getTeamColor();
                        //checks if you can take the piece
                        if (piece_color.equals(my_color)){
                            System.out.println("same color");
                            System.out.println(board.getPiece(new_position).getPieceType());
                            break;
                        }
                        else {
                            System.out.println("different color");
                            ChessMove move_to_add = new ChessMove(myPosition, new_position, null);
                            moves.add(move_to_add);
                            break;
                        }
                    }
                    System.out.println("no piece there");
                    ChessMove move_to_add = new ChessMove(myPosition, new_position, null);
                    moves.add(move_to_add);
                }
            }
        }
        else if (!iterative){
            if (getPieceType().equals(PieceType.PAWN)){
                int first_move = 0;
                for (int[] move : possibilities) {
                    ++first_move;
                    while(true) {
                        int current_row = myPosition.getRow();
                        int current_column = myPosition.getColumn();
                        current_row += move[0];
                        current_column += move[1];
                        if (current_row < 1 || current_row > 8 || current_column < 1 || current_column > 8) {
                            break;
                        }
                        ChessPosition new_position = new ChessPosition(current_row, current_column);
                        if (first_move == 1){
                            if (board.getPiece(new_position) != null){
                                break;
                            }
                        }
                        else {
                            if (board.getPiece(new_position) != null){
                                if(!board.getPiece(new_position).getTeamColor().equals(my_color)){
                                    ChessMove move_to_add = new ChessMove(myPosition, new_position, null);
                                    moves.add(move_to_add);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }

            }
            else {
                for (int[] move : possibilities) {
                    while(true) {
                        int current_row = myPosition.getRow();
                        int current_column = myPosition.getColumn();
                        current_row += move[0];
                        current_column += move[1];
                        if (current_row < 1 || current_row > 8 || current_column < 1 || current_column > 8) {
                            break;
                        }
                        ChessPosition new_position = new ChessPosition(current_row, current_column);
                        if (board.getPiece(new_position) != null) {
                            ChessGame.TeamColor piece = board.getPiece(new_position).getTeamColor();
                            if (piece.equals(my_color)) {
                                break;
                            } else {
                                ChessMove move_to_add = new ChessMove(myPosition, new_position, null);
                                moves.add(move_to_add);
                                break;
                            }

                        }
                        ChessMove move_to_add = new ChessMove(myPosition, new_position, null);
                        moves.add(move_to_add);
                        break;
                    }
                }
            }
        }
        else {
            throw new RuntimeException("check_for_moves");
        }
        return moves;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
