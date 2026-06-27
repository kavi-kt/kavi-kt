package io.github.kavikt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static io.github.kavikt.JavaClassWithArrayValueAnnotation.AnnotationWithArrayValue;

@AnnotationWithArrayValue({
    Object.class, Boolean.class
})
public class JavaClassWithArrayValueAnnotation {

  @Retention(RetentionPolicy.RUNTIME)
  @interface AnnotationWithArrayValue {
    Class[] value();
  }

}
