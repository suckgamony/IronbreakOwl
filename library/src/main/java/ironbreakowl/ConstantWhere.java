package ironbreakowl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConstantWhere {
    int[] ints() default {};
    String[] strings() default {};
    boolean[] booleans() default {};
}