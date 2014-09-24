import interpretable.Parity;
import net.sf.jauvm.vm.GlobalCodeCache;
import net.sf.jauvm.vm.VirtualMachine;
import net.sf.jauvm.vm.VirtualMachineException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BasicTest {

    @Test
    public void test_vm_execute() throws Throwable {
        GlobalCodeCache.setCodeLoader(new GlobalCodeCache.CodeLoader() {
            @Override
            public InputStream getBytecodeStream(Class<?> cls) {
                if (cls.getCanonicalName() != null) {
                    if (cls.getCanonicalName().startsWith("java.")) {
                        return null;
                    }
                    if (cls.getCanonicalName().startsWith("javax.")) {
                        return null;
                    }
                    if (cls.getCanonicalName().startsWith("com.sun.")) {
                        return null;
                    }
                }
                return super.getBytecodeStream(cls);
            }

            @Override
            public boolean checkAccess(Class cls) {
                if (cls.equals(Class.class)) {
                    return false;
                }
                return true;
            }
        });

        VirtualMachine vm = VirtualMachine.create(new Runnable() {
            @Override
            public void run() {
                Parity.main();
            }
        });

        StringBuilder b = new StringBuilder();
        while (vm.isActive()) {
            vm.step();
            StackTraceElement pointer = vm.getPointer();
            String size;
            try {
                ByteArrayOutputStream state = new ByteArrayOutputStream();
                vm.save(state);
                size = "" + state.toByteArray().length;
            } catch (VirtualMachineException e) {
                size = e.getMessage();
            }

            Assert.assertTrue("on step " + vm.getStepNumber(),
                    (pointer == null && !vm.isActive()) ||
                            (
                                    (
                                            size.contains("Instance of illegal class [java.io.PrintStream]") ||
                                            size.contains("Instance of illegal class [BasicTest$2]") ||
                                            size.matches("[0-9]{4}")
                                    ) && pointer.getFileName().matches("(BasicTest|Parity)\\.java")
                            )
            );
        }
    }
}
