package partition.poc.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import partition.poc.configuration.properties.ApplicationProperties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CuratorConfiguration {
	private final ApplicationProperties applicationProperties;

	@Bean(destroyMethod = "close")
	public CuratorFramework curatorFramework() {
		ApplicationProperties.CuratorProperties properties = applicationProperties.getCurator();

		CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
				.connectionTimeoutMs(properties.getConnectionTimeoutMs())
				.sessionTimeoutMs(properties.getSessionTimeoutMs())
				.canBeReadOnly(false)
				.connectString(properties.getConnectionString())
				.retryPolicy(new RetryForever(properties.getRetryTimeoutMs()))
				.build();

		log.info("Started curator framework with connection timeout of {} ms, session timeout of {} ms, " +
						"retry timeout of {} and connect string of '{}'.",
				properties.getConnectionTimeoutMs(), properties.getSessionTimeoutMs(), properties.getRetryTimeoutMs(), properties.getConnectionString());

		if (properties.isAutoStartEnabled()) {
			curatorFramework.start();
		}

		return curatorFramework;
	}
}