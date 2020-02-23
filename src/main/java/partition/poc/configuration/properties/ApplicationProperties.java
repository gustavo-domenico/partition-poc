package partition.poc.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "application")
@Configuration
@Data
public class ApplicationProperties {
	private CuratorProperties curator = new CuratorProperties();

	@Data
	public static class CuratorProperties {
		private boolean autoStartEnabled = true;
		private String connectionString = "";
		private String groupName = "brazilian_transaction_reporting-curator";
		private int sessionTimeoutMs = 60000;
		private int connectionTimeoutMs = 15000;
		private int retryTimeoutMs = 5000;
	}
}
