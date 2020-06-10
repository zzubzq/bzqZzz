package com.bzq.bzqZzz.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

/**
 * 对查询(通常)方法有入参长度限制，查询所有数据可以使用的工具类
 * @author bai
 * @date 2019-08-14 12:54
 */
public class SizeLimitQueryUtil {
    /**
     * 适用于参数是单个List对象，结果也是List对象的情况
     * @param maxSize 入参长度限制
     * @param paramList 参数list
     * @param function 查询方法
     * @param <R> 结果对象
     * @param <P> 参数对象
     * @return list R
     */
    public static <R, P> List<R> query(int maxSize, List<P> paramList, Function<List<P>, List<R>> function) {
        if (CollectionUtils.isEmpty(paramList)) {
            return Lists.newArrayList();
        }
        List<R> resultList = Lists.newArrayList();
        int index = 0;
        do {
            List<P> tmpParamList = paramList.subList(index, Math.min((index + maxSize), paramList.size()));
            List<R> tmpResultList = function.apply(tmpParamList);
            if (CollectionUtils.isNotEmpty(tmpResultList)) {
                resultList.addAll(tmpResultList);
            }
            index += maxSize;
        } while (index < paramList.size());

        return resultList;
    }


    /**
     * 适用于参数除了有限制长度的list，还包含其它参数的情况
     * @param maxSize 最大限制数量
     * @param paramList 有长度限制参数list
     * @param o 方法执行对象
     * @param method 方法
     * @param otherArgs 其它参数
     * @param <R> 结果对象
     * @param <P> 参数对象
     * @return list R
     */
    public static <R, P> List<R> query(int maxSize, List<P> paramList, Object o, Method method, Class<R> rClass, Object... otherArgs) {
        if (CollectionUtils.isEmpty(paramList)) {
            return Lists.newArrayList();
        }
        List<R> resultList = Lists.newArrayList();
        int index = 0;
        do {
            List<P> tmpParamList = paramList.subList(index, Math.min((index + maxSize), paramList.size()));
            try {
                method.setAccessible(true);
                Object tmpResult = method.invoke(o, linkArgs(tmpParamList, otherArgs));
                if (CollectionUtils.isNotEmpty((List<R>) tmpResult)) {
                    resultList.addAll((List<R>) tmpResult);
                }
                index += maxSize;
            } catch (Exception e) {
                throw new RuntimeException("方法执行异常");
            }
        } while (index < paramList.size());

        return resultList;
    }


    private static Object[] linkArgs(Object firstArg, Object... otherArgs) {
        Object[] realArgs = new Object[otherArgs.length + 1];
        for (int i = 0; i < realArgs.length; i++) {
            if (i == 0) {
                realArgs[i] = firstArg;
            }else {
                realArgs[i] = otherArgs[i - 1];
            }
        }
        return realArgs;
    }

}
