package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * @author 周闹闹
 * @version 1.0
 */
@Data
public class RequestParams {
    private String key;
    private int page;
    private int size;
    private String sortBy;
    private String city;
    private String starName;
    private String brand;
    private double maxPrice;
    private double minPrice;

}
