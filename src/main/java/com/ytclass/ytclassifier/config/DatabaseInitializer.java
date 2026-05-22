package com.ytclass.ytclassifier.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if yid column exists in users table
            jdbcTemplate.execute("SELECT yid FROM users LIMIT 1");
        } catch (Exception e) {
            System.out.println("[DatabaseInitializer] yid column not found in users table. Altering table...");
            try {
                // Alter table to add yid column
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN yid TEXT");
                // Create unique index
                jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_yid ON users(yid)");
                System.out.println("[DatabaseInitializer] Successfully added yid column to users table.");
            } catch (Exception ex) {
                System.err.println("[DatabaseInitializer] Failed to alter users table: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
