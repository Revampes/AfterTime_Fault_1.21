package com.aftertime.aftertimefault.UI.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColorPicker {
    String key();
    String title();
}
