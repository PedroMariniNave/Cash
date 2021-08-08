package com.zpedroo.voltzcash.mysql;

import com.zpedroo.voltzcash.player.PlayerData;
import com.zpedroo.voltzcash.purchases.Purchase;
import com.zpedroo.voltzcash.transactions.Transaction;
import com.zpedroo.voltzcash.transactions.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DBManager {

    public void save(PlayerData data) {
        if (contains(data.getUUID().toString(), "uuid")) {
            String query = "UPDATE `" + DBConnection.TABLE + "` SET" +
                    "`uuid`='" + data.getUUID().toString() + "', " +
                    "`cash`='" + data.getCash().toString() + "', " +
                    "`transactions`='" + serializeTransactions(data.getTransactions()) + "', " +
                    "`purchases`='" + serializePurchases(data.getPurchases()) + "' " +
                    "WHERE `uuid`='" + data.getUUID().toString() + "';";
            executeUpdate(query);
            return;
        }

        String query = "INSERT INTO `" + DBConnection.TABLE + "` (`uuid`, `cash`, `transactions`, `purchases`) VALUES " +
                "('" + data.getUUID().toString() + "', " +
                "'" + data.getCash().toString() + "', " +
                "'" + serializeTransactions(data.getTransactions()) + "', " +
                "'" + serializePurchases(data.getPurchases()) + "');";
        executeUpdate(query);
    }

    public HashMap<UUID, PlayerData> loadData() {
        HashMap<UUID, PlayerData> ret = new HashMap<>(2048);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger cash = result.getBigDecimal(2).toBigInteger();
                List<Transaction> transactions = deserializeTransactions(result.getString(3));
                List<Purchase> purchases = deserializePurchases(result.getString(4));

                ret.put(uuid, new PlayerData(uuid, cash, transactions, purchases));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return ret;
    }

    public PlayerData load(OfflinePlayer player) {
        if (!contains(player.getUniqueId().toString(), "uuid")) {
            PlayerData data = new PlayerData(player.getUniqueId(), BigInteger.ZERO, null, null);
            data.setQueue(true);
            return data;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` WHERE `uuid`='" + player.getUniqueId().toString() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger cash = result.getBigDecimal(2).toBigInteger();
                List<Transaction> transactions = deserializeTransactions(result.getString(3));
                List<Purchase> purchases = deserializePurchases(result.getString(4));

                return new PlayerData(uuid, cash, transactions, purchases);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return null;
    }

    public List<PlayerData> getTop() {
        List<PlayerData> top = new ArrayList<>(10);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "` ORDER BY `cash` DESC LIMIT 10;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                BigInteger cash = result.getBigDecimal(2).toBigInteger();
                List<Transaction> transactions = deserializeTransactions(result.getString(3));
                List<Purchase> purchases = deserializePurchases(result.getString(4));

                top.add(new PlayerData(uuid, cash, transactions, purchases));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return top;
    }

    public Boolean contains(String value, String column) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT `" + column + "` FROM `" + DBConnection.TABLE + "` WHERE `" + column + "`='" + value + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, result, preparedStatement, null);
        }

        return false;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnections(connection, null, null, statement);
        }
    }

    protected void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`uuid` VARCHAR(255) NOT NULL, `cash` DECIMAL(40,0) NOT NULL DEFAULT '0', `transactions` LONGTEXT NOT NULL, `purchases` LONGTEXT NOT NULL, PRIMARY KEY(`uuid`));";
        executeUpdate(query);
    }

    private void closeConnections(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException, NullPointerException {
        return DBConnection.getInstance().getConnection();
    }

    private String serializePurchases(List<Purchase> purchases) {
        StringBuilder serialized = new StringBuilder();
        for (Purchase purchase : purchases) {
            serialized.append(purchase.getName()).append("#")
                    .append(purchase.getAmount().toString()).append("#")
                    .append(purchase.getPrice().toString()).append("#")
                    .append(purchase.getDate().getTime()).append("#")
                    .append(purchase.getID()).append(",");
        }

        return serialized.toString();
    }

    private List<Purchase> deserializePurchases(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        List<Purchase> purchases = new LinkedList<>();

        String[] split = serialized.split(",");

        for (String str : split) {
            String[] strSplit = str.split("#");

            String name = strSplit[0];
            Integer amount = Integer.parseInt(strSplit[1]);
            BigInteger price = new BigInteger(strSplit[2]);
            Date date = new Date(Long.parseLong(strSplit[3]));
            Integer id = Integer.parseInt(strSplit[4]);

            purchases.add(new Purchase(name, amount, price, date, id));
        }

        return purchases;
    }

    private String serializeTransactions(List<Transaction> transactions) {
        StringBuilder serialized = new StringBuilder();
        for (Transaction transaction : transactions) {
            serialized.append(transaction.getActor().getName()).append("#")
                    .append(transaction.getTarget().getName()).append("#")
                    .append(transaction.getAmount().toString()).append("#")
                    .append(transaction.getType().toString()).append("#")
                    .append(transaction.getDate().getTime()).append("#")
                    .append(transaction.getID()).append(",");
        }

        return serialized.toString();
    }

    private List<Transaction> deserializeTransactions(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        List<Transaction> transactions = new LinkedList<>();

        String[] split = serialized.split(",");

        for (String str : split) {
            String[] strSplit = str.split("#");

            OfflinePlayer actor = Bukkit.getOfflinePlayer(strSplit[0]);
            OfflinePlayer target = Bukkit.getOfflinePlayer(strSplit[1]);
            BigInteger amount = new BigInteger(strSplit[2]);
            TransactionType type = TransactionType.valueOf(strSplit[3]);
            Date date = new Date(Long.parseLong(strSplit[4]));
            Integer id = Integer.parseInt(strSplit[5]);

            transactions.add(new Transaction(actor, target, amount, type, date, id));
        }

        return transactions;
    }
}