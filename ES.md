## 1. 连接ES
#### 原生 RestHighLevelClient
1. 导入maven
```java
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.example</groupId>
        <artifactId>ESDemo</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>Code01_LinkES</artifactId>
    <packaging>jar</packaging>

    <name>Code01_LinkES</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <elasticsearch.version>9.1.3</elasticsearch.version> <!-- 与你的 ES 服务器版本一致 -->
        <jackson.version>2.15.2</jackson.version>
    </properties>


    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
            <version>9.1.3</version>
        </dependency>

        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>9.1.3</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.0</version>
        </dependency>
    </dependencies>
</project>

```
2. 进行连接
```java
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
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
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
        ElasticsearchClient client = new ElasticsearchClient(transport);

        // 测试：查询集群版本
        try {
            String version = client.info().version().number();
            System.out.println("连接成功！ES 版本: " + version);
        } catch (Exception e) {
            System.err.println("连接失败，请检查密码或 ES 是否启动：" + e.getMessage());
        }
    }
}

```
#### SpringBoot进行连接

## 2. 索引操作
#### mapping和创建索引
```java
 /**
 * 创建Mapping
 * @throws NoSuchAlgorithmException
 * @throws KeyStoreException
 * @throws KeyManagementException
 * @throws IOException
 */
public void createMapping()throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
    ElasticsearchClient elasticsearchClient = Link.getElasticsearchClient();
    TypeMapping typeMapping =  TypeMapping.of(m -> m
            .properties("name", Property.of(p -> p.text(t -> t)))
            .properties("price", Property.of(p -> p.double_(d -> d)))
    );

    CreateIndexRequest createIndexRequest  = CreateIndexRequest.of(c -> c
            .index("products")
            .mappings(typeMapping)
    );

    elasticsearchClient.indices().create(createIndexRequest);
    elasticsearchClient.close();
}
```

#### 插入数据
```java
 public static void insertProduct(ElasticsearchClient elasticsearchClient) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
    Product product = new Product("iPhone 15", 7999.0);
    IndexRequest<Product> indexRequest = IndexRequest.of(i->i
            .index("products")
            .document(product));
    IndexResponse response = elasticsearchClient.index(indexRequest);
    System.out.println("文档已插入，ID: " + response.id() + ", 版本: " + response.version());
    elasticsearchClient.close();
}
```

#### 查看数据
```java
 public static void search(ElasticsearchClient elasticsearchClient) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
    SearchRequest searchRequest = SearchRequest.of(i->i
            .index("products")
            .query(Query.of(q -> q.matchAll(m -> m)))
    );
    SearchResponse<Product> search = elasticsearchClient.search(searchRequest,Product.class);
    System.out.println(search.hits());
    elasticsearchClient.close();
}
```

#### 索引删除
```java

```
