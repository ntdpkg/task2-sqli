package com.vulnapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:postgresql://postgres:5432/todotask_db";
    private static final String USER = "todotask_user";
    private static final String PASS = "todotask_passwd";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void initUsersTable(Statement stmt) throws Exception {
        String checkTable = "SELECT to_regclass('public.users')";
        var rs = stmt.executeQuery(checkTable);
        boolean tableExists = rs.next() && rs.getString(1) != null;
        if (!tableExists) {
            stmt.execute("""                    
                CREATE TABLE users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    role VARCHAR(10) DEFAULT 'user' NOT NULL,
                    password VARCHAR(50) NOT NULL
                )
            """);
            stmt.execute("INSERT INTO users (username, email, password, role) VALUES ('admin', 'admin@admin.com', 'admin123', 'admin') ON CONFLICT (username) DO NOTHING");
        }
    }

    private static void initTasksTable(Statement stmt) throws Exception {
        String checkTable = "SELECT to_regclass('public.tasks')";
        var rs = stmt.executeQuery(checkTable);
        boolean tableExists = rs.next() && rs.getString(1) != null;
        if (!tableExists) {
            stmt.execute("""
                CREATE TABLE tasks (
                    id SERIAL,
                    user_id INTEGER REFERENCES users(id),
                    title VARCHAR(100) NOT NULL,
                    description TEXT NOT NULL,
                    PRIMARY KEY (id, user_id)
                )
            """);
        }
    }

    public static void init() throws Exception {
        Class.forName("org.postgresql.Driver");
        while (true) {
            try (Connection conn = getConnection()) {
                initUsersTable(stmt);
                initTasksTable(stmt);
                System.out.println("Database is ready.");
                break;
            } catch (Exception e) {
                System.out.println("Database not ready, retrying in 2 seconds...");
                Thread.sleep(2000);
            }
        }

    }
}