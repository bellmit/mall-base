package com.yan.mall.middleware.redis.aop;

import com.yan.mall.common.exception.BusinessException;
import com.yan.mall.middleware.redis.annotation.NeedRLock;
import com.yan.mall.middleware.redis.utils.RedisClient;
import com.yan.mall.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by huyan on 2021/12/8.
 * TIME: 8:38
 * DESC:
 */
@Aspect
@EnableAspectJAutoProxy
@Component
@Slf4j
public class RLockAOP {

    @Around("@annotation(com.yan.mall.middleware.redis.annotation.NeedRLock)")
    public Object aounrd(ProceedingJoinPoint point) throws Throwable {
        //获取拦截的方法名
        Signature sig = point.getSignature();
        MethodSignature msig = null;
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        msig = (MethodSignature) sig;

        Method currentMethod = point.getTarget().getClass().getMethod(msig.getName(), msig.getParameterTypes());
        NeedRLock needRLock = currentMethod.getAnnotation(NeedRLock.class);
        if (CommonUtil.isEmpty(needRLock.lockName()) || CommonUtil.isEmpty(needRLock.lockKey())) {
            return point.proceed(point.getArgs());
        }

        //buildKey
        String lockName = this.buildKey(currentMethod, needRLock, point.getArgs());
        log.info("<{}>" + currentMethod.getName() + " start", lockName);

        //加锁
        RLock rlock = null;
        Object result;
        try {
            rlock = RedisClient.tryLock(lockName, needRLock.timeout());
            if (rlock != null) {
                log.info("<{}>" + currentMethod.getName() +" doing", lockName);
                // 业务操作
                result = point.proceed(point.getArgs());
            } else {
                throw new BusinessException(needRLock.errMsg());
            }
        } finally {
            if (rlock != null) {
                // 释放锁
                rlock.unlock();
            }
        }
        log.info("<{}>" + currentMethod.getName() +" end", lockName);
        return result;
    }

    private String buildKey(Method method, NeedRLock needRLock, Object[] args) {

        StringBuilder result = new StringBuilder(needRLock.lockName());
        if (CommonUtil.isNotEmpty(needRLock.lockKey())) {
            LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
            String[] paraNameAttr = u.getParameterNames(method);
            if (paraNameAttr == null) {
                throw new BusinessException("没有入参，配置错误");
            }
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();

            for (int i = 0; i < paraNameAttr.length; i++) {
                context.setVariable(paraNameAttr[i], args[i]);
            }

            Arrays.asList(needRLock.lockKey().split(",")).forEach(key ->{
                result.append("_").append(parser.parseExpression(key).getValue(context));
            });
        }

        return result.toString();
    }
}
