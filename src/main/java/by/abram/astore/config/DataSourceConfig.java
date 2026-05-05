package by.abram.astore.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(Environment environment) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        HikariDataSource dataSource = new HikariDataSource();

        if (StringUtils.hasText(databaseUrl)) {
            configureFromDatabaseUrl(dataSource, databaseUrl.trim());
            return dataSource;
        }

        dataSource.setJdbcUrl(environment.getRequiredProperty("spring.datasource.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username", ""));
        dataSource.setPassword(environment.getProperty("spring.datasource.password", ""));

        String driverClassName = environment.getProperty("spring.datasource.driver-class-name");
        if (StringUtils.hasText(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }

        return dataSource;
    }

    private void configureFromDatabaseUrl(HikariDataSource dataSource, String databaseUrl) {
        String normalizedUrl = databaseUrl.startsWith("postgres://")
                ? "postgresql://" + databaseUrl.substring("postgres://".length())
                : databaseUrl;

        URI uri = URI.create(normalizedUrl);
        String userInfo = uri.getRawUserInfo();
        if (!StringUtils.hasText(userInfo)) {
            throw new IllegalArgumentException("DATABASE_URL must include username and password");
        }

        String[] credentials = userInfo.split(":", 2);
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost())
                .append(":")
                .append(port)
                .append(uri.getPath());

        if (StringUtils.hasText(uri.getRawQuery())) {
            jdbcUrl.append("?").append(uri.getRawQuery());
        }

        dataSource.setJdbcUrl(jdbcUrl.toString());
        dataSource.setUsername(decode(credentials[0]));
        dataSource.setPassword(credentials.length > 1 ? decode(credentials[1]) : "");
        dataSource.setDriverClassName("org.postgresql.Driver");
    }

    private String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
