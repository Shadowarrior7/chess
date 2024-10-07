package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard current_board;
    private TeamColor turn_color;
    public ChessGame(ChessBoard current_board, TeamColor turn_color ) {
            this.current_board = current_board;
            this.turn_color = turn_color;
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
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);
        ChessGame.TeamColor my_color = piece.getTeamColor();
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition); //gives a collection of moves the pieces can Physically make
        for (ChessMove move : moves) {
            board.addPiece(move.getEndPosition(), piece);
            if (isInCheck(my_color) || isInCheckmate(my_color) || isInStalemate(my_color)) {
                moves.remove(move);
            }
        }
        return moves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(move.getEndPosition());
        ChessGame.TeamColor piece_color = piece.getTeamColor();
        TeamColor turn_color = getTeamTurn();

        if(turn_color != piece_color) {
            System.out.print("not your turn");
            throw new InvalidMoveException();
        }
        Collection<ChessMove> legal_moves = validMoves(move.getStartPosition());
        boolean is_move_legal = false;
        for(ChessMove legal_move : legal_moves) {
            if (move.getEndPosition().equals(legal_move.getEndPosition())) {
                is_move_legal = true;
                break;
            }
        }
        if(!is_move_legal) {
            throw new InvalidMoveException();
        }

        //this should execute the move, but idk
        board_changer(move.getStartPosition());
        current_board.addPiece(move.getEndPosition(), piece);
    }

    public void board_changer(ChessPosition position_to_remove) {

        ChessBoard new_board = new ChessBoard();
        int j = 0;
        for(int i =0; i < 8; ++i){
            ChessPosition current_position = new ChessPosition(i, j);
            ChessPiece current_piece = current_board.getPiece(current_position);
            if (current_piece != null && !current_position.equals(position_to_remove)) {
                new_board.addPiece(current_position, current_piece);
            }
        }
        current_board = new_board;
    }

    public Collection<ChessPosition> get_enemy_positions(TeamColor teamColor) {
        Collection<ChessPosition> positions = new HashSet<>();
        ChessBoard board = getBoard();
        int j =0;
        for(int i =0; i < 8; ++i){
            ChessPosition current_position = new ChessPosition(i, j);
            ChessPiece current_piece = board.getPiece(current_position);
            if (current_piece !=  null && current_piece.getTeamColor() != teamColor) {
                positions.add(current_position);
            }
            ++j;
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
        Collection<ChessPosition> enemy_positions = get_enemy_positions(teamColor);
        ChessPosition my_king_pos = get_king_position(teamColor);

        for(ChessPosition enemy : enemy_positions){
            ChessPiece enemy_piece = current_board.getPiece(enemy);
            Collection<ChessMove> enemy_moves = enemy_piece.pieceMoves(current_board, enemy);
            for(ChessMove move : enemy_moves){
                if (move.getEndPosition().equals(my_king_pos)) {
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
        ChessPosition my_king_pos = get_king_position(teamColor);
        Collection<ChessPosition> enemy_positions = get_enemy_positions(teamColor);
        Collection<ChessMove> king_moves = getBoard().getPiece(my_king_pos).pieceMoves(getBoard(), my_king_pos);
        if(isInCheck(teamColor)){
            for(ChessPosition enemy: enemy_positions){
                ChessPiece enemy_piece = current_board.getPiece(enemy);
                Collection<ChessMove> enemy_moves = enemy_piece.pieceMoves(current_board, enemy);
                for(ChessMove enemy_move : enemy_moves){
                    for(ChessMove king_move : king_moves){
                        if(!enemy_move.getEndPosition().equals(king_move.getEndPosition())) {
                            continue;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
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
        throw new RuntimeException("Not implemented");
    }


    public ChessPosition get_king_position(TeamColor teamColor) {
        ChessBoard board = getBoard();
        int j =0;
        for (int i = 0; i < 8; i++) {
            ChessPosition new_position = new ChessPosition(i, j);
            ChessPiece piece = board.getPiece(new_position);
            if (piece.getPieceType().equals(ChessPiece.PieceType.KING) && piece.getTeamColor().equals(teamColor)) {
                return new_position;
            }
            ++j;
        }
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


