package com.github.forax.framework.mapper;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JSONWriter {

  @FunctionalInterface
  private interface Generator {
    String generate(JSONWriter writer, Object bean);
  }
  


  private static final ClassValue<List<Generator>> CACHE = new ClassValue<>() {
    @Override
    protected List<Generator> computeValue(Class<?> type) {
      var beanInfo = Utils.beanInfo(type);

      return Arrays.stream(beanInfo.getPropertyDescriptors())
              .filter(property -> !property.getName().equals("class"))
              .<Generator>map(property -> {

                var readMethod = property.getReadMethod();
                var annotation = readMethod.getAnnotation(JSONProperty.class);
                var key = annotation == null ? "\""+property.getName()+"\"" : "\""+annotation.value()+"\"" ;

                return (writer, bean) -> key +": "+ writer.toJSON(Utils.invokeMethod(bean,readMethod));
              })
              .toList();
    }
  };

  public String toJSON(Object o) {


    return switch(o) {
      case null -> "null";
      case String s -> '"'+String.valueOf(s)+'"';
      case Boolean b -> b.toString();
      case Integer i -> i.toString();
      case Double d -> d.toString();
      default -> jsonBean(o);

    };


  }

  public String jsonBean(Object o) {

    return CACHE.get(o.getClass()).stream()
            .map(generator -> generator.generate(this,o))
            .collect(Collectors.joining(", ","{","}"));
  }
}
