package net.ketone.accrptgen.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.connection.SslSettings;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.List;
import java.util.Properties;

@Configuration
@Profile("!test")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Autowired
    private CredentialsService credentialsService;


    @Value("${mongo.database.name}")
    private String mongoDbName;

    @Override
    public String getDatabaseName() {
        return mongoDbName;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        Properties p = credentialsService.getCredentials();
        builder
                .credential(MongoCredential.createCredential("root", "admin",
                        p.get(CredentialsService.MONGODB_PASS).toString().toCharArray()))
                .applyToClusterSettings(settings  -> {
                    settings.hosts(List.of(new ServerAddress("cluster0-shard-00-00.yztpq.mongodb.net", 27017),
                            new ServerAddress("cluster0-shard-00-01.yztpq.mongodb.net", 27017),
                            new ServerAddress("cluster0-shard-00-02.yztpq.mongodb.net", 27017)
                    ))
                    ;
                })
                .applyToSslSettings((block) -> block.applySettings(SslSettings.builder()
                        .enabled(true)
                        .invalidHostNameAllowed(true)
                        .build()))
                ;
    }
}
