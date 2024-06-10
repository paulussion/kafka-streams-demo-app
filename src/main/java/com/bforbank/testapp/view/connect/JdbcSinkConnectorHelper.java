package com.bforbank.testapp.view.connect;

import java.util.HashMap;

public class JdbcSinkConnectorHelper {
    private JdbcSinkConnectorHelper() {}

    public static final String AUTO_CREATE = "auto.create ";
    public static final String TABLE_NAME_FORMAT = "table.name.format";
    public static final String TOPICS = "topics";
    public static final String PK_MODE = "pk.mode";
    public static final String FIELDS_WHITELIST = "fields.whitelist";
    public static final String CONNECTION_URL = "connection.url";
    public static final String CONNECTION_USER = "connection.user";
    public static final String CONNECTION_PASSWORD = "connection.password";
    public static final String DIALECT_NAME = "dialect.name";
    public static final String INSERT_MODE = "insert.mode";
    public static final String CONNECTOR_CLASS = "connector.class";
    public static final String TASKS_MAX = "tasks.max";
    public static final String PK_FIELDS = "pk.fields";
    public static final String RECORD_KEY = "record_key";
    public static final String RECORD_VALUE = "record_value";
    public static final String POSTGRE_SQL_DATABASE_DIALECT = "PostgreSqlDatabaseDialect";
    public static final String UPSERT = "upsert";
    public static final String IO_CONFLUENT_CONNECT_JDBC_JDBC_SINK_CONNECTOR = "io.confluent.connect.jdbc.JdbcSinkConnector";

    public static String getJdbcUrl(KafkaConnectProperties.DatabaseConnection db) {
        return String.format("jdbc:postgresql://%s:5432/%s", db.host(), db.name());
    }

    public static HashMap<String, String> getBaseConnectorConfig(KafkaConnectProperties.DatabaseConnection db) {
        String jdbcUrl = getJdbcUrl(db);

        HashMap<String, String> config = new HashMap<>();
        config.put(CONNECTOR_CLASS, IO_CONFLUENT_CONNECT_JDBC_JDBC_SINK_CONNECTOR);
        config.put(CONNECTION_URL, jdbcUrl);
        config.put(CONNECTION_USER, db.user());
        config.put(CONNECTION_PASSWORD, db.password());
        config.put(DIALECT_NAME, POSTGRE_SQL_DATABASE_DIALECT);
        return config;
    }
}
