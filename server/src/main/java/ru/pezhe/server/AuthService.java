package ru.pezhe.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public boolean checkCredentials(String login, String password) {
        Connection connection = DBConnector.getConnection();
        try {
            PreparedStatement statement =
                    connection.prepareStatement("SELECT password FROM entry WHERE login = ?");
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            return rs.next() && password.equals(rs.getString("password"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnector.close(connection);
        }
    }

    public String getNickname(String login) {
        Connection connection = DBConnector.getConnection();
        try {
            PreparedStatement statement =
                    connection.prepareStatement("SELECT nickname FROM entry WHERE login = ?");
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return rs.getString("nickname");
            return "Anonymous";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBConnector.close(connection);
        }
    }

    public boolean setNickname(String login, String newNickname) {
        Connection connection = DBConnector.getConnection();
        try {
            PreparedStatement statement =
                    connection.prepareStatement("UPDATE entry SET nickname = ? WHERE login = ?");
            statement.setString(1, newNickname);
            statement.setString(2, login);
            if (statement.executeUpdate() == 1) {
                connection.commit();
                return true;
            } else {
                DBConnector.rollback(connection);
                return false;
            }
        } catch (SQLException e) {
            DBConnector.rollback(connection);
            throw new RuntimeException(e);
        } finally {
            DBConnector.close(connection);
        }
    }

}

