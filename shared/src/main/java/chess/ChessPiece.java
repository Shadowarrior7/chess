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
            int[][] kingMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};
            return checkForMoves(kingMoves, board, myPosition, false);
        } else if (pieceType.equals(PieceType.QUEEN)) {
            int[][] queenMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};
            return checkForMoves(queenMoves, board, myPosition, true);
        } else if (pieceType.equals(PieceType.BISHOP)) {
            int[][] bishopMoves = {{1, -1}, {-1, 1}, {1, 1}, {-1, -1}};
            return checkForMoves(bishopMoves, board, myPosition, true);
        } else if (pieceType.equals(PieceType.KNIGHT)) {
            int[][] knightMoves = {{2, 1}, {2, -1}, {1, -2}, {1, 2}, {-1, -2}, {-2, -1}, {-1, 2}, {-2, 1}};
            return checkForMoves(knightMoves, board, myPosition, false);
        } else if (pieceType.equals(PieceType.ROOK)) {
            int[][] rookMoves = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            return checkForMoves(rookMoves, board, myPosition, true);
        } else if (pieceType.equals(PieceType.PAWN)) {
            //black move, black right, black left
            int[][] pawnMovesBlack = {{-1, 0}, {-2, 0}, {-1, -1}, {-1, 1}};
            //white move, white right, white left
            int[][] pawnMovesWhite = {{1, 0}, {2, 0}, {1, 1}, {1, -1}};

            if (ChessGame.TeamColor.BLACK.equals(getTeamColor())) {
                return checkForMoves(pawnMovesBlack, board, myPosition, false);
            } else if (ChessGame.TeamColor.WHITE.equals(getTeamColor())) {
                return checkForMoves(pawnMovesWhite, board, myPosition, false);
            }
        }
        //return null;moves
        throw new RuntimeException("pieceMoves");
    }

    public Collection<ChessMove> checkForMoves(int[][] possibilities, ChessBoard board,
                                               ChessPosition myPosition, Boolean iterative) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor myColor = getTeamColor();

        if (getIterative(iterative)) {
            for (int[] move : possibilities) {
                //set cord for current position
                int currentRow = myPosition.getRow();
                int currentColumn = myPosition.getColumn();

                while (true) {
                    //add the move to position
                    currentRow += move[0];
                    currentColumn += move[1];

                    //checks if piece is off the board
                    //System.out.println(currentRow + " " + currentColumn);
                    if (currentRow < 1 || currentRow > 8 || currentColumn < 1 || currentColumn > 8) {
                        //System.out.println("out of bounds");
                        break;
                    }
                    //check to see if there is a piece there
                    ChessPosition newPosition = new ChessPosition(currentRow, currentColumn);
                    //ChessPosition check = new ChessPosition(currentRow-1, currentColumn-1);
                    //System.out.println(newPosition);
                    if (board.getPiece(newPosition) != null) {
                        extract1(board, myPosition, newPosition, myColor, moves);
                        break;
                    }
                    //System.out.println("no piece there");
                    ChessMove moveToAdd = new ChessMove(myPosition, newPosition, null);
                    moves.add(moveToAdd);
                }
            }
        } else {
            if (getPieceType().equals(PieceType.PAWN)) {
                extract2(possibilities, board, myPosition, moves, myColor);

            } else {
                extract3(possibilities, board, myPosition, myColor, moves);
            }
        }
        return moves;
    }

    private static void extract3(int[][] possibilities, ChessBoard board,
                                 ChessPosition myPosition, ChessGame.TeamColor myColor, Collection<ChessMove> moves) {
        for (int[] move : possibilities) {
            while (true) {
                int currentColumn = myPosition.getColumn();
                int currentRow = myPosition.getRow() + move[0];
                currentColumn += move[1];
                if (currentRow < 1 || currentRow > 8 || currentColumn < 1 || currentColumn > 8) {
                    break;
                }
                ChessPosition newPosition = new ChessPosition(currentRow, currentColumn);
                if (extracted2(board, myPosition, myColor, moves, newPosition)){
                    break;
                }
                ChessMove moveToAdd = new ChessMove(myPosition, newPosition, null);
                moves.add(moveToAdd);
                break;
            }
        }
    }

    private static boolean extracted2(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor,
                                      Collection<ChessMove> moves, ChessPosition newPosition) {
        if (board.getPiece(newPosition) != null) {
            ChessGame.TeamColor piece = board.getPiece(newPosition).getTeamColor();
            if (piece.equals(myColor)) {
                return true;
            } else {
                ChessMove moveToAdd = new ChessMove(myPosition, newPosition, null);
                moves.add(moveToAdd);
                return true;
            }

        }
        return false;
    }

    private void extract2(int[][] possibilities, ChessBoard board, ChessPosition myPosition,
                          Collection<ChessMove> moves, ChessGame.TeamColor myColor) {
        int firstMove = 0;
        PieceType promotion = null;
        for (int[] move : possibilities) {
            ++firstMove; //keeps track of which move is happening
            while (true) {
                //sets current pos
                int currentRow = myPosition.getRow();
                int currentColumn = myPosition.getColumn();
                //makes the move
                currentRow += move[0];
                currentColumn += move[1];
                //checks for out of bounds
                if (currentRow < 1 || currentRow > 8 || currentColumn < 1 || currentColumn > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(currentRow, currentColumn);
                promotion = getPieceType(board, myPosition, moves, myColor, firstMove, newPosition, promotion, currentRow);
                break;
            }
        }
    }

    private PieceType getPieceType(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
                                   ChessGame.TeamColor myColor, int firstMove, ChessPosition newPosition,
                                   PieceType promotion, int currentRow) {
        if (firstMove == 1) {
            if (board.getPiece(newPosition) != null) {
                return promotion;
            } else {
                promotion = getPieceType(myPosition, moves, promotion, currentRow, newPosition);
            }
        } else if (firstMove == 2) {
            //checks if in the starting pos
            if ((myPosition.getRow() == 2 && getTeamColor().equals(ChessGame.TeamColor.WHITE)) ||
                    (myPosition.getRow() == 7 && getTeamColor().equals(ChessGame.TeamColor.BLACK))) {
                promotion = getPieceType(board, myPosition, moves, newPosition, promotion, currentRow);
                return promotion;
            }

        } else {
            if (board.getPiece(newPosition) != null) {
                if (!board.getPiece(newPosition).getTeamColor().equals(myColor)) {
                    promotion = getPieceType(myPosition, moves, promotion, currentRow, newPosition);
                }
            }
        }
        return promotion;
    }

    private PieceType getPieceType(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves,
                                   ChessPosition newPosition, PieceType promotion, int currentRow) {
        if (board.getPiece(newPosition) != null) {
            return promotion;
        }

        if (getTeamColor().equals(ChessGame.TeamColor.WHITE)) {
            ChessPosition secondCheck = new ChessPosition(myPosition.getRow() + 1,
                    myPosition.getColumn());
            if (board.getPiece(secondCheck) != null) {
                System.out.println("white pawn cannot double jump");
                return promotion;
            }
        } else if (getTeamColor().equals(ChessGame.TeamColor.BLACK)) {
            ChessPosition secondCheck = new ChessPosition(myPosition.getRow() - 1,
                    myPosition.getColumn());
            if (board.getPiece(secondCheck) != null) {
                System.out.println("black pawn cannot double jump");
                return promotion;
            }
        }

        promotion = getPieceType(myPosition, moves, promotion, currentRow, newPosition);
        return promotion;
    }

    private static void extract1(ChessBoard board, ChessPosition myPosition,
                                 ChessPosition newPosition, ChessGame.TeamColor myColor, Collection<ChessMove> moves) {
        //System.out.println("piece there");
        ChessGame.TeamColor pieceColor = board.getPiece(newPosition).getTeamColor();
        //checks if you can take the piece
        if (pieceColor.equals(myColor)) {
            //System.out.println("same color");
            //System.out.println(board.getPiece(newPosition).getPieceType());
            return;
        } else {
            //System.out.println("different color");
            ChessMove moveToAdd = new ChessMove(myPosition, newPosition, null);
            moves.add(moveToAdd);
            return;
        }
    }

    private static Boolean getIterative(Boolean iterative) {
        return iterative;
    }

    public PieceType getPieceType(ChessPosition myPosition, Collection<ChessMove> moves,
                                  PieceType promotion, int currentRow, ChessPosition newPosition) {
        if (currentRow == 1 || currentRow == 8) {
            promotion = PieceType.QUEEN;
            moves.add(new ChessMove(myPosition, newPosition, promotion));
            promotion = PieceType.ROOK;
            moves.add(new ChessMove(myPosition, newPosition, promotion));
            promotion = PieceType.BISHOP;
            moves.add(new ChessMove(myPosition, newPosition, promotion));
            promotion = PieceType.KNIGHT;
            moves.add(new ChessMove(myPosition, newPosition, promotion));
            promotion = null;
            return promotion;
        } else {
            ChessMove moveToAdd = new ChessMove(myPosition, newPosition, promotion);
            moves.add(moveToAdd);
            return promotion;
        }
        //return promotion;
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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
}

