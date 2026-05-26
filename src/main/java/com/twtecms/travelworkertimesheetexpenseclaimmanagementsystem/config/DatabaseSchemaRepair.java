package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseSchemaRepair implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaRepair(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureAutoIncrement("claim", "claim_id");
        ensureAutoIncrement("claim_detail", "claim_detail_id");
        ensureAutoIncrement("claim_image", "image_id");
        ensureAutoIncrement("payment", "payment_id");
        jdbcTemplate.execute("alter table claim_detail modify description text");
    }

    private void ensureAutoIncrement(String tableName, String columnName) {
        Boolean isAutoIncrement = jdbcTemplate.queryForObject("""
                select extra like '%auto_increment%'
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, Boolean.class, tableName, columnName);

        if (Boolean.TRUE.equals(isAutoIncrement)) {
            return;
        }

        List<Map<String, Object>> foreignKeys = getReferencingForeignKeys(tableName, columnName);
        try {
            for (Map<String, Object> foreignKey : foreignKeys) {
                jdbcTemplate.execute("alter table " + foreignKey.get("table_name")
                        + " drop foreign key " + foreignKey.get("constraint_name"));
            }

            jdbcTemplate.execute("alter table " + tableName + " modify " + columnName + " bigint not null auto_increment");
        } finally {
            for (Map<String, Object> foreignKey : foreignKeys) {
                jdbcTemplate.execute("alter table " + foreignKey.get("table_name")
                        + " add constraint " + foreignKey.get("constraint_name")
                        + " foreign key (" + foreignKey.get("column_name") + ")"
                        + " references " + tableName + "(" + columnName + ")");
            }
        }
    }

    private List<Map<String, Object>> getReferencingForeignKeys(String tableName, String columnName) {
        return jdbcTemplate.queryForList("""
                select tc.constraint_name, tc.table_name, kcu.column_name
                from information_schema.table_constraints tc
                join information_schema.key_column_usage kcu
                  on tc.constraint_schema = kcu.constraint_schema
                 and tc.constraint_name = kcu.constraint_name
                 and tc.table_name = kcu.table_name
                where tc.constraint_schema = database()
                  and tc.constraint_type = 'FOREIGN KEY'
                  and kcu.referenced_table_name = ?
                  and kcu.referenced_column_name = ?
                """, tableName, columnName);
    }
}
