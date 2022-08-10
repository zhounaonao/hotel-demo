package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.pojo.ResponseResult;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public ResponseResult search(RequestParams params) {
        ResponseResult responseResult = new ResponseResult();

        try {
            SearchRequest request = new SearchRequest("hotel");

            SearchSourceBuilder source = request.source();

            if (StringUtils.isNotEmpty(params.getKey())){
                source.query(QueryBuilders.matchQuery("all", params.getKey()));
            } else {
                source.query(QueryBuilders.matchAllQuery());
            }
            source
                    .from((params.getPage() - 1) * params.getSize())
                    .size(params.getSize());
            if (!"default".equals(params.getSortBy())) {
                source.sort(params.getSortBy());
            }

            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

            SearchHits searchHits = response.getHits();
            long total = searchHits.getTotalHits().value;
            List<HotelDoc> hotels = Arrays.stream(searchHits.getHits())
                    .map(documentFields -> JSON.parseObject(documentFields.getSourceAsString(), HotelDoc.class))
                    .collect(Collectors.toList());
            responseResult.setTotal(total);
            responseResult.setHotels(hotels);
        } catch (IOException e) {
            new RuntimeException("查询出错");
        }

        return responseResult;
    }
}
