package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 周闹闹
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult {
    private long total;
    private List<HotelDoc> hotels;


}
