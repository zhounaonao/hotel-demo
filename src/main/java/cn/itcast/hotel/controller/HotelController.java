package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.pojo.ResponseResult;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周闹闹
 * @version 1.0
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    IHotelService hotelService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody RequestParams params){
        return hotelService.search(params);
    }

    @PostMapping("/hotels")
    public ResponseResult hotels(@RequestBody RequestParams params){
        return hotelService.search(params);
    }

}
