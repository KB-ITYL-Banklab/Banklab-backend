package com.banklab.config;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j2
class RootConfigTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Datasource 연결이 된다.")
    public void dataSource() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Datasource 준비 완료");
            log.info(connection);
        }
    }

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test public void testSqlSessionFactory() {
        try {
            SqlSession session = sqlSessionFactory.openSession();
            Connection con = session.getConnection();
            log.info(session);
            log.info(con);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}