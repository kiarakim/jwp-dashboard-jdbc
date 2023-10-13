package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, rowMapper, createPreparedStatementSetter(params));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, rowMapper, createPreparedStatementSetter(params));
    }

    public int update(String sql, Object... params) {
        return update(sql, createPreparedStatementSetter(params));
    }

    private <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            return mapResultSetToObject(rowMapper, preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        List<T> result = query(sql, rowMapper, pss);
        validateResultSize(result);
        return result.get(0);
    }

    private int update(String sql, PreparedStatementSetter pss) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... params) {
        return psmt -> {
            for (int i = 0; i < params.length; i++) {
                psmt.setObject(i + 1, params[i]);
            }
        };
    }

    private <T> List<T> mapResultSetToObject(RowMapper<T> rowMapper, PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.run(resultSet));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static <T> void validateResultSize(List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("해당하는 유저가 없습니다.");
        }
    }
}
