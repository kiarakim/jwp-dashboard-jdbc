package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private static final int QUERY_SINGLE_SIZE = 1;

    public AppUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        User user = findById(id);
        user.changePassword(newPassword);
        updateUser(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }

    private void updateUser(User user) {
        int updateSize = userDao.update(user);
        if (updateSize != QUERY_SINGLE_SIZE) {
            throw new DataAccessException("갱신된 데이터의 개수가 올바르지 않습니다.");
        }
    }
}
