package org.cedj.geekseek.domain.attachment.infinispan;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

public class CacheProducer {

    @Produces @ApplicationScoped
    public EmbeddedCacheManager create() {
        GlobalConfiguration global = new GlobalConfigurationBuilder()
            .globalJmxStatistics().cacheManagerName("geekseek")
            .build();

        Configuration local = new ConfigurationBuilder()
            .clustering()
                .cacheMode(CacheMode.LOCAL)
            .transaction()
                .transactionMode(TransactionMode.TRANSACTIONAL)
                .transactionManagerLookup(new GenericTransactionManagerLookup())
             .autoCommit(false)
            .build();
        return new DefaultCacheManager(global, local);
    }

    @Produces @ApplicationScoped
    public Cache<Object, Map<String, String>> create(EmbeddedCacheManager manager) {
        return manager.getCache();
    }

    public void destory(@Disposes EmbeddedCacheManager manager) {
        manager.stop();
    }

    public void destory(@Disposes Cache<?, ?> cache) {
        cache.stop();
    }
}
