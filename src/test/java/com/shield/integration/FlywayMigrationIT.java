package com.shield.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.shield.integration.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class FlywayMigrationIT extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyMigrationsOnFreshDatabase() {
        Integer tenantTableExists = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'public'
                          and table_name = 'tenant'
                        """,
                Integer.class);

        assertThat(tenantTableExists).isEqualTo(1);
    }
}
