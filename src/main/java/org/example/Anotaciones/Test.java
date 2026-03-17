package org.example.Anotaciones;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // retinada por la vm
@Target(ElementType.METHOD)  // solo metodos
public @interface Test {

}