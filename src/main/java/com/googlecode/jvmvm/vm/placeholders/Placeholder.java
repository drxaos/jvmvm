package com.googlecode.jvmvm.vm.placeholders;

import java.io.Serializable;

public interface Placeholder extends Serializable {
    Object restore();
}
