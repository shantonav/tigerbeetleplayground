package com.drc.poc.drcdemo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IdempotencyService {
    private final Map<String, Long> map = new HashMap<>();

    public void store(String accountName, Long accountNumber){
        map.put(accountName, accountNumber);
    }

    public boolean check(String accountName, Long accountNumber){
        return map.containsKey(accountName);
    }
}
