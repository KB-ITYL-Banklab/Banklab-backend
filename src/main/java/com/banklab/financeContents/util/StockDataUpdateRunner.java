package com.banklab.financeContents.util;

import com.banklab.financeContents.service.FinanceStockService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

/**
 * 주식 데이터 업데이트 러너
 * POST /api/stocks/save/recent 와 동일한 기능을 수행
 */
@Slf4j
public class StockDataUpdateRunner {

    public static void main(String[] args) {
        // log.info("🚀 주식 데이터 업데이트 시작");
        
        try {
            // 최소한의 Spring 컨텍스트 로드 (Security 제외)
            ApplicationContext context = new AnnotationConfigApplicationContext(MinimalConfig.class);
            FinanceStockService financeStockService = context.getBean(FinanceStockService.class);
            
            // 1. 오래된 데이터 삭제
            // log.info("🗑️ 30일 이전 데이터 삭제 중...");
            int deletedCount = financeStockService.deleteOldData();
            // log.info("✅ 삭제 완료: {}건", deletedCount);
            
            // 2. 최근 30일간 상위 200개 종목 데이터 저장
            // log.info("📊 최근 30일간 데이터 저장 중...");
            int savedCount = financeStockService.saveRecentStockData(30, 1000);
            // log.info("✅ 저장 완료: {}건", savedCount);
            
            // log.info("🎉 업데이트 완료! 삭제: {}건, 저장: {}건", deletedCount, savedCount);
            
        } catch (Exception e) {
            // log.error("❌ 업데이트 실패: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * 러너 전용 최소 설정 클래스 (Security 제외)
     */
    @Configuration
    @MapperScan(basePackages = "com.banklab.financeContents.mapper")
    @ComponentScan(basePackages = "com.banklab.financeContents.service")
    @EnableTransactionManagement
    static class MinimalConfig {

        @Bean
        public DataSource dataSource() {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
            config.setJdbcUrl("jdbc:log4jdbc:mysql://localhost:3306/banklab");
            config.setUsername("root");
            config.setPassword("1234");
            return new HikariDataSource(config);
        }

        @Bean
        public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
            factory.setDataSource(dataSource);
            factory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
            return factory.getObject();
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}