package by.abram.astore;

import by.abram.astore.config.DataSourceConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataSourceConfigTest {

    @Test
    void dataSource_ShouldUseRenderDatabaseUrl() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://astore:pa%2Bss@db.internal:6543/astore?sslmode=require");

        try (HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(environment)) {
            assertEquals("jdbc:postgresql://db.internal:6543/astore?sslmode=require", dataSource.getJdbcUrl());
            assertEquals("astore", dataSource.getUsername());
            assertEquals("pa+ss", dataSource.getPassword());
            assertEquals("org.postgresql.Driver", dataSource.getDriverClassName());
        }
    }

    @Test
    void dataSource_ShouldUseSpringProperties_WhenDatabaseUrlIsMissing() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:h2:mem:astore")
                .withProperty("spring.datasource.username", "sa")
                .withProperty("spring.datasource.password", "")
                .withProperty("spring.datasource.driver-class-name", "org.h2.Driver");

        try (HikariDataSource dataSource = (HikariDataSource) new DataSourceConfig().dataSource(environment)) {
            assertEquals("jdbc:h2:mem:astore", dataSource.getJdbcUrl());
            assertEquals("sa", dataSource.getUsername());
            assertEquals("", dataSource.getPassword());
            assertEquals("org.h2.Driver", dataSource.getDriverClassName());
        }
    }

    @Test
    void dataSource_ShouldRejectDatabaseUrlWithoutCredentials() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://db.internal:5432/astore");
        DataSourceConfig config = new DataSourceConfig();

        assertThrows(IllegalArgumentException.class, () -> config.dataSource(environment));
    }
}
