package ru.red.authenticationservice.config;

import lombok.extern.log4j.Log4j2;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderKeywordCase;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Log4j2
@Configuration
public class PersistenceConfig {

    private final DataSource dataSource;

    @Autowired
    public PersistenceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() {
        return new DataSourceConnectionProvider
                (new TransactionAwareDataSourceProxy(dataSource));
    }

    @Bean
    public DefaultDSLContext defaultDSLContext() {
        return new DefaultDSLContext(jooqConfig());
    }

    @Bean
    public DefaultConfiguration jooqConfig() {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider());
        jooqConfiguration.set(jooqRenderSettings());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
        jooqConfiguration.set(SQLDialect.POSTGRES);

        return jooqConfiguration;
    }

    @Bean
    public Settings jooqRenderSettings() {
        return new Settings()
                .withInterpreterDialect(SQLDialect.POSTGRES)
                .withParseDialect(SQLDialect.POSTGRES)
                .withRenderNameCase(RenderNameCase.LOWER)
                .withRenderKeywordCase(RenderKeywordCase.LOWER);
    }

    private ExecuteListener exceptionTransformer() {
        return new DefaultExecuteListener() {
            @Override
            public void exception(ExecuteContext ctx) {
                if (ctx.exception() != null) {
                    log.info(ctx.exception());
                }
            }
        };
    }
}
