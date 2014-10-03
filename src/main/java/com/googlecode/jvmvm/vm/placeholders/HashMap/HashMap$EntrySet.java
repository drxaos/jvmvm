package com.googlecode.jvmvm.vm.placeholders.HashMap;

import com.googlecode.jvmvm.vm.placeholders.PlaceholderFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class HashMap$EntrySet implements PlaceholderFactory {
    static String CLASS_NAME = "java.util.HashMap$EntrySet";
    static Field fThis0;

    static {
        try {
            fThis0 = Class.forName(CLASS_NAME).getDeclaredField("this$0");
            fThis0.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(fThis0, fThis0.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public class Placeholder implements com.googlecode.jvmvm.vm.placeholders.Placeholder {
        HashMap owner;

        @Override
        public Object restore() {
            return owner.entrySet();
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
            return placeholder;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
