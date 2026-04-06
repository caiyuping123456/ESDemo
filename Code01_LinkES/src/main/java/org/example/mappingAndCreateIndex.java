package org.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.IndexField;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author caiyuping
 * @date 2026/4/6 22:56
 * @description: 业务
 */
public class mappingAndCreateIndex {

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ElasticsearchClient elasticsearchClient = Link.getElasticsearchClient();
        SearchRequest searchRequest = SearchRequest.of(i->i
                .index("products")
                .query(Query.of(q -> q.matchAll(m -> m)))
        );
        SearchResponse<Product> search = elasticsearchClient.search(searchRequest,Product.class);
        System.out.println(search.hits());
        elasticsearchClient.close();
    }

    public static void search(ElasticsearchClient elasticsearchClient) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        SearchRequest searchRequest = SearchRequest.of(i->i
                .index("products")
                .query(Query.of(q -> q.matchAll(m -> m)))
        );
        SearchResponse<Product> search = elasticsearchClient.search(searchRequest,Product.class);
        System.out.println(search.hits());
        elasticsearchClient.close();
    }

    public static void insertProduct(ElasticsearchClient elasticsearchClient) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Product product = new Product("iPhone 15", 7999.0);
        IndexRequest<Product> indexRequest = IndexRequest.of(i->i
                .index("products")
                .document(product));
        IndexResponse response = elasticsearchClient.index(indexRequest);
        System.out.println("文档已插入，ID: " + response.id() + ", 版本: " + response.version());
        elasticsearchClient.close();
    }

    /**
     * 创建Mapping
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws IOException
     */
    public void createMapping(ElasticsearchClient elasticsearchClient)throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
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
}
record Product(String name,Double price){}
