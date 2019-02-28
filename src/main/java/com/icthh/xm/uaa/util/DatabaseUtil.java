package com.icthh.xm.uaa.util;

import lombok.experimental.UtilityClass;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.icthh.xm.uaa.config.Constants.DDL_CREATE_SCHEMA;

/**
 * Utility for database operations.
 */
@UtilityClass
@SuppressWarnings("squid:S1118") // private constructor generated by lombok
public final class DatabaseUtil {

    private static final String DEFAULT_LANG = "tenant.defaultLang";

    /**
     * Creates new database scheme.
     *
     * @param dataSource the datasource
     * @param name       schema name
     */
    public static void createSchema(DataSource dataSource, String name) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format(DDL_CREATE_SCHEMA, name));
        } catch (SQLException e) {
            throw new RuntimeException("Can not connect to database", e);
        }
    }

    /**
     * Get tenant default language.
     * @param tenant the tenant key
     * @return default language
     */
    public static Map<String, String> defaultParams(String tenant) {
        Map<String, String> params = new HashMap<>();
        params.put(DEFAULT_LANG, "en"); // TODO get from config
        return params;
    }
}
