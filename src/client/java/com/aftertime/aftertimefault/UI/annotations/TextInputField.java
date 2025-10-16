package com.aftertime.aftertimefault.UI.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TextInputField {
    String key();
    String title();
    int maxLength() default 32;
}
