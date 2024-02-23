package com.nodomain.autoconfigure.distributelock;

import com.nodomain.autoconfigure.distributelock.config.DistributeLockConfig;
import com.nodomain.autoconfigure.distributelock.core.BusinessKeyProvider;
import com.nodomain.autoconfigure.distributelock.core.DistributeAspectHandler;
import com.nodomain.autoconfigure.distributelock.core.LockInfoProvider;
import com.nodomain.autoconfigure.distributelock.lock.LockFactory;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author: zph
 * @date: 2024/02/22
 * @description: 自动配置类
 */
@Configuration
@ConditionalOnProperty(prefix = DistributeLockConfig.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)//当配置文件拥有相应的前缀 生效
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(DistributeLockConfig.class)//哪种配置生效
@Import({DistributeAspectHandler.class})//导入的组件
public class DistributeLockAutoConfiguration {

    @Autowired
    private DistributeLockConfig distributeLockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        if(distributeLockConfig.getClusterServer()!=null){
            config.useClusterServers().setPassword(distributeLockConfig.getPassword())
                    .addNodeAddress(distributeLockConfig.getClusterServer().getNodeAddresses());
        }else {
            config.useSingleServer().setAddress(distributeLockConfig.getAddress())
                    .setDatabase(distributeLockConfig.getDatabase())
                    .setPassword(distributeLockConfig.getPassword());
        }
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockInfoProvider lockInfoProvider(){
        return new LockInfoProvider();
    }

    @Bean
    public BusinessKeyProvider businessKeyProvider(){
        return new BusinessKeyProvider();
    }

    @Bean
    public LockFactory lockFactory(){
        return new LockFactory();
    }


}
