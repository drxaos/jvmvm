package com.github.drxaos.jvmvm.vm.placeholders;

import com.github.drxaos.jvmvm.SilentObjectCreator;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Mapper implements PlaceholderFactory {

    public class Placeholder implements com.github.drxaos.jvmvm.vm.placeholders.Placeholder {
        HashMap fields;
        Class cls;

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
                    Object val = fields.get(field.getDeclaringClass().getName() + "/" + field.getName());
                    field.set(obj, val);
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

    public String getClassName() {
        return "";
    }

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
                Object val = field.get(original);
                String key = field.getDeclaringClass().getName() + "/" + field.getName();
                placeholder.fields.put(key, val);
            }
            return placeholder;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
