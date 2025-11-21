package com.vulnapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "StrongPassw0rd!123";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void initUsersTable(Statement stmt) throws Exception {
        String sql = """
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
            BEGIN
                CREATE TABLE users (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    username NVARCHAR(50) UNIQUE NOT NULL,
                    email NVARCHAR(100) UNIQUE NOT NULL,
                    role NVARCHAR(10) DEFAULT 'user' NOT NULL,
                    password NVARCHAR(50) NOT NULL
                );
                INSERT INTO users (username, email, password, role) VALUES ('admin', 'admin@admin.com', 'admin123', 'admin');
            END
        """;
        stmt.execute(sql);
    }

    private static void initTasksTable(Statement stmt) throws Exception {
        String sql = """
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tasks' AND xtype='U')
            BEGIN
                CREATE TABLE tasks (
                    id INT IDENTITY(1,1),
                    user_id INT FOREIGN KEY REFERENCES users(id),
                    title NVARCHAR(100) NOT NULL,
                    description NVARCHAR(MAX) NOT NULL,
                    PRIMARY KEY (id, user_id)
                );
            END
        """;
        stmt.execute(sql);
    }

    private static void initVulnerableProcedure(Statement stmt) throws Exception {
        stmt.execute("IF OBJECT_ID('dbo.searchTasks', 'P') IS NOT NULL DROP PROCEDURE dbo.searchTasks;");

        String createProc = """
            CREATE PROCEDURE dbo.searchTasks
                @user_id INT, 
                @keyword NVARCHAR(100), 
                @sort_dir NVARCHAR(50)
            AS
            BEGIN
                DECLARE @sql NVARCHAR(MAX);                
                SET @sql = 'SELECT id, title, description FROM tasks WHERE user_id = ' + CAST(@user_id AS NVARCHAR(20));
                
                IF @keyword IS NOT NULL AND LEN(@keyword) > 0
                BEGIN
                    DECLARE @safe_keyword NVARCHAR(100) = REPLACE(@keyword, '''', '');
                    SET @safe_keyword = REPLACE(@keyword, ';', '');
                    SET @sql = @sql + ' AND (title LIKE ''%' + @safe_keyword + '%'' OR description LIKE ''%' + @safe_keyword + '%'')';
                END

                SET @sql = @sql + ' ORDER BY title ' + @sort_dir;
                PRINT @sql; 

                EXEC(@sql);
            END
        """;
        stmt.execute(createProc);
    }

    public static void init() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            initUsersTable(stmt);
            initTasksTable(stmt);
            initVulnerableProcedure(stmt);
        }
    }
}