package com.banklab.config;

import com.banklab.category.dto.CategoryDTO;
import com.banklab.category.service.CategoryService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@PropertySource("classpath:/application.properties")
@MapperScan(basePackages = {
        "com.banklab.account.mapper",
        "com.banklab.member.mapper",
        "com.banklab.financeContents.mapper",
        "com.banklab.typetest.mapper",
        "com.banklab.product.mapper",
        "com.banklab.risk.mapper",
        "com.banklab.transaction.mapper",
        "com.banklab.category.mapper",
        "com.banklab.transaction.summary.mapper"
})
@ComponentScan(basePackages = {
        "com.banklab.member.service",
        "com.banklab.oauth.service",
        "com.banklab.oauth.client",
        "com.banklab.account.service",
        "com.banklab.financeContents.service",
        "com.banklab.typetest",
        "com.banklab.product",
        "com.banklab.risk",
        "com.banklab.codef",
        "com.banklab.transaction.service",
        "com.banklab.transaction",
        "com.banklab.category",
        "com.banklab.category.perplexity",
        "com.banklab.category.kakaomap",
        "com.banklab.verification.sender",
        "com.banklab.verification.service",
        "com.banklab.common.redis"
})
@EnableTransactionManagement
public class RootConfig {
    @Value("${jdbc.driver}")
    String driver;
    @Value("${jdbc.url}")
    String url;
    @Value("${jdbc.username}")
    String username;
    @Value(("${jdbc.password}"))
    String password;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());
        return sqlSessionFactory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());

        return manager;
    }

    @Bean(name = "categoryMap")
    public Map<String, Long> categoryMap(CategoryService categoryService) {
        return categoryService.findAll().stream()
                .collect(Collectors.toMap(CategoryDTO::getName, CategoryDTO::getId));
    }



}