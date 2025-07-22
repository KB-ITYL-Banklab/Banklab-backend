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
@PropertySources({
        @PropertySource("classpath:/application.properties"),
        @PropertySource("classpath:application-secret.properties")})
@MapperScan(basePackages = {
        "com.banklab.member.mapper",
        "com.banklab.account.mapper",
        "com.banklab.transaction.mapper",
        "com.banklab.category.mapper"})
@ComponentScan(basePackages = {
        "com.banklab.member.service",
        "com.banklab.account.service",
        "com.banklab.codef.service",
        "com.banklab.transaction.service",
        "com.banklab.category.service",
        "com.banklab.perplexity.service",
})
@EnableTransactionManagement
public class RootConfig {
    @Value("${jdbc.driver}")
    String driver;
    @Value("${jdbc.url}")
    String url;
    @Value("${jdbc.username}")
    String username;
    @Value("${jdbc.password}")
    String password;
    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return new HikariDataSource(config);
    }

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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
