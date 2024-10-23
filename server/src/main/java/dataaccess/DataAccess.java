package dataaccess;

public interface DataAccess {
    add_UserData() throws DataAccessException;
    add_AuthData() throws DataAccessException;
    find_username() throws  DataAccessException;
    get_password() throws DataAccessException;
    void delete_auth() throws DataAccessException;
    get_gameData() throws DataAccessException;
    get_authData() throws DataAccessException;
    create_game() throws DataAccessException;
    void delete_game() throws DataAccessException;
    void update_game() throws DataAccessException;
    void delete_all() throws DataAccessException;
}
