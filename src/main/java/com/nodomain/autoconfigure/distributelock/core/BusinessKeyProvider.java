package com.nodomain.autoconfigure.distributelock.core;

import com.nodomain.autoconfigure.distributelock.annotation.DistributeLock;
import com.nodomain.autoconfigure.distributelock.annotation.DistributeLockKey;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: zph
 * @date: 2024/02/04
 * @description: 获取用户定义业务key
 */
public class BusinessKeyProvider {

    private ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private ExpressionParser parser = new SpelExpressionParser();

    public String getKeyName(JoinPoint joinPoint, DistributeLock dLock) {
        List<String> keyList = new ArrayList<>();
        //拿目标方法
        Method method = getMethod(joinPoint);
        //获取注解的keys对应的值
        List<String> definitionKeys = getSpelDefinitionKey(dLock.keys(), method, joinPoint.getArgs());
        keyList.addAll(definitionKeys);
        //解析修饰参数的DistributeLockKey 注解对应的值
        List<String> parameterKeys = getParameterKey(method.getParameters(), joinPoint.getArgs());
        keyList.addAll(parameterKeys);
        //将这些值的集合以-进行拼接
        return StringUtils.collectionToDelimitedString(keyList,"","-","");
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    private List<String> getSpelDefinitionKey(String[] definitionKeys, Method method, Object[] parameterValues) {//解析spel表达式如 "#userId"，把parameterValues对应的userId的值解析出来。
        List<String> definitionKeyList = new ArrayList<>();
        for (String definitionKey : definitionKeys) {
            if (!ObjectUtils.isEmpty(definitionKey)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
                //用spring提供的api解析出definitionKey的值
                Object objKey = parser.parseExpression(definitionKey).getValue(context);
                definitionKeyList.add(ObjectUtils.nullSafeToString(objKey));
            }
        }
        return definitionKeyList;
    }

    //处理修饰参数的DistributeLockKey 注解
    private List<String> getParameterKey(Parameter[] parameters, Object[] parameterValues) {
        List<String> parameterKey = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(DistributeLockKey.class) != null) {
                DistributeLockKey keyAnnotation = parameters[i].getAnnotation(DistributeLockKey.class);
                if (keyAnnotation.value().isEmpty()) {
                    Object parameterValue = parameterValues[i];
                    parameterKey.add(ObjectUtils.nullSafeToString(parameterValue));
                } else {
                    StandardEvaluationContext context = new StandardEvaluationContext(parameterValues[i]);
                    Object key = parser.parseExpression(keyAnnotation.value()).getValue(context);
                    parameterKey.add(ObjectUtils.nullSafeToString(key));
                }
            }
        }
        return parameterKey;
    }

}
