package com.jchotel.service;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Customer;
import com.jchotel.entity.Room;
import com.jchotel.utils.Result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CustomerService {
    Result<PageResult<Customer>> list(PageQuery query);
    Result<Customer> detail(Long id);
    Result<String> add(Customer customer);
    Result<String> update(Customer customer);
    Result<String> delete(Long id);
    Result<String> addToBlacklist(Long id, String reason);
    Result<String> removeFromBlacklist(Long id);
    Result<List<Customer>> birthdayList();
    Result<List<Customer>> blacklist();
    Result<Integer> upgradeVipLevel(Long customerId);
    Result<String> addSpent(Long customerId, BigDecimal amount, LocalDateTime stayTime);
    Result<List<Room>> recommendRooms(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut);
    Result<Map<String, Object>> getCustomerStats(Long customerId);
}
