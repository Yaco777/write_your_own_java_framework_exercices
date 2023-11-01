package com.github.forax.framework.mapper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.lang.reflect.Type;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class JSONReader {
  private record BeanData(Constructor<?> constructor, Map<String, PropertyDescriptor> propertyMap) {
    PropertyDescriptor findProperty(String key) {
      var property = propertyMap.get(key);
      if (property == null) {
        throw new IllegalStateException("unknown key " + key + " for bean " + constructor.getDeclaringClass().getName());
      }
      return property;
    }
  }

  @FunctionalInterface
  public interface TypeMatcher {
    Optional<ObjectBuilder<?>> match(Type type);
  }

  public interface TypeReference<T> { }

  private final ArrayList<TypeMatcher> typeMatchers = new ArrayList<>();

  public void addTypeMatcher(TypeMatcher typeMatcher) {
    Objects.requireNonNull(typeMatcher);
    typeMatchers.add(typeMatcher);
  }

  ObjectBuilder<?> findObjectBuilder(Type type) {
    return typeMatchers.reversed().stream()
            .flatMap(typeMatcher -> typeMatcher.match(type).stream())
            .findFirst()
            .orElseGet(() -> ObjectBuilder.bean(Utils.erase(type)));
  }

  private record Context<T>(ObjectBuilder<T> objectBuilder, T result) {
    void populate(String key, Object value) {
      objectBuilder.populater.populate(result,key,value);
    }

    static <T> Context<T> createContext(ObjectBuilder<T> objectBuilder) {
      var instance = objectBuilder.supplier.get();
      return new Context<T>(objectBuilder,instance);
    }

    Object finish() {
      return objectBuilder.finisher.apply(result);
    }
  }



  public record ObjectBuilder<T>(Function<? super String, ? extends Type> typeProvider,
                                 Supplier<? extends T> supplier,
                                 Populater<? super T> populater,
                                 Function<? super T, ?> finisher) {
    public interface Populater<T> {
      void populate(T instance, String key, Object value);
    }

    public static ObjectBuilder<List<Object>> list(Type componentType) {
      Objects.requireNonNull(componentType);
      return new ObjectBuilder<List<Object>>(
              key -> componentType,
              ArrayList::new,
              (instance, key, value) -> instance.add(value),
              List::copyOf
      );

    }



    public static ObjectBuilder<Object[]> record(Class<?> recordClass) {

      Objects.requireNonNull(recordClass);
      var components = recordClass.getRecordComponents();
      var map = IntStream.range(0, components.length)
              .boxed()
              .collect(Collectors.toMap(i -> components[i].getName(), Function.identity()));
      var constructor = Utils.canonicalConstructor(recordClass, components);
      return new ObjectBuilder<>(
              key -> components[map.get(key)].getGenericType(),
              () -> new Object[components.length],
              (array, key, value) -> array[map.get(key)] = value,
              array -> Utils.newInstance(constructor, array)
      );
    }

    public static ObjectBuilder<Object> bean(Class<?> beanClass) {
      var beanData = BEAN_DATA_CLASS_VALUE.get(beanClass);
      var constructor = beanData.constructor;
      return new ObjectBuilder<>(
        key -> beanData.findProperty(key).getWriteMethod().getGenericParameterTypes()[0],
              () -> Utils.newInstance(constructor),
              (instance,key,value) -> {
                var setter = beanData.findProperty(key).getWriteMethod();
                Utils.invokeMethod(instance,setter,value);
              },
              Function.identity()

              );
    }
  }



  private static final ClassValue<BeanData> BEAN_DATA_CLASS_VALUE = new ClassValue<>() {
    @Override
    protected BeanData computeValue(Class<?> type) {
      var beanInfo = Utils.beanInfo(type);
      //Function.identy() -->   p -> p
      var constructor = Utils.defaultConstructor(type);
      var map = Arrays.stream(beanInfo.getPropertyDescriptors())
              .filter(property -> !property.getName().equals("class"))
              .collect(toMap(PropertyDescriptor::getName, Function.identity()));
      return new BeanData(constructor,map);
    }
  };

  public <T> T parseJSON(String text, TypeReference<T> typeReference) {

    return parseJSON(text,giveMeTheTypeOfTheTypeReference(typeReference));
  }

  private <T> TypeReference<T> giveMeTheTypeOfTheTypeReference(TypeReference<T> typeReference) {
    throw new UnsupportedOperationException("d");
  }


  public <T> T parseJSON(String text, Class<T> beanClass) {

    return beanClass.cast(parseJSON(text,(Type) beanClass));//the cast is used to call the right parseJSON method
  }

  public Object parseJSON(String text, Type exceptedType) {
    Objects.requireNonNull(text);
    Objects.requireNonNull(exceptedType);

    var stack =  new ArrayDeque<Context<?>>();
    var visitor = new ToyJSONParser.JSONVisitor() {
      private Object result;

      @Override
      public void value(String key, Object value) {
        // call the corresponding setter on result


        var currentContext = stack.peek();
        currentContext.populate(key,value);


      }

      @Override
      public void startObject(String key) {
        //get the beanData and store it in the field
        //create an instance and store it in result

        var currentContext = stack.peek();
        var type = currentContext == null ?
                exceptedType : currentContext.objectBuilder.typeProvider.apply(key);

        var objectBuilder = findObjectBuilder(type);
        stack.push(Context.createContext(objectBuilder));
      }

      @Override
      public void endObject(String key) {
        var previousContext = stack.pop();
        var result = previousContext.finish();

        if(stack.isEmpty()) {
          this.result = result;
        }
        else {
          var currentContext = stack.peek(); //the context exist because the stack is not empty

          currentContext.populate(key,result);
        }
      }

      @Override
      public void startArray(String key) {
        startObject(key);

      }

      @Override
      public void endArray(String key) {
        endObject(key);

      }
    };
    ToyJSONParser.parse(text, visitor);

    return visitor.result;
  }
}