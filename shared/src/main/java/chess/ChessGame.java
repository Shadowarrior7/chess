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

        ChessBoard new_board = new ChessBoard();
        ChessBoard board = getBoard();
        new_board.setSquares(copyBoard(board));
        ChessPiece piece = board.getPiece(startPosition);
        ChessGame.TeamColor my_color = piece.getTeamColor();
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(current_board, startPosition); //gives a collection of moves the pieces can Physically make
        Collection<ChessMove> validMoves  = new ArrayList<>();
        for (ChessMove move : moves) {
            if(current_board.getPiece(move.getEndPosition()) != null) {
                board_changer(move.getEndPosition());
            }
            current_board.addPiece(move.getEndPosition(), piece);
            board_changer(startPosition);
            if (!isInCheck(my_color) && !isInCheckmate(my_color) && !isInStalemate(my_color)) {
                validMoves.add(move);
            }
            current_board.setSquares(copyBoard(new_board));
            //board.setSquares(copyBoard(current_board));

        }
        return validMoves;
    }

    public ChessPiece[][] copyBoard(ChessBoard board) {
        ChessPiece[][] new_board = new ChessPiece[9][9];
        ChessPiece[][] old_board = board.getSquares();
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                new_board[i][j] = old_board[i][j];
            }
        }
        return new_board;
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
        ChessGame.TeamColor piece_color = piece.getTeamColor();
        TeamColor turn_color = getTeamTurn();

        if(turn_color != piece_color) {
            System.out.print("not your turn");
            throw new InvalidMoveException();
        }
        Collection<ChessMove> legal_moves = validMoves(move.getStartPosition());
        if(legal_moves.isEmpty()) {
            System.out.println("no legal moves");
        }
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
        if(piece.getPieceType().equals(ChessPiece.PieceType.PAWN)){
            if (move.getPromotionPiece() != null){
                ChessPiece new_type = new ChessPiece(piece_color, move.getPromotionPiece());
                current_board.addPiece(move.getEndPosition(), new_type);
            }
            else {
                current_board.addPiece(move.getEndPosition(), piece);
            }
        }
        else {
            current_board.addPiece(move.getEndPosition(), piece);
        }
        if (turn_color.equals(TeamColor.WHITE)) {
            setTeamTurn(TeamColor.BLACK);
        }
        else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    public void board_changer(ChessPosition position_to_remove) {
        ChessBoard new_board = new ChessBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition current_position = new ChessPosition(i, j);
                ChessPiece current_piece = current_board.getPiece(current_position);
                if (current_piece != null && !current_position.equals(position_to_remove)) {
                    new_board.addPiece(current_position, current_piece);
                }
            }
        }
        current_board.setSquares(copyBoard(new_board));
    }

    public Collection<ChessPosition> get_enemy_positions(TeamColor teamColor) {
        Collection<ChessPosition> positions = new ArrayList<>();
        ChessBoard board = getBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition current_position = new ChessPosition(i, j);
                ChessPiece current_piece = board.getPiece(current_position);
                if (current_piece != null && (current_piece.getTeamColor() != teamColor)) {
                    positions.add(current_position);
                }
            }
        }
        return positions;
    }

    public Collection<ChessPosition> get_friend_positions(TeamColor teamColor) {
        Collection<ChessPosition> positions = new ArrayList<>();
        ChessBoard board = getBoard();
        for (int j = 1; j < 9; ++j) {
            for (int i = 1; i < 9; ++i) {
                ChessPosition current_position = new ChessPosition(i, j);
                ChessPiece current_piece = board.getPiece(current_position);
                if (current_piece != null && (current_piece.getTeamColor() == teamColor)) {
                    positions.add(current_position);
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
        Collection<ChessPosition> enemy_positions = get_enemy_positions(teamColor);
        ChessBoard board = current_board;
        if(enemy_positions.isEmpty()) {
            System.out.println("there are no enemy's ");
            return false;
        }
        ChessPosition my_king_pos = get_king_position(teamColor);

        for(ChessPosition enemy : enemy_positions){
            ChessPiece enemy_piece = board.getPiece(enemy);
            Collection<ChessMove> enemy_moves = enemy_piece.pieceMoves(board, enemy);
            for(ChessMove move : enemy_moves){
                if (move.getEndPosition().equals(my_king_pos)) {
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
        ChessPosition my_king_pos = get_king_position(teamColor);
        Collection<ChessPosition> enemy_positions = get_enemy_positions(teamColor);
        ChessBoard board = getBoard();
        ChessBoard new_board = new ChessBoard();
        new_board.setSquares(copyBoard(board));
        if (my_king_pos == null) {
            System.out.println("king pos is null");
            return false;
        }
        ChessPiece king = board.getPiece(my_king_pos);
        Collection<ChessMove> king_moves = king.pieceMoves(getBoard(), my_king_pos);
        Collection<ChessMove> king_moves_copy = king.pieceMoves(getBoard(), my_king_pos);
        Collection<ChessPosition> friendly_pos = get_friend_positions(teamColor);
        int king_safe_moves = king_moves.size();
        for(ChessPosition friendly: friendly_pos){
            ChessPiece friendly_piece = current_board.getPiece(friendly);
            if (friendly_piece.getPieceType().equals(ChessPiece.PieceType.KING)){
                continue;
            }
            Collection<ChessMove> friendly_moves = friendly_piece.pieceMoves(current_board, friendly);
            for (ChessMove move : friendly_moves){
                if(current_board.getPiece(move.getEndPosition()) != null) {
                    board_changer(move.getEndPosition());
                }
                current_board.addPiece(move.getEndPosition(), friendly_piece);
                board_changer(move.getStartPosition());
                if(!isInCheck(teamColor)){
                    System.out.println(move);
                    current_board.setSquares(copyBoard(new_board));
                    return false;
                }
                current_board.setSquares(copyBoard(new_board));
            }
        }
        if(isInCheck(teamColor)){
            for(ChessPosition enemy: enemy_positions){
                for(ChessMove king_move : king_moves){
                    if(current_board.getPiece(king_move.getEndPosition()) != null) {
                        board_changer(king_move.getEndPosition());
                    }
                    current_board.addPiece(king_move.getEndPosition(), king);
                    board_changer(king_move.getStartPosition());
                    ChessPiece enemy_piece = current_board.getPiece(enemy);
                    Collection<ChessMove> enemy_moves = enemy_piece.pieceMoves(current_board, enemy);
                    for(ChessMove enemy_move : enemy_moves){
                        if(enemy_move.getEndPosition().equals(king_move.getEndPosition())) {
                            --king_safe_moves;
                            king_moves_copy.remove(king_move);
                            if (king_safe_moves == 0) {
                                System.out.println("in check mate");
                                return true;
                            }
                        }
                    }
                    current_board.setSquares(copyBoard(new_board));
                }
            }
            System.out.println(king_moves_copy);
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
        ChessPosition my_king_pos = get_king_position(teamColor);
        if(my_king_pos == null){
            System.out.println("Big problem");
            return false;
        }
        Collection<ChessPosition> enemy_positions = get_enemy_positions(teamColor);
        ChessPiece king_piece = current_board.getPiece(my_king_pos);
        Collection<ChessMove> king_moves = king_piece.pieceMoves(getBoard(), my_king_pos);
        int king_safe_moves = king_moves.size();
        for(ChessPosition enemy: enemy_positions){
            ChessPiece enemy_piece = current_board.getPiece(enemy);
            Collection<ChessMove> enemy_moves = enemy_piece.pieceMoves(current_board, enemy);
            for(ChessMove enemy_move : enemy_moves){
                for(ChessMove king_move : king_moves){
                    if(enemy_move.getEndPosition().equals(king_move.getEndPosition())) {
                        --king_safe_moves;
                        if (king_safe_moves == 0) {
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
                ChessPosition new_position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(new_position);
                if (piece == null) {
                    continue;
                }
                if (piece.getPieceType().equals(ChessPiece.PieceType.KING) && piece.getTeamColor().equals(teamColor)) {
                    return new_position;
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


