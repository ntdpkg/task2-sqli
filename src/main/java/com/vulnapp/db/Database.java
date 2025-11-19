package com.vulnapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    // Connection String cho MSSQL
    private static final String URL = "jdbc:sqlserver://mssql:1433;databaseName=master;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "StrongPassw0rd!123";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // --- TẠO BẢNG USERS (T-SQL) ---
    private static void initUsersTable(Statement stmt) throws Exception {
        // Kiểm tra bảng tồn tại trong MSSQL
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

    // --- TẠO BẢNG TASKS (T-SQL) ---
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

    // --- PROCEDURE BỊ LỖI (T-SQL) ---
    private static void initVulnerableProcedure(Statement stmt) throws Exception {
        // Xóa procedure nếu đã tồn tại để tạo lại
        stmt.execute("IF OBJECT_ID('dbo.usp_GetTaskReport', 'P') IS NOT NULL DROP PROCEDURE dbo.usp_GetTaskReport");

        String createProc = """
            CREATE PROCEDURE dbo.usp_GetTaskReport
                @p_user_id INT, 
                @p_keyword NVARCHAR(100), 
                @p_sort_col NVARCHAR(50),
                @p_sort_dir NVARCHAR(50) -- Tham số bị lỗi
            AS
            BEGIN
                DECLARE @sql NVARCHAR(MAX);
                
                -- 1. Base Query
                SET @sql = 'SELECT id, title, description FROM tasks WHERE user_id = ' + CAST(@p_user_id AS NVARCHAR(20));
                
                -- 2. Dynamic Filtering
                IF @p_keyword IS NOT NULL AND LEN(@p_keyword) > 0
                BEGIN
                    -- Escape dấu nháy đơn để tránh lỗi cú pháp cơ bản, nhưng logic vẫn dùng dynamic SQL
                    DECLARE @safe_keyword NVARCHAR(100) = REPLACE(@p_keyword, '''', '''''');
                    SET @sql = @sql + ' AND (title LIKE ''%' + @safe_keyword + '%'' OR description LIKE ''%' + @safe_keyword + '%'')';
                END
                
                -- 3. Validate Column (Whitelist)
                DECLARE @v_sort_col NVARCHAR(50) = 'title';
                IF @p_sort_col = 'description' SET @v_sort_col = 'description';

                -- 4. Vulnerability: Nối chuỗi trực tiếp @p_sort_dir vào câu lệnh
                -- Lập trình viên tin tưởng người dùng chỉ gửi ASC/DESC
                SET @sql = @sql + ' ORDER BY ' + @v_sort_col + ' ' + @p_sort_dir;

                -- Debug (nếu cần)
                -- PRINT @sql; 

                -- Thực thi
                EXEC(@sql);
            END
        """;
        stmt.execute(createProc);
    }

    public static void init() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // Load driver MSSQL
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            initUsersTable(stmt);
            initTasksTable(stmt);
            initVulnerableProcedure(stmt);
        }
    }
}