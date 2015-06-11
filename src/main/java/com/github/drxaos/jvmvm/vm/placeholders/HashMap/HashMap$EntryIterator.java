package com.github.drxaos.jvmvm.vm.placeholders.HashMap;

import com.github.drxaos.jvmvm.vm.placeholders.PlaceholderFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

public class HashMap$EntryIterator implements PlaceholderFactory {
    static String CLASS_NAME = "java.util.HashMap$EntryIterator";
    static String SUPER_NAME = "java.util.HashMap$HashIterator";
    static Field fThis0;
    static Field fIndex;

    static {
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            fThis0 = Class.forName(CLASS_NAME).getDeclaredField("this$0");
            fThis0.setAccessible(true);
            modifiersField.setInt(fThis0, fThis0.getModifiers() & ~Modifier.FINAL);

            fIndex = Class.forName(SUPER_NAME).getDeclaredField("index");
            fIndex.setAccessible(true);
            modifiersField.setInt(fIndex, fIndex.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public class Placeholder implements com.github.drxaos.jvmvm.vm.placeholders.Placeholder {
        HashMap owner;
        int index;

        @Override
        public Object restore() {
            try {
                Iterator iterator = owner.entrySet().iterator();

                while (fIndex.getInt(iterator) != index) {
                    iterator.next();
                }
                return iterator;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public Serializable replace(Object original) {
        try {
            Placeholder placeholder = new Placeholder();
            placeholder.owner = (HashMap) fThis0.get(original);
            placeholder.index = fIndex.getInt(original);
            return placeholder;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
