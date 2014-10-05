package com.googlecode.jvmvm.vm.placeholders;

import com.googlecode.jvmvm.SilentObjectCreator;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Mapper implements PlaceholderFactory {

    public class Placeholder implements com.googlecode.jvmvm.vm.placeholders.Placeholder {
        HashMap fields;
        Class cls;

        @Override
        public Object restore() {
            try {
                Object obj = SilentObjectCreator.create(cls, Object.class);

                List<Field> fieldList = new ArrayList<Field>();
                Class tmpClass = cls;
                while (tmpClass != null) {
                    fieldList.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
                    tmpClass = tmpClass.getSuperclass();
                }
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                for (Field field : fieldList) {
                    field.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    field.set(obj, fields.get(field.getDeclaringClass().getName() + "/" + field.getName()));
                }
                return obj;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public Serializable replace(Object original) {
        try {
            Placeholder placeholder = new Placeholder();
            placeholder.cls = original.getClass();
            placeholder.fields = new HashMap();

            List<Field> fieldList = new ArrayList<Field>();
            Class tmpClass = original.getClass();
            while (tmpClass != null) {
                fieldList.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
                tmpClass = tmpClass.getSuperclass();
            }
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            for (Field field : fieldList) {
                field.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                placeholder.fields.put(field.getDeclaringClass().getName() + "/" + field.getName(), field.get(original));
            }
            return placeholder;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
