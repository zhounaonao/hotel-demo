package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.impl.HotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.constant.HotelConstants.MAPPING_TEMPLATE;

/**
 * @author 周闹闹
 * @version 1.0
 */
@SpringBootTest
public class HotelIndexTest {

    @Autowired
    private HotelService hotelService;

    private RestHighLevelClient client;

    @Test
    void testClient() {
        System.out.println(client);
    }

    @Test
    void testCreateHotel() throws IOException {

        // 创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);

        // 发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteHotel() throws IOException {

        // 创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistsHotel() throws IOException {
        // 创建Request对象
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists?"存在":"不存在");
    }

    @Test
    void testAddDocument() throws IOException {
        Hotel hotel = hotelService.getById(61083L);
        HotelDoc hotelDoc = new HotelDoc(hotel);

        // 准备request对象
        IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
        // 准备JSON文档
        request.source(JSON.toJSONString(hotelDoc),XContentType.JSON);
        // 发起请求
        client.index(request, RequestOptions.DEFAULT);
    }

    /**
     * 查询，没有就null
     * @throws IOException
     */
    @Test
    void testGetDocument() throws IOException {
        // 准备request对象
        GetRequest request = new GetRequest("hotel", "61083");
        // 发起请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    /**
     * 局部更新
     * @throws IOException
     */
    @Test
    void testUpdateDocument() throws IOException {
        // 准备request对象
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        request.doc(
                "price", "952",
                "starName", "四钻"
        );
        // 发起请求
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteDocument() throws IOException {

        DeleteRequest request = new DeleteRequest("hotel", "61083");

        client.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量导入
     * @throws IOException
     */
    @Test
    void testBulkDocument() throws IOException {

        List<Hotel> hotels = hotelService.list();


        BulkRequest request = new BulkRequest();
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        }
        client.bulk(request, RequestOptions.DEFAULT);
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
