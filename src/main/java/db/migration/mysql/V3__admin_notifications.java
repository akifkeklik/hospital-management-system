package db.migration.mysql;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

/**
 * Adds the administrator notification target.
 *
 * Older deployments may already have this column/index because Hibernate's
 * former ddl-auto=update setting could have created them before Flyway owned
 * the schema. Check metadata first so the migration is safe for both cases.
 */
public class V3__admin_notifications extends BaseJavaMigration {

    private static final String TABLE = "notifications";
    private static final String COLUMN = "admin_id";
    private static final String INDEX = "IDX_NOTIFICATION_ADMIN_DATE";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!columnExists(connection, TABLE, COLUMN)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE notifications ADD COLUMN admin_id BIGINT NULL");
            }
        }

        if (!indexExists(connection, TABLE, INDEX)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE INDEX IDX_NOTIFICATION_ADMIN_DATE "
                        + "ON notifications (admin_id, created_at DESC)");
            }
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        for (String tableName : List.of(table, table.toUpperCase(Locale.ROOT))) {
            for (String columnName : List.of(column, column.toUpperCase(Locale.ROOT))) {
                try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, tableName, columnName)) {
                    if (result.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean indexExists(Connection connection, String table, String index) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        for (String tableName : List.of(table, table.toUpperCase(Locale.ROOT))) {
            try (ResultSet result = metadata.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
                while (result.next()) {
                    String indexName = result.getString("INDEX_NAME");
                    if (indexName != null && index.equalsIgnoreCase(indexName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
