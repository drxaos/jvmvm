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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import static java.lang.reflect.Modifier.*;

public class AccessControl {
    protected AccessControl() {
    }

    public static void checkPermision(Class<?> cls, Class<?> referringCls) {
        if (isPublic(cls.getModifiers()) || cls.getPackage() == referringCls.getPackage()) return;
        throw new IllegalAccessError(Types.getInternalName(cls));
    }

    public static void checkPermission(Member member, Class<?> referringCls) {
        Class<?> cls = member.getDeclaringClass();
        switch (member.getModifiers() & (PUBLIC | PROTECTED | PRIVATE)) {
            case PUBLIC:
                return;
            case PROTECTED:
                if (cls.getPackage() == referringCls.getPackage() || cls.isAssignableFrom(referringCls)) return;
                else break;
            default:
                if (cls.getPackage() == referringCls.getPackage()) return;
                else break;
            case PRIVATE:
                if (cls == referringCls) return;
                else break;
        }
        throw new IllegalAccessError(Types.getInternalName(member));
    }

    public static void makeAccessible(AccessibleObject accessible) {
        try {
            accessible.setAccessible(true);
        } catch (SecurityException e) {
            // intentionally empty
        }
    }
}
