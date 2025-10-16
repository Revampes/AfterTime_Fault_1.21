package com.aftertime.aftertimefault.UI.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NormalButton {
    String key();
    String title();
    String action();
}
