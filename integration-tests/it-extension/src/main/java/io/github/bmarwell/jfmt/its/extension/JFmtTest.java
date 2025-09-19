package io.github.bmarwell.jfmt.its.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(JFmtExtension.class)
public @interface JFmtTest {
    String[] args() default {};
}
