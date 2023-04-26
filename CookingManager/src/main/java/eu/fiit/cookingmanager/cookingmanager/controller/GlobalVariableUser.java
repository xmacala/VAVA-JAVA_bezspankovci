package eu.fiit.cookingmanager.cookingmanager.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GlobalVariableUser {

    private static int account_id;
    private static String login;
    private static int user_id;
    private static String name;
    private static String surname;
    private static String email;
    private static int user_type;

    public static void setUser(int id_user, Connection conn) throws SQLException {

        String query = "SELECT * FROM public.user WHERE id=(?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, id_user);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            user_id = rs.getInt("id");
            name = rs.getString("name");
            surname = rs.getString("surname");
            email = rs.getString("email");
            user_type = rs.getInt("user_type_id");
            query = "SELECT * FROM public.account WHERE user_id=(?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id_user);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                account_id = rs.getInt("id");
                login = rs.getString("login");
            }
        }


    }

    public static int getUserId() {
        return user_id;
    }
    public static String getName() {
        return name;
    }
    public static int getType(){
        return user_type;
    }
    public static String getSurname() {
        return surname;
    }
    public static String getEmail() {
        return email;
    }
    public static int getAccountId() {
        return account_id;
    }
    public static String getLogin() {
        return login;
    }
}
