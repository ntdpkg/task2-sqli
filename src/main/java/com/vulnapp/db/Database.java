package com.vulnapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {
    // KẾT NỐI LOCALHOST VÌ CÙNG SERVER
    private static final String URL = "jdbc:mysql://localhost:3306/todotask_db?allowPublicKeyRetrieval=true&useSSL=false&allowMultiQueries=true";
    private static final String USER = "todotask_user";
    private static final String PASS = "todotask_passwd";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void initUsersTable(Statement stmt) throws Exception {
        ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'users'");
        if (!rs.next()) {
            stmt.execute("""                    
                CREATE TABLE users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    role VARCHAR(10) DEFAULT 'user' NOT NULL,
                    password VARCHAR(50) NOT NULL
                );
            """);
            stmt.execute("INSERT IGNORE INTO users (username, email, password, role) VALUES ('admin', 'admin@admin.com', 'admin123', 'admin')");
        }
    }

    private static void initTasksTable(Statement stmt) throws Exception {
        ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'tasks'");
        if (!rs.next()) {
            stmt.execute("""
                CREATE TABLE tasks (
                    id INT AUTO_INCREMENT,
                    user_id INT NOT NULL, 
                    title VARCHAR(100) NOT NULL,
                    description TEXT NOT NULL,
                    PRIMARY KEY (id, user_id), 
                    CONSTRAINT fk_user_task FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);
        }
    }

    private static void initVulnerableProcedure(Statement stmt) throws Exception {
        stmt.execute("DROP PROCEDURE IF EXISTS searchTasks");

        // VULNERABLE PROCEDURE (ORDER BY INJECTION)
        String createProc = """
            CREATE PROCEDURE searchTasks(
                IN user_id INT, 
                IN keyword VARCHAR(255), 
                IN sort_dir VARCHAR(500) 
            )
            BEGIN
                SET @query = CONCAT('SELECT id, title, description FROM tasks WHERE user_id = ', user_id);
                
                IF keyword IS NOT NULL AND keyword != '' THEN
                    SET @query = CONCAT(@query, ' AND (title LIKE ''%', keyword, '%'' OR description LIKE ''%', keyword, '%'')');
                END IF;

                -- Injection Point
                SET @query = CONCAT(@query, ' ORDER BY title ', sort_dir);
                
                PREPARE stmt FROM @query;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;
            END
        """;
        stmt.execute(createProc);
    }

    public static void init() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Localhost nên kết nối rất nhanh, retry ít thôi
        int retries = 5;
        while (retries > 0) {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                System.out.println("Connected to Local MySQL.");
                initUsersTable(stmt);
                initTasksTable(stmt);
                initVulnerableProcedure(stmt);
                System.out.println("DB Initialized.");
                return;
            } catch (Exception e) {
                System.out.println("Waiting for MySQL... " + e.getMessage());
                retries--;
                Thread.sleep(2000);
            }
        }
    }
}