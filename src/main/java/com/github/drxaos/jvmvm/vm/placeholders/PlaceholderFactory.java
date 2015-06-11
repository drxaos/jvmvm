package com.github.drxaos.jvmvm.vm.placeholders;

import java.io.Serializable;

public interface PlaceholderFactory extends Serializable {
    String getClassName();

    Serializable replace(Object original);
}
