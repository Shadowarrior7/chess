package chess;

import javax.swing.text.Position;
import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] squares= new ChessPiece [9][9];
    public ChessBoard() {
        //resetBoard();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()][position.getColumn()] = piece;
        //throw new RuntimeException("Not implemented");
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()][position.getColumn()];

        //throw new RuntimeException("Not implemented");
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //the board is 8 accoss and 8 up
        //adds black pieces
        for (int i = 1; i < 8; i++) {
            ChessPiece pawn = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            addPiece(new ChessPosition(7, i), pawn);
            if (i ==1){
                ChessPiece rook = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
                addPiece(new ChessPosition(8, i), rook);
                addPiece(new ChessPosition(8, i+7), rook);
            }
            if (i ==2){
                ChessPiece knight = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
                addPiece(new ChessPosition(8, i), knight);
                addPiece(new ChessPosition(8, i+5), knight);
            }
            if (i ==3){
                ChessPiece bishop = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
                addPiece(new ChessPosition(8, i), bishop);
                addPiece(new ChessPosition(8, i+3), bishop);
            }
        }
        ChessPiece king = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        ChessPiece queen = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        addPiece(new ChessPosition(8, 4), queen);
        addPiece(new ChessPosition(8, 5), king);


        //adds white pieces
        for (int i = 0; i < 8; i++) {
            ChessPiece pawn = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            addPiece(new ChessPosition(7, i), pawn);
            if (i ==1){
                ChessPiece rook = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
                addPiece(new ChessPosition(1, i), rook);
                addPiece(new ChessPosition(1, i+7), rook);
            }
            if (i ==2){
                ChessPiece knight = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
                addPiece(new ChessPosition(1, i), knight);
                addPiece(new ChessPosition(1, i+5), knight);
            }
            if (i ==3){
                ChessPiece bishop = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
                addPiece(new ChessPosition(1, i), bishop);
                addPiece(new ChessPosition(1, i+3), bishop);
            }
        }
        ChessPiece king2 = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        ChessPiece queen2 = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        addPiece(new ChessPosition(1, 4), queen2);
        addPiece(new ChessPosition(1, 5), king2);
        System.out.print(toString());
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "squares=" + Arrays.toString(squares) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
