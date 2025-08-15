package io.github.bmarwell.jdtfmt.its.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(JdtFmtExtension.class)
public @interface JdtFmtTest {
    String[] args() default {};
}
