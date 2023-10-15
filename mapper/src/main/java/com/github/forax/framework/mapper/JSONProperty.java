package com.github.forax.framework.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME) /*quand est ce que c'est présent/dispo*/
@Target({METHOD, RECORD_COMPONENT}) /*sur quoi on peut placer*/
public @interface JSONProperty {
  String value();
}
