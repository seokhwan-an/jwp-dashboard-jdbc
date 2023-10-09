package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.DataAccessException;
import org.springframework.jdbc.exception.RollbackFailException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException("데이터에 접근할 수 없습니다.");
        } catch (SQLException exception) {
            throw new RollbackFailException("롤백을 실패했습니다.");
        }
    }
}
