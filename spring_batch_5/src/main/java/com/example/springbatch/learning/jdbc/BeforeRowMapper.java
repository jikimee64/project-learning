package com.example.springbatch.learning.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class BeforeRowMapper implements RowMapper<Before> {

    public Before mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Before(
            rs.getString("documentId"),
            rs.getString("username")
        );
    }
}
