package org.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author caiyuping
 * @date 2026/4/6 22:33
 * @description: 业务
 */
public class Link {
    public static ElasticsearchClient getElasticsearchClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        // 1. 配置身份认证 (ES 9.x 默认账号是 elastic)
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("admin", "123456"));

        // 2. 配置 SSL (开发环境跳过证书检查，生产环境建议加载 http_ca.crt)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chained, authType) -> true)
                .build();

        // 3. 构建底层 RestClient
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http") // 注意是 https
        ).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                .setDefaultCredentialsProvider(credentialsProvider)
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier((hostname, session) -> true) // 忽略域名校验
        ).build();

        // 4. 创建传输层并指定序列化工具
        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // 5. 得到最终的 API 客户端
        return  new ElasticsearchClient(transport);
    }
}
