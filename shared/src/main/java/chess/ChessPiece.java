package chess;

import java.util.ArrayList;
import java.util.Collection;

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
        PieceType pieceType = type;
        //return new ArrayList<>();
        if (pieceType == PieceType.KING) {
            Collection<ChessMove> moves = new ArrayList<>();
            int[][] king_moves = {{1,0},{-1,0},{0,1},{0,-1},{-1,1},{1,1},{-1,-1},{1,-1}};
        }
        else if (pieceType == PieceType.QUEEN) {
            int[][] queen_moves = {{1,0},{-1,0},{0,1},{0,-1},{-1,1},{1,1},{-1,-1},{1,-1}};
        }
        else if (pieceType == PieceType.BISHOP) {
            int[][] bishop_moves = {{1,1},{-1,1},{1,-1},{-1,-1}};
        }
        else if (pieceType == PieceType.KNIGHT) {
            int[][] knight_moves = {{2,1},{2,-1},{1,-2},{1,2},{-1,-2},{-2,-1},{-1,2},{-2,1}};
        }
        else if (pieceType == PieceType.ROOK) {
            int[][] rook_moves = {{1,0},{-1,0},{0,1},{0,-1}};
        }
        else if (pieceType == PieceType.PAWN) {
            //white move, black move, white right, white left, black right, black left
            int[][] pawn_moves = {{1,0},{0,1},{1,1},{1,-1},{-1,-1},{-1,1}};
        }
        //throw new RuntimeException("Not implemented");
    }

    public Collection<ArrayList<ChessMove>> check_for_moves(int[][] possibilities, ChessBoard board, ChessPosition myPosition, Boolean iterative) {
        Collection<ArrayList<ChessMove>> moves = new ArrayList<>();
        if (iterative){
            for (int[] move : possibilities){
                int current_row = myPosition.getRow();
                int current_column = myPosition.getColumn();

                while(true){
                    current_row += move[0];
                    current_column += move[1];

                    //checks if piece is off the board
                    if (current_row < 0 | current_row > 9 | current_column < 0 | current_column > 9){
                        break;
                    }
                    ChessPosition new_position = new ChessPosition(current_row, current_column);
                    if (board.getPiece(new_position) != null){
                        ChessGame.TeamColor piece = board.getPiece(new_position).pieceColor;
                        
                    };

                }
            }
        }
        else if (!iterative){

        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}
