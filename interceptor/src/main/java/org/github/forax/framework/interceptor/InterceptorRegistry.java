package org.github.forax.framework.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class InterceptorRegistry {
  private AroundAdvice advice;
  private HashMap<Class<? extends Annotation>,List<AroundAdvice>> annotationMap = new HashMap<>();
  private HashMap<Class<? extends Annotation>, List<Interceptor>> interceptorMap = new HashMap<>();

    private final HashMap<java.lang.reflect.Method, Invocation> cache = new HashMap<>();

  public void addAroundAdvice(Class<? extends Annotation> annotationClass,AroundAdvice aroundAdvice) {

    /*Objects.requireNonNull(aroundAdvice);
    Objects.requireNonNull(annotationClass);
    advice = aroundAdvice;
    annotationMap.computeIfAbsent(annotationClass, __ -> new ArrayList<>()).add(aroundAdvice);*/
      Objects.requireNonNull(annotationClass);
      Objects.requireNonNull(aroundAdvice);
      addInterceptor(annotationClass, ((instance, method, args, invocation) -> {
        aroundAdvice.before(instance,method,args);
        Object result = null;
        try {
            result = invocation.proceed(instance,method,args);
            return result;
        } finally {
            aroundAdvice.after(instance,method,args,result);
        }


    }));

  }

  public void addInterceptor(Class<? extends Annotation> annotationClass, Interceptor interceptor) {
      Objects.requireNonNull(annotationClass);
      Objects.requireNonNull(interceptor);
      interceptorMap.computeIfAbsent(annotationClass, __ -> new ArrayList<>()).add(interceptor);
      cache.clear(); //invalidate cache after adding a new interceptor

  }

    List<Interceptor> findInterceptors(java.lang.reflect.Method method) {

        method.getDeclaringClass().getAnnotations();
        method.getParameterAnnotations();

        /*return Arrays.stream(method.getAnnotations())
                .flatMap(annotation -> interceptorMap.getOrDefault(annotation.annotationType(), List.of()).stream())
                .toList();*/



        return Stream.of(
                Arrays.stream(method.getDeclaringClass().getAnnotations()),
                        Arrays.stream(method.getAnnotations()),
                        Arrays.stream(method.getParameterAnnotations()).flatMap(Arrays::stream))
                .flatMap(s -> s)
                        .flatMap(annotation -> interceptorMap.getOrDefault(annotation.annotationType(), List.of()).stream())
                        .toList();

    }

  public <T> T createProxy(Class<T> interfaceType, T instance) {
    Objects.requireNonNull(interfaceType);
    Objects.requireNonNull(instance);
    return interfaceType.cast(Proxy.newProxyInstance(interfaceType.getClassLoader(),new Class<?>[] {interfaceType},
            (Object proxy,Method method, Object[] args) -> {
//                var advices = findAdvices(method);
//                for(var advice : advices) {
//                    advice.before(instance,method,args);
//                }
//
//                Object result = null;
//                try {
//                    result =  Utils.invokeMethod(instance,method,args);
//                    return result;
//                } finally {
//                    for(var advice : advices) {
//                        advice.after(instance,method,args,result);
//                    }
//
//                }
                //var interceptors = findInterceptors(method);
                var invocation = computeInvocation(method);
                return invocation.proceed(instance, method,args);
            }));
  }

    private Invocation computeInvocation(Method method) {

        return cache.computeIfAbsent(method, key -> getInvocation(findInterceptors(method)));
    }

  List<AroundAdvice> findAdvices(java.lang.reflect.Method method) {
    return Arrays.stream(method.getAnnotations())
            .flatMap(annotation -> annotationMap.getOrDefault(annotation.annotationType(), List.of()).stream())
            .toList();
  }

    static Invocation getInvocation(List<Interceptor>interceptorList) {
       /* Invocation invocation = (instance, method, args) -> {
            return Utils.invokeMethod(instance,method,args);
        };*/
        Invocation invocation = Utils::invokeMethod;
        for(var interceptor : interceptorList) {  //reversedList
            var previousInvocation = invocation;
            invocation = (instance, method, args) -> interceptor.intercept(instance,method,args,previousInvocation);

        }
        return invocation;
    }






}
