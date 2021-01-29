package com.seckill.dao;

import com.seckill.entity.SuccessKiller;
import org.apache.ibatis.annotations.Param;

public interface SuccessKillerMapper {
    /**
     * 插入购买明细，可过滤重复
     * @return
     */
    int insertSuccessKiller(@Param("seckillId") long seckillId, @Param("userPhone")long userPhone);

    /**
     * 根据id查询SuccessKiller并携带秒杀产品对象实体
     * @param seckillId
     * @return
     */
    SuccessKiller queryByIdWithSeckill(@Param("seckillId")long seckillId,@Param("userPhone")long userPhone);

}
