package com.github.forax.framework.injector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;

public final class InjectorRegistry {

    private final HashMap<Class<?>,Supplier<?>> hashmap = new HashMap<>();

    public <T> void registerInstance(Class<T> type, T instance) {

        Objects.requireNonNull(type);
        Objects.requireNonNull(instance);
        registerProvider(type,() -> instance);
        /*var exist = hashmap.putIfAbsent(type,instance);
        if(exist == null) {
            throw new IllegalStateException("An object is already present in the hashmap for this type "+type.getName());
        }*/
    }

    public <T> T lookupInstance(Class<T> type) {
        Objects.requireNonNull(type);
        var checkPresent = hashmap.get(type);

        if(checkPresent == null) {
            throw new IllegalStateException("The type "+type+" is invalid, you need to use registerInstance before");
        }
        return type.cast(checkPresent.get());
    }

    public <T> void registerProvider(Class<T> type, Supplier<T> supplier) {

        Objects.requireNonNull(type);
        Objects.requireNonNull(supplier);
        var exist = hashmap.putIfAbsent(type,supplier);
    }

     static List<PropertyDescriptor> findInjectableProperties(Class<?> type) {
        var beanInfo = Utils.beanInfo(type);
        return Arrays.stream(beanInfo.getPropertyDescriptors())
                .filter(property -> {
                    var setter = property.getWriteMethod();
                    return setter != null && setter.getAnnotation(Inject.class) != null;
                })
                .toList();
    }

    private Optional<Constructor<?>> findInjectableConstructor(Class<?> type) {
        var constructors = Arrays.stream(type.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toList();
        return switch (constructors.size()) {
            case 0 ->  Optional.empty();
            case 1 ->  Optional.of(constructors.get(0));
            default -> throw new IllegalStateException("Too many injectable constructors "+type.getConstructors());
        };
    }

    public <T> void registerProviderClass(Class<T>type, Class<? extends T> providerClass) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(providerClass);
        var constructor = findInjectableConstructor(providerClass).orElseGet(() -> Utils.defaultConstructor(providerClass));


        var properties = findInjectableProperties(providerClass);

        registerProvider(type, () -> {
            var arguments = Arrays.stream(constructor.getParameterTypes()).map(this::lookupInstance).toArray();
            var instance = Utils.newInstance(constructor,arguments);

            for(var property : properties) {
                var setterType = property.getPropertyType();
                var argument = lookupInstance(setterType);

                Utils.invokeMethod(instance,property.getWriteMethod(),argument);
            }
            return providerClass.cast(instance);


        });


    }

    public <T> void registerProviderClass(Class<?> providerClass) {
        Objects.requireNonNull(providerClass);
        registerProviderClassImpl(providerClass);
    }

    private <T> void registerProviderClassImpl(Class<T> providerClass) {

        registerProviderClass(providerClass,providerClass);
    }


}