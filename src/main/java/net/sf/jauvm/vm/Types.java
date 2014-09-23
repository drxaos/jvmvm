/**
 * Copyright (c) 2005 Nuno Cruces
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package net.sf.jauvm.vm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import org.objectweb.asm.Type;

public class Types {
    protected Types() {
    }

    public static String getInternalName(String desc) {
        switch (desc.charAt(0)) {
            case 'L':
                return Type.getType(desc).getInternalName();
            case '[':
                return desc;
            case 'I':
                return int.class.getName();
            case 'J':
                return long.class.getName();
            case 'F':
                return float.class.getName();
            case 'D':
                return double.class.getName();
            case 'C':
                return char.class.getName();
            case 'B':
                return byte.class.getName();
            case 'S':
                return short.class.getName();
            case 'Z':
                return boolean.class.getName();
            default:
                return null;
        }
    }

    public static String getInternalName(Type type) {
        return getInternalName(type.getDescriptor());
    }

    public static String getInternalName(Class<?> cls) {
        return Type.getInternalName(cls);
    }

    public static String getInternalName(Member member) {
        Class<?> cls = member.getDeclaringClass();
        StringBuilder builder = new StringBuilder(getInternalName(cls)).append('/');

        if (member instanceof Method) {
            Method m = (Method) member;
            builder.append(m.getName()).append(getDescriptor(m));
        } else if (member instanceof Field) {
            Field f = (Field) member;
            builder.append(f.getName()).append(' ').append(getDescriptor(f));
        } else {
            Constructor<?> c = (Constructor<?>) member;
            builder.append("<init>").append(getDescriptor(c));
        }

        return builder.toString();
    }

    public static String getDescriptor(Type type) {
        return type.getDescriptor();
    }

    public static String getDescriptor(Class<?> cls) {
        return Type.getDescriptor(cls);
    }

    public static String getDescriptor(Member member) {
        if (member instanceof Method) {
            Method m = (Method) member;
            return Type.getMethodDescriptor(m);
        } else if (member instanceof Field) {
            Field f = (Field) member;
            return Type.getDescriptor(f.getType());
        } else {
            Constructor<?> c = (Constructor<?>) member;
            StringBuilder builder = new StringBuilder("(");
            for (Class<?> t : c.getParameterTypes()) builder.append(Type.getDescriptor(t));
            return builder.append(")V").toString();
        }
    }
}
