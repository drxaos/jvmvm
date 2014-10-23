package com.googlecode.jvmvm.ui;

import java.awt.*;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface Vm {
    List<String> bootstrap = Arrays.asList(
            Object.class.getName(),

            String.class.getName(),
            StringBuilder.class.getName(),
            CharSequence.class.getName(),

            Color.class.getName(),

            PrintStream.class.getName(),

            Character.class.getName(),
            Boolean.class.getName(),
            Number.class.getName(),
            Byte.class.getName(),
            Short.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Float.class.getName(),
            Double.class.getName(),

            Math.class.getName(),

            Iterable.class.getName(),
            Iterator.class.getName(),
            Collection.class.getName(),

            StackTraceElement.class.getName(),
            Throwable.class.getName(),
            Exception.class.getName(),
            IllegalArgumentException.class.getName(),
            RuntimeException.class.getName(),
            UnsupportedOperationException.class.getName(),
            NoSuchElementException.class.getName(),
            UnsupportedEncodingException.class.getName(),

            Serializable.class.getName(),

            Arrays.class.getName(),
            Collections.class.getName(),

            List.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName(),

            Set.class.getName(),
            HashSet.class.getName(),

            Map.class.getName(),
            HashMap.class.getName(),
            TreeMap.class.getName()
    );
}
