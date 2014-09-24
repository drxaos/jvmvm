package net.sf.jauvm;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SilentObjectCreator {
    public static <T> T create(Class<T> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return create(clazz, Object.class);
    }

    public static <T> T create(Class<T> clazz, Class<? super T> parent) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return create(clazz, parent, null, null);
    }

    public static <T> T create(Class<T> clazz, Class<? super T> parent, Class<?>[] paramTypes, Object[] paramValues) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        if (paramTypes == null) {
            paramTypes = new Class[0];
        }
        if (paramValues == null) {
            paramValues = new Object[0];
        }
        ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
        Constructor objDef = parent.getDeclaredConstructor(paramTypes);
        Constructor intConstr = rf.newConstructorForSerialization(clazz, objDef);
        return clazz.cast(intConstr.newInstance(paramValues));
    }
}
