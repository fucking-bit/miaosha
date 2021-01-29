package com.seckill.dao;

import com.seckill.entity.SuccessKiller;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKillerMapperTest {

    @Autowired
    private SuccessKillerMapper successKillerMapper;
    @Test
    public void insertSuccessKiller() {
        long id = 1001L;
        long phone = 15802061574L;
        int inserCount = successKillerMapper.insertSuccessKiller(id, phone);
        System.out.println("inserCount="+inserCount);
    }

    @Test
    public void queryByIdWithSeckill() {
        long id = 1001L;
        long phone = 15802061574L;
        SuccessKiller successKiller = successKillerMapper.queryByIdWithSeckill(id, phone);
        System.out.println(successKiller);
        System.out.println(successKiller.getSeckill());
    }
}