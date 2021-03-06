package net.azisaba.main.budgets.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SQLの基本操作を行うクラス
 *
 * @author siloneco
 *
 */
@RequiredArgsConstructor
public class SQLHandler {

    private final File file;

    private Connection connection;
    @Getter(value = AccessLevel.PROTECTED)
    private boolean initialized = false;

    public void init() {
        // ドライバの登録
        registerDriver();
        // ファイルがなかった場合にファイルを作成
        createFileIfNotExists(file);

        // 接続
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        initialized = true;
    }

    /**
     * 指定したコマンドを実行します。このコマンドは {@link ResultSet} を返さない必要があります
     *
     * @see #executeQuery(String)
     *
     * @param cmd 実行したいコマンド
     * @return コマンドの実行に成功したかどうか
     */
    synchronized public int executeCommand(String cmd) {
        try {
            return connection.prepareStatement(cmd).executeUpdate();
        } catch ( Exception e ) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 指定したコマンドを実行します。このコマンドは {@link ResultSet} を返す必要があります
     *
     * @see #executeCommand(String)
     *
     * @param cmd 実行したいコマンド
     * @return 取得されたResultSet, 実行に失敗した場合はnull
     */
    synchronized public ResultSet executeQuery(String cmd) {
        try {
            return connection.prepareStatement(cmd).executeQuery();
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 接続するためにドライバを登録する
     */
    private void registerDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }
    }

    /**
     * ファイルが存在しなかった場合にファイルを作成する
     *
     * @param file 対象のファイル
     */
    private void createFileIfNotExists(File file) {
        if ( !file.exists() ) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public boolean closeConnection() {
        try {
            if ( connection != null && !connection.isClosed() ) {
                connection.close();
            }
            return true;
        } catch ( SQLException e ) {
            e.printStackTrace();
            return false;
        }
    }
}
