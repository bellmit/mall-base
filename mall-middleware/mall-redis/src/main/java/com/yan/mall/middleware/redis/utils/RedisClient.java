package com.yan.mall.middleware.redis.utils;

import com.yan.mall.utils.SpringUtil;
import io.micrometer.core.instrument.util.StringUtils;
import org.redisson.api.*;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RedisClient {

    public static final long MIN_ONE = 60;
    public static final long MIN_TEN = 600;

    private static RedissonClient getRedissonClient() {
        return SpringUtil.getBean(RedissonClient.class);
    }

    public static void setRAtomicLong(String key, Long value) {
        RAtomicLong rAtomicLong = getRedissonClient().getAtomicLong(key);
        rAtomicLong.set(value);
    }

    public static Long incrementAndGet(String key) {
        RAtomicLong rAtomicLong = getRedissonClient().getAtomicLong(key);
        return rAtomicLong.incrementAndGet();
    }

    public static <T> T get(String key, Supplier<T> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        RBucket<T> rBucket = getRedissonClient().getBucket(key);
        if (!rBucket.isExists()) {
            T result = supplier.get();
            rBucket.set(result, expire, timeUnit);
            return result;
        }

        return rBucket.get();
    }

    public static <T> void set(String key, T value, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        RBucket<T> rBucket = getRedissonClient().getBucket(key);
        if (!rBucket.isExists()) {
            rBucket.set(value, expire, timeUnit);
        }
        rBucket.set(value, expire, timeUnit);
    }

    public static <T> RLock tryLock(String key, long timout) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        RLock lock = getRedissonClient().getLock(key);
        try {
            if (lock.tryLock(0, timout, TimeUnit.SECONDS)) {
                return lock;
            }
        } catch (InterruptedException e) {
        }

        return null;
    }

    public static RLock getLock(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        return getRedissonClient().getLock(key);
    }

    public static Boolean tryRateLimit(String key, Integer rate, RateIntervalUnit timeUnit) {
        if (key == null) {
            return false;
        }

        RRateLimiter rateLimiter = getRedissonClient().getRateLimiter(key);
        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(RateType.OVERALL, rate, 1, timeUnit);
        }
        return rateLimiter.tryAcquire(1);
    }

    public static void deleteRateLimit(String key) {
        if (key == null) {
            return;
        }

        RRateLimiter rateLimiter = getRedissonClient().getRateLimiter(key);
        rateLimiter.delete();
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static <T> void sAdd(String key, Supplier<T> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        T result = supplier.get();
        RSet<T> rSet = getRedissonClient().getSet(key);
        if (!rSet.isExists()) {
            rSet.expire(expire, timeUnit);
        }
        rSet.add(result);
    }

    public static <T> void sAddAll(String key, Supplier<List<T>> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        List<T> result = supplier.get();
        RSet<T> rSet = getRedissonClient().getSet(key);
        if (!rSet.isExists()) {
            rSet.expire(expire, timeUnit);
        }
        rSet.addAll(result);
    }

    public static <T> boolean sContains(String key, T value) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        RSet<T> rSet = getRedissonClient().getSet(key);
        return rSet.isExists() ? rSet.contains(value) : false;
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static <T> void lAdd(String key, Supplier<T> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        T result = supplier.get();
        RList<T> rList = getRedissonClient().getList(key);
        if (!rList.isExists()) {
            rList.expire(expire, timeUnit);
        }
        rList.add(result);
    }

    public static <T> void lAddAll(String key, Supplier<List<T>> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        List<T> result = supplier.get();
        RList<T> rList = getRedissonClient().getList(key);
        if (!rList.isExists()) {
            rList.expire(expire, timeUnit);
        }
        rList.addAll(result);
    }

    public static <T> List<T> lSubList(String key, int fromIndex, int toIndex) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }

        RList<T> rList = getRedissonClient().getList(key);
        return rList.isExists() ? rList.subList(fromIndex, toIndex) : Collections.emptyList();
    }

    public static <T> List<T> lReadAll(String key) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        RList<T> rList = getRedissonClient().getList(key);
        return rList.isExists() ? rList.readAll() : Collections.emptyList();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    public static <T> void zAdd(String key, double score, Supplier<T> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        T result = supplier.get();
        RScoredSortedSet<T> scoredSortedSet = getRedissonClient().getScoredSortedSet(key);
        if (!scoredSortedSet.isExists()) {
            scoredSortedSet.expire(expire, timeUnit);
        }
        scoredSortedSet.add(score, result);
    }

    public static <T> Collection<T> zValueRange(String key, double startScore, double endScore, boolean reversed) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }

        RScoredSortedSet<T> scoredSortedSet = getRedissonClient().getScoredSortedSet(key);
        return reversed ? scoredSortedSet.valueRangeReversed(startScore, true, endScore, true) : scoredSortedSet.valueRange(startScore, true, endScore, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static <T> void mPut(String key, String field, Supplier<T> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        T result = supplier.get();
        RMap<String, T> rMap = getRedissonClient().getMapCache(key);
        if (!rMap.isExists()) {
            rMap.expire(expire, timeUnit);
        }
        rMap.put(field, result);
    }

    public static <T> void mPut(String key, String field, T result, Date expireAt) {
        if (StringUtils.isBlank(key)) {
            return;
        }

        RMap<String, T> rMap = getRedissonClient().getMap(key);
        if (!rMap.isExists()) {
            rMap.put(field, result);
            rMap.expireAt(expireAt);
        } else {
            rMap.put(field, result);
        }
    }

    /*public static <T> void mPutAll(String key, Supplier<T> supplier, int expire, TimeUnit timeUnit) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (StringUtils.isBlank(key)) {
            return;
        }

        T result = supplier.get();
        RMap<String, String> rMap = getRedissonClient().getMap(key);
        if (!rMap.isExists()) {
            rMap.expire(expire, timeUnit);
        }
        rMap.putAll(BeanUtils.describe(result));
    }*/

    public static <T> T mGet(String key, String field) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        RMap<String, T> rMap = getRedissonClient().getMap(key);
        return rMap.isExists() ? rMap.get(field) : null;
    }

    public static <T> T mGet(String key, String field, Supplier<T> supplier, int expire, TimeUnit timeUnit) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        RMap<String, T> rMap = getRedissonClient().getMap(key);
        if (!rMap.isExists()) {
            T result = supplier.get();
            rMap.expire(expire, timeUnit);
            rMap.put(field, result);
            return result;
        }

        return  rMap.get(field);
    }

    public static <T> Map<String, T> mGetAll(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        return getRedissonClient().getMap(key);
    }

    ////////////////////////////////////////////////////////////////////////////////////

    public static void deleteKey(String key) {
        getRedissonClient().getBucket(key).delete();
    }

    public static void deleteMKey(String key) {
        getRedissonClient().getMap(key).delete();
    }

    public static void deleteKeys(List<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        getRedissonClient().getKeys().delete(keys.toArray(new String[]{}));
    }
}
