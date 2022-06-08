package auth;

import java.sql.ResultSet;
import java.sql.SQLException;


public class SimpleAuthService  {

   DataBase dataBase ;

   public SimpleAuthService(){
       dataBase = new DataBase();
   }


    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickName = null;
        try {
            DataBase.getNicknameStatement.setString(1, login);
            DataBase.getNicknameStatement.setString(2, password);
            nickName = DataBase.getNicknameStatement.executeQuery().getString("nickName");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nickName;
    }



    public boolean registration(String login, String password, String nickname) {

        try {
            ResultSet resultSet = DataBase.getLogPassNicknameStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("login").equals(login) ||
                        resultSet.getString("nickName").equals(nickname)) {
                    return false;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        try {
            DataBase.regInsertStatement.setString(1, login);
            DataBase.regInsertStatement.setString(2, password);
            DataBase.regInsertStatement.setString(3, nickname);
            DataBase.regInsertStatement.addBatch();
            DataBase.regInsertStatement.executeBatch();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }
}



