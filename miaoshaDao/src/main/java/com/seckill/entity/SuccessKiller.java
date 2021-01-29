package com.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessKiller {
    private long seckillId;

    private long userPhone;

    private short state;

    private Date createTime;

    private Seckill seckill;
}
