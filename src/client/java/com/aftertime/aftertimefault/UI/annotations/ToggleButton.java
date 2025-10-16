package com.aftertime.aftertimefault.UI.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ToggleButton {
    String key();
    String name();
    String description();
    String category();
}