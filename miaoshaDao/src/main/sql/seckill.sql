-- 秒杀执行存储过程
DELIMITER $$ --console ; 转换为 $$
-- row_count():返回上一条修改类型sql(delete,insert,update)的影响行数
-- row_count():0:未修改数据；>0：标识修改的行数；<0：sql错误/未执行修改sql
CREATE PROCEDURE seckill.excute_seckill
    (IN v_seckill_id BIGINT,IN v_phone BIGINT,
     IN v_kill_time TIMESTAMP ,OUT r_result INT)
     BEGIN
        DECLARE insert_count INT DEFAULT 0;
        START TRANSACTION ;
        INSERT IGNORE INTO success_killer(seckill_id,user_phone,create_time)
        VALUES (v_seckill_id,v_phone,v_kill_time);
        SELECT ROW_COUNT() INTO insert_count;
        IF(insert_count = 0) THEN
            ROLLBACK;
            SET r_result = -1;
        ELSEIF(insert_count < 0)THEN
            ROLLBACK;
            SET r_result = -2;
        ELSE
            UPDATE seckill
            SET number = number-1
            WHERE seckill_id = v_seckill_id
                AND end_time > v_kill_time
                AND start_time < v_kill_time
                AND number > 0;
            SELECT ROW_COUNT() INTO insert_count;
            IF (insert_count = 0) THEN
                ROLLBACK ;
                SET r_result = 0;
            ELSEIF(insert_count < 0)THEN
                ROLLBACK;
                SET r_result = -2;
            ELSE
                COMMIT;
                SET r_result = 1;
            END IF;
        END IF;
    END
$$

delimiter ;

set @r_result = -3;

call execute_seckill(1003,15802061574,now(),@r_result);

select @r_result;