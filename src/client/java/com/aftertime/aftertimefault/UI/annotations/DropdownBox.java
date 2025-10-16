package com.aftertime.aftertimefault.UI.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DropdownBox {
    String key();
    String title();
    String[] options();
}
