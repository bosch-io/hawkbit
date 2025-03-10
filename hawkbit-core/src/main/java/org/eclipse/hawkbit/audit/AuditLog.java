package org.eclipse.hawkbit.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditLog {
    enum Level {
        INFO, WARN, ERROR
    }

    String entity();
    String[] includeParams() default {};
    String message() default "";
    Level level() default Level.INFO;
    boolean logResponse() default false;
}
