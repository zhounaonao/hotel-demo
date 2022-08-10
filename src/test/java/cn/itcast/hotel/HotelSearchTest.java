package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author 周闹闹
 * @version 1.0
 */

public class HotelSearchTest {
    private RestHighLevelClient client;

    @Test
    void testMatchAll() throws IOException {
        // 1. 准备request
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL，source
        request.source().query(QueryBuilders.matchAllQuery());

        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        System.out.println(response);

        handleResponse(response);
    }

    @Test
    void testMatch() throws IOException {
        // 1. 准备request
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL，source
        request.source().query(QueryBuilders.matchQuery("all", "如家"));

        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    @Test
    void testBool() throws IOException {
        // 1. 准备request
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL，source
        // 2.1 准备BooleanQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 2.2条件
        boolQuery.must(QueryBuilders.matchQuery("city", "上海"));
        boolQuery.filter(QueryBuilders.rangeQuery("price").gte(400).lte(700));
        request.source().query(boolQuery);

        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    @Test
    void testPageAndSort() throws IOException {
        // 页码
        int page = 2, size = 5;

        // 1. 准备request
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL，source
        // 2.2条件
        request.source()
                .sort("price", SortOrder.ASC)
                .from((page - 1) * size).size(size);

        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    @Test
    void testHighLighter() throws IOException {
        // 页码
        int page = 2, size = 5;

        // 1. 准备request
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL，source
        // 2.2条件
        request.source()
                .query(QueryBuilders.matchQuery("all", "如家"))
                .highlighter(
                        new HighlightBuilder().field("name").requireFieldMatch(false)
                );

        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    // ctrl alt m 抽取代码为函数
    private void handleResponse(SearchResponse response) {
        // 4. 解析响应(根据返回的json)
        SearchHits responseHits = response.getHits();
        long value = responseHits.getTotalHits().value;
        SearchHit[] hits = responseHits.getHits();
        System.out.println("共搜到条" + value + "数据");
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            // 获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            String highLighterName = "";
            if (!CollectionUtils.isEmpty(highlightFields)){
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null) {
                    highLighterName = highlightField.getFragments()[0].toString();
                }
            }

            // 反序列化
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            hotelDoc.setName(highLighterName);
            System.out.println(hotelDoc);
        }
    }

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.32.131:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }
}
