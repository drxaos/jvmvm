package com.googlecode.jvmvm.vm.placeholders;

import java.io.Serializable;

public interface PlaceholderFactory extends Serializable {
    String getClassName();

    Serializable replace(Object original);
}
