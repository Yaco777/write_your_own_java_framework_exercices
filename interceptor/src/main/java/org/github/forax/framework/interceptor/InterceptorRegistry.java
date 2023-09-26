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

  public void addAroundAdvice(Class<? extends Annotation> annotationClass,AroundAdvice aroundAdvice) {

    Objects.requireNonNull(aroundAdvice);
    Objects.requireNonNull(annotationClass);
    advice = aroundAdvice;
    annotationMap.computeIfAbsent(annotationClass, __ -> new ArrayList<>()).add(aroundAdvice);
  }

  public void addInterceptor(Class<? extends Annotation> annotationClass, Interceptor interceptor) {
      Objects.requireNonNull(annotationClass);
      Objects.requireNonNull(interceptor);
  }

  public <T> T createProxy(Class<T> interfaceType, T instance) {
    Objects.requireNonNull(interfaceType);
    Objects.requireNonNull(instance);
    return interfaceType.cast(Proxy.newProxyInstance(interfaceType.getClassLoader(),new Class<?>[] {interfaceType},
            (Object proxy,Method method, Object[] args) -> {
                var advices = findAdvices(method);
                for(var advice : advices) {
                    advice.before(instance,method,args);
                }

                Object result = null;
                try {
                    result =  Utils.invokeMethod(instance,method,args);
                    return result;
                } finally {
                    for(var advice : advices) {
                        advice.after(instance,method,args,result);
                    }

                }
            }));
  }

  List<AroundAdvice> findAdvices(java.lang.reflect.Method method) {
    return Arrays.stream(method.getAnnotations())
            .flatMap(annotation -> annotationMap.getOrDefault(annotation.annotationType(), List.of()).stream())
            .toList();
  }




}
