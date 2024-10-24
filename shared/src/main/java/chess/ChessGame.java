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
    private ChessBoard current_board;
    private TeamColor turn_color;
    public ChessGame() {
        current_board = new ChessBoard();
        current_board.resetBoard();
        turn_color = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn_color;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn_color = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
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
        ChessGame.TeamColor my_color = piece.getTeamColor();
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(current_board, startPosition); //gives a collection of moves the pieces can Physically make
        Collection<ChessMove> validMoves  = new ArrayList<>();
        for (ChessMove move : moves) {
            if(current_board.getPiece(move.getEndPosition()) != null) {
                boardChanger(move.getEndPosition());
            }
            current_board.addPiece(move.getEndPosition(), piece);
            boardChanger(startPosition);
            if (!isInCheck(my_color) && !isInCheckmate(my_color) && !isInStalemate(my_color)) {
                validMoves.add(move);
            }
            current_board.setSquares(copyBoard(newBoard));
            //board.setSquares(copyBoard(current_board));

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
        if (piece == null){
            System.out.println("Houston, we have a problem");
            throw new InvalidMoveException();
        }
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        TeamColor teamColor = getTeamTurn();

        if(teamColor != pieceColor) {
            System.out.print("not your turn");
            throw new InvalidMoveException();
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if(legalMoves.isEmpty()) {
            System.out.println("no legal moves");
        }
        boolean isMoveLegal = false;
        for(ChessMove legalMove : legalMoves) {
            if (move.getEndPosition().equals(legalMove.getEndPosition())) {
                isMoveLegal = true;
                break;
            }
        }
        if(!isMoveLegal) {
            throw new InvalidMoveException();
        }

        //this should execute the move, but idk
        boardChanger(move.getStartPosition());
        if(piece.getPieceType().equals(ChessPiece.PieceType.PAWN)){
            if (move.getPromotionPiece() != null){
                ChessPiece newType = new ChessPiece(pieceColor, move.getPromotionPiece());
                current_board.addPiece(move.getEndPosition(), newType);
            }
            else {
                current_board.addPiece(move.getEndPosition(), piece);
            }
        }
        else {
            current_board.addPiece(move.getEndPosition(), piece);
        }
        if (teamColor.equals(TeamColor.WHITE)) {
            setTeamTurn(TeamColor.BLACK);
        }
        else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    public void boardChanger(ChessPosition position_to_remove) {
        ChessBoard newBoard = new ChessBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition currentPosition = new ChessPosition(i, j);
                ChessPiece currentPiece = current_board.getPiece(currentPosition);
                if (currentPiece != null && !currentPosition.equals(position_to_remove)) {
                    newBoard.addPiece(currentPosition, currentPiece);
                }
            }
        }
        current_board.setSquares(copyBoard(newBoard));
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
        ChessBoard board = current_board;
        if(enemyPositions.isEmpty()) {
            System.out.println("there are no enemy's ");
            return false;
        }
        ChessPosition myKingPos = get_king_position(teamColor);

        for(ChessPosition enemy : enemyPositions){
            ChessPiece enemyPiece = board.getPiece(enemy);
            Collection<ChessMove> enemyMoves = enemyPiece.pieceMoves(board, enemy);
            for(ChessMove move : enemyMoves){
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
        ChessPosition myKingPos = get_king_position(teamColor);
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
        for(ChessPosition friendly: friendPositions){
            ChessPiece friendlyPiece = current_board.getPiece(friendly);
            if (friendlyPiece.getPieceType().equals(ChessPiece.PieceType.KING)){
                continue;
            }
            Collection<ChessMove> friendlyMoves = friendlyPiece.pieceMoves(current_board, friendly);
            for (ChessMove move : friendlyMoves){
                if(current_board.getPiece(move.getEndPosition()) != null) {
                    boardChanger(move.getEndPosition());
                }
                current_board.addPiece(move.getEndPosition(), friendlyPiece);
                boardChanger(move.getStartPosition());
                if(!isInCheck(teamColor)){
                    System.out.println(move);
                    current_board.setSquares(copyBoard(newBoard));
                    return false;
                }
                current_board.setSquares(copyBoard(newBoard));
            }
        }
        if(isInCheck(teamColor)){
            for(ChessPosition enemy: enemyPositions){
                for(ChessMove kingMove : kingMoves){
                    if(current_board.getPiece(kingMove.getEndPosition()) != null) {
                        boardChanger(kingMove.getEndPosition());
                    }
                    current_board.addPiece(kingMove.getEndPosition(), king);
                    boardChanger(kingMove.getStartPosition());
                    ChessPiece enemyPiece = current_board.getPiece(enemy);
                    Collection<ChessMove> enemy_moves = enemyPiece.pieceMoves(current_board, enemy);
                    for(ChessMove enemyMove : enemy_moves){
                        if(enemyMove.getEndPosition().equals(kingMove.getEndPosition())) {
                            --kingSafeMoves;
                            kingMovesCopy.remove(kingMove);
                            if (kingSafeMoves == 0) {
                                System.out.println("in check mate");
                                return true;
                            }
                        }
                    }
                    current_board.setSquares(copyBoard(newBoard));
                }
            }
            System.out.println(kingMovesCopy);
            return false;
        }
        else{
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition myKingPos = get_king_position(teamColor);
        if(myKingPos == null){
            System.out.println("Big problem");
            return false;
        }
        Collection<ChessPosition> enemyPositions = getEnemyPositions(teamColor);
        ChessPiece kingPiece = current_board.getPiece(myKingPos);
        Collection<ChessMove> kingMoves = kingPiece.pieceMoves(getBoard(), myKingPos);
        int kingSafeMoves = kingMoves.size();
        for(ChessPosition enemy: enemyPositions){
            ChessPiece enemyPiece = current_board.getPiece(enemy);
            Collection<ChessMove> enemyMoves = enemyPiece.pieceMoves(current_board, enemy);
            for(ChessMove enemyMove : enemyMoves){
                for(ChessMove kingMove : kingMoves){
                    if(enemyMove.getEndPosition().equals(kingMove.getEndPosition())) {
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


    public ChessPosition get_king_position(TeamColor teamColor) {
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
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        current_board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return current_board;
    }
}


