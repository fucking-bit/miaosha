package com.seckill.dto;

import com.seckill.entity.SuccessKiller;
import com.seckill.enums.SeckillStateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillExecution {

    private long seckillId;

    //秒杀执行状态结果
    private int state;

    //状态信息
    private String stateInfo;

    //秒杀成功对象
    private SuccessKiller successKiller;

    public SeckillExecution(long seckillId, SeckillStateEnum stateEnum) {
        this.seckillId = seckillId;
        this.state = stateEnum.getState();
        this.stateInfo = stateEnum.getStateInfo();
    }

    public SeckillExecution(long seckillId, SeckillStateEnum stateEnum, SuccessKiller successKiller) {
        this.seckillId = seckillId;
        this.state = stateEnum.getState();
        this.stateInfo = stateEnum.getStateInfo();
        this.successKiller = successKiller;
    }
}
