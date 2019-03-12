package dev.koiki.chord.communitysearch.config

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestHighLevelClientConfig {
    @Bean
    fun restHighLevelClient() = RestHighLevelClient(
            RestClient.builder(HttpHost("localhost", 9200))
                    .setRequestConfigCallback { config ->
                        config.setConnectTimeout(5_000)
                                .setConnectionRequestTimeout(5_000)
                                .setSocketTimeout(5_000)
                    }
                    .setMaxRetryTimeoutMillis(5_000)
    )
}
