package com.vulnapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
   private static final String URL = "jdbc:oracle:thin:@oracle:1521/FREEPDB1";
    // private static final String URL = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
    private static final String USER = "todotask_user";
    private static final String PASS = "todotask_passwd";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void initUsersTable(Statement stmt) throws Exception {
        String sql = """
            CREATE TABLE users (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                username VARCHAR2(50) UNIQUE NOT NULL,
                email VARCHAR2(100) UNIQUE NOT NULL,
                role VARCHAR2(10) DEFAULT 'user' NOT NULL,
                password VARCHAR2(50) NOT NULL
            )
        """;
        stmt.execute(sql);
        stmt.execute("INSERT INTO users (username, email, password, role) VALUES ('admin', 'admin@admin.com', 'admin123', 'admin');");
    }

    private static void initTasksTable(Statement stmt) throws Exception {
        String sql = """
            CREATE TABLE tasks (
                id NUMBER GENERATED ALWAYS AS IDENTITY,
                user_id NUMBER REFERENCES users(id),
                title VARCHAR2(100) NOT NULL,
                description VARCHAR2(4000) NOT NULL,
                PRIMARY KEY (id, user_id)
            );
        """;
        stmt.execute(sql);
    }

    private static void initVulnerableProcedure(Statement stmt) throws Exception {
        String createProc = """
            CREATE OR REPLACE PROCEDURE searchTasks(
                user_id IN NUMBER,
                keyword IN VARCHAR2,
                sort_dir IN VARCHAR2,
                recordset OUT SYS_REFCURSOR
            )
            AS
                v_sql VARCHAR2(4000);
                v_safe_keyword VARCHAR2(100);
            BEGIN
                v_sql := 'SELECT id, title, description FROM tasks WHERE user_id = ' || user_id;

                IF keyword IS NOT NULL AND LENGTH(keyword) > 0 THEN
                    v_safe_keyword := REPLACE(keyword, '''', '');
                    v_safe_keyword := REPLACE(v_safe_keyword, ';', '');

                    v_sql := v_sql || ' AND (title LIKE ''%' || v_safe_keyword || '%'' OR description LIKE ''%' || v_safe_keyword || '%'')';
                END IF;

                v_sql := v_sql || ' ORDER BY title ' || sort_dir;
                DBMS_OUTPUT.PUT_LINE(v_sql);

                OPEN recordset FOR v_sql;
            END;
        """;
        stmt.execute(createProc);
    }

    public static void init() throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");

        int maxRetries = 7;
        int retryCount = 0;
        int retryDelayMs = 5000;
        while (true) {
            System.out.println("Attempting to connect to the database...");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                try {
                    initUsersTable(stmt);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                try {
                    initTasksTable(stmt);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                try {
                    initVulnerableProcedure(stmt);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break; 
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    System.err.println("Exceeded maximum number of retries to connect to the database.");
                    throw e;
                }
                System.err.println("Database connection failed. Retrying in " + (retryDelayMs / 1000) + " seconds...");
                try { Thread.sleep(retryDelayMs); } catch (InterruptedException ie) {}
            }
        }
    }
}

