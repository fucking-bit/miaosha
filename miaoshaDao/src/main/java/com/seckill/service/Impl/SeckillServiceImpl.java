package com.seckill.service.Impl;

import com.seckill.dao.SeckillMapper;
import com.seckill.dao.SuccessKillerMapper;
import com.seckill.dao.cache.RedisDao;
import com.seckill.dto.Exposer;
import com.seckill.dto.SeckillExecution;
import com.seckill.entity.Seckill;
import com.seckill.entity.SuccessKiller;
import com.seckill.enums.SeckillStateEnum;
import com.seckill.exception.RepeatKillException;
import com.seckill.exception.SeckillCloseException;
import com.seckill.exception.SeckillException;
import com.seckill.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillMapper seckillMapper;
    @Autowired
    private SuccessKillerMapper successKillerMapper;
    @Autowired
    private RedisDao redisDao;

    //md5盐值字符串
    private final String slat = "daasd@as@#!@$!#%@#";
    @Override
    public List<Seckill> getSeckillList() {
        return seckillMapper.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillMapper.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化:超时的基础上维护一致性
        //1：访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){
            //2：访问数据库
            seckill = seckillMapper.queryById(seckillId);
            if (seckill == null)
                return new Exposer(false,seckillId);
            else
                redisDao.putSeckill(seckill);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.compareTo(startTime)== -1 || nowTime.compareTo(endTime) == 1)
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());

        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base = seckillId + "/" +slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点:
     * 1：开发团队达成一致约定，明确标注事务的编程风格。
     * 2：保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP，或者剥离到事务方法外部
     * 3：不是所有的方法都需要事务，如果只有一条修改操作，只操作不需要事务控制
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        if (md5 == null || !md5.equals(getMD5(seckillId)))
            throw new SeckillException("seckill data rewrite");
        //执行秒杀逻辑：减库存 + 记录购买记录
        Date nowTime = new Date();
        try{
            //记录购买行为
            int insertCount = successKillerMapper.insertSuccessKiller(seckillId, userPhone);
            //唯一：seckillId，userPhone
            if (insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else {
                //减库存,热点商品竞争
                int updateCount = seckillMapper.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0){
                    //没有跟新到记录，秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed");
                }else {
                    //秒杀成功 commit
                    SuccessKiller successKiller = successKillerMapper.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,successKiller);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e3){
            logger.error(e3.getMessage());
            throw  new SeckillException("seckill inner error:"+e3.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId)))
            return new SeckillExecution(seckillId,SeckillStateEnum.DATA_REWRITE);
        Date killTime = new Date();
        HashMap<String, Object> map = new HashMap<>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",null);
        try {
            seckillMapper.killByProcedure(map);
            Integer result = MapUtils.getInteger(map, "result", -2);
            if (result == 1){
                SuccessKiller sk = successKillerMapper.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,sk);
            }else {
                return new SeckillExecution(seckillId,SeckillStateEnum.stateOf(result));
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return new SeckillExecution(seckillId,SeckillStateEnum.INNER_ERROR);
        }
    }
}
