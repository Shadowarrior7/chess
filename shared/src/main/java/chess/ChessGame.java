package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard currentBoard;
    private TeamColor turnColor;

    public ChessGame() {
        currentBoard = new ChessBoard();
        currentBoard.resetBoard();
        turnColor = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turnColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turnColor = team;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        ChessBoard newBoard = new ChessBoard();
        ChessBoard board = getBoard();
        newBoard.setSquares(copyBoard(board));
        ChessPiece piece = board.getPiece(startPosition);
        ChessGame.TeamColor myColor = piece.getTeamColor();
        Collection<ChessMove> moves = piece.pieceMoves(currentBoard, startPosition); //gives a collection of moves the pieces can Physically make
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            if (currentBoard.getPiece(move.getEndPosition()) != null) {
                boardChanger(move.getEndPosition());
            }
            currentBoard.addPiece(move.getEndPosition(), piece);
            boardChanger(startPosition);
            if (!isInCheck(myColor) && !isInCheckmate(myColor) && !isInStalemate(myColor)) {
                validMoves.add(move);
            }
            currentBoard.setSquares(copyBoard(newBoard));
        }
        return validMoves;
    }

    public ChessPiece[][] copyBoard(ChessBoard board) {
        ChessPiece[][] newBoard = new ChessPiece[9][9];
        ChessPiece[][] oldBoard = board.getSquares();
        for (int i = 1; i < 9; i++) {
            System.arraycopy(oldBoard[i], 1, newBoard[i], 1, 8);
        }
        return newBoard;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            System.out.println("Houston, we have a problem");
            throw new InvalidMoveException();
        }
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        TeamColor teamColor = getTeamTurn();

        if (teamColor != pieceColor) {
            System.out.print("not your turn");
            throw new InvalidMoveException();
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves.isEmpty()) {
            System.out.println("no legal moves");
        }
        boolean isMoveLegal = false;
        for (ChessMove legalMove : legalMoves) {
            if (move.getEndPosition().equals(legalMove.getEndPosition())) {
                isMoveLegal = true;
                break;
            }
        }
        if (!isMoveLegal) {
            throw new InvalidMoveException();
        }

        //this should execute the move, but idk
        boardChanger(move.getStartPosition());
        if (piece.getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            if (move.getPromotionPiece() != null) {
                ChessPiece newType = new ChessPiece(pieceColor, move.getPromotionPiece());
                currentBoard.addPiece(move.getEndPosition(), newType);
            } else {
                currentBoard.addPiece(move.getEndPosition(), piece);
            }
        } else {
            currentBoard.addPiece(move.getEndPosition(), piece);
        }
        if (teamColor.equals(TeamColor.WHITE)) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    public void boardChanger(ChessPosition positionToRemove) {
        ChessBoard newBoard = new ChessBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition currentPosition = new ChessPosition(i, j);
                ChessPiece currentPiece = currentBoard.getPiece(currentPosition);
                if (currentPiece != null && !currentPosition.equals(positionToRemove)) {
                    newBoard.addPiece(currentPosition, currentPiece);
                }
            }
        }
        currentBoard.setSquares(copyBoard(newBoard));
    }

    public Collection<ChessPosition> getEnemyPositions(TeamColor teamColor) {
        Collection<ChessPosition> positions = new ArrayList<>();
        ChessBoard board = getBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition currentPosition = new ChessPosition(i, j);
                ChessPiece currentPiece = board.getPiece(currentPosition);
                if (currentPiece != null && (currentPiece.getTeamColor() != teamColor)) {
                    positions.add(currentPosition);
                }
            }
        }
        return positions;
    }

    public Collection<ChessPosition> getFriendPositions(TeamColor teamColor) {
        Collection<ChessPosition> positions = new ArrayList<>();
        ChessBoard board = getBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition chessPosition = new ChessPosition(i, j);
                ChessPiece currentPiece = board.getPiece(chessPosition);
                if (currentPiece != null && (currentPiece.getTeamColor() == teamColor)) {
                    positions.add(chessPosition);
                }
            }
        }
        return positions;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessPosition> enemyPositions = getEnemyPositions(teamColor);
        ChessBoard board = currentBoard;
        if (enemyPositions.isEmpty()) {
            System.out.println("there are no enemy's ");
            return false;
        }
        ChessPosition myKingPos = getKingPosition(teamColor);

        for (ChessPosition enemy : enemyPositions) {
            ChessPiece enemyPiece = board.getPiece(enemy);
            Collection<ChessMove> enemyMoves = enemyPiece.pieceMoves(board, enemy);
            for (ChessMove move : enemyMoves) {
                if (move.getEndPosition().equals(myKingPos)) {
                    System.out.println("in check");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition myKingPos = getKingPosition(teamColor);
        Collection<ChessPosition> enemyPositions = getEnemyPositions(teamColor);
        ChessBoard board = getBoard();
        ChessBoard newBoard = new ChessBoard();
        newBoard.setSquares(copyBoard(board));
        if (myKingPos == null) {
            System.out.println("king pos is null");
            return false;
        }
        ChessPiece king = board.getPiece(myKingPos);
        Collection<ChessMove> kingMoves = king.pieceMoves(getBoard(), myKingPos);
        Collection<ChessMove> kingMovesCopy = king.pieceMoves(getBoard(), myKingPos);
        Collection<ChessPosition> friendPositions = getFriendPositions(teamColor);
        int kingSafeMoves = kingMoves.size();
        for (ChessPosition friendly : friendPositions) {
            ChessPiece friendlyPiece = currentBoard.getPiece(friendly);
            if (friendlyPiece.getPieceType().equals(ChessPiece.PieceType.KING)) {
                continue;
            }
            Collection<ChessMove> friendlyMoves = friendlyPiece.pieceMoves(currentBoard, friendly);
            for (ChessMove move : friendlyMoves) {
                if (currentBoard.getPiece(move.getEndPosition()) != null) {
                    boardChanger(move.getEndPosition());
                }
                currentBoard.addPiece(move.getEndPosition(), friendlyPiece);
                boardChanger(move.getStartPosition());
                if (!isInCheck(teamColor)) {
                    System.out.println(move);
                    currentBoard.setSquares(copyBoard(newBoard));
                    return false;
                }
                currentBoard.setSquares(copyBoard(newBoard));
            }
        }
        if (isInCheck(teamColor)) {
            return loopFunction(enemyPositions, kingMoves, king, kingSafeMoves, kingMovesCopy, newBoard);
        } else {
            return false;
        }
    }

    public boolean loopFunction(Collection<ChessPosition> enemyPositions, Collection<ChessMove> kingMoves,
                                ChessPiece king, int kingSafeMoves,
                                Collection<ChessMove> kingMovesCopy, ChessBoard newBoard) {
        for (ChessPosition enemy : enemyPositions) {
            for (ChessMove kingMove : kingMoves) {
                if (currentBoard.getPiece(kingMove.getEndPosition()) != null) {
                    boardChanger(kingMove.getEndPosition());
                }
                currentBoard.addPiece(kingMove.getEndPosition(), king);
                boardChanger(kingMove.getStartPosition());
                ChessPiece enemyPiece = currentBoard.getPiece(enemy);
                Collection<ChessMove> enemyMoves = enemyPiece.pieceMoves(currentBoard, enemy);
                for (ChessMove enemyMove : enemyMoves) {
                    if (enemyMove.getEndPosition().equals(kingMove.getEndPosition())) {
                        --kingSafeMoves;
                        kingMovesCopy.remove(kingMove);
                        if (extract1(kingSafeMoves)) {
                            return true;
                        }
                    }
                }

                currentBoard.setSquares(copyBoard(newBoard));

            }
        }
        System.out.println(kingMovesCopy);
        return false;
    }

    private static boolean extract1(int kingSafeMoves) {
        if (kingSafeMoves == 0) {
            System.out.println("in check mate");
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition myKingPos = getKingPosition(teamColor);
        if (myKingPos == null) {
            System.out.println("Big problem");
            return false;
        }
        Collection<ChessPosition> enemyPositions = getEnemyPositions(teamColor);
        ChessPiece kingPiece = currentBoard.getPiece(myKingPos);
        Collection<ChessMove> kingMoves = kingPiece.pieceMoves(getBoard(), myKingPos);
        int kingSafeMoves = kingMoves.size();
        for (ChessPosition enemy : enemyPositions) {
            ChessPiece enemyPiece = currentBoard.getPiece(enemy);
            Collection<ChessMove> enemyMoves = enemyPiece.pieceMoves(currentBoard, enemy);
            for (ChessMove enemyMove : enemyMoves) {
                for (ChessMove kingMove : kingMoves) {
                    if (enemyMove.getEndPosition().equals(kingMove.getEndPosition())) {
                        --kingSafeMoves;
                        if (kingSafeMoves == 0) {
                            System.out.println("in stale mate");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public ChessPosition getKingPosition(TeamColor teamColor) {
        ChessBoard board = getBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition newPosition = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(newPosition);
                if (piece == null) {
                    continue;
                }
                if (piece.getPieceType().equals(ChessPiece.PieceType.KING) && piece.getTeamColor().equals(teamColor)) {
                    return newPosition;
                }
            }
        }
        System.out.println("problem with finding the king");
        return null;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        currentBoard = board;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }
}


