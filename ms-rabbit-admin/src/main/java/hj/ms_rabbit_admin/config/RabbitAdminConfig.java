package hj.ms_rabbit_admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitAdminProperties.class)
public class RabbitAdminConfig {
}
