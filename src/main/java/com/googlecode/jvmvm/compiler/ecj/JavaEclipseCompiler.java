package com.googlecode.jvmvm.compiler.ecj;

import com.googlecode.jvmvm.compiler.JarUtil;
import com.googlecode.jvmvm.loader.ProjectCompilerException;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Experimental. Compiles try/finally statements to JSR/RET so asm cannot modify compiled classes.
 */
public class JavaEclipseCompiler implements com.googlecode.jvmvm.compiler.Compiler {

    @Override
    public Map<String, byte[]> compile(Map<String, String> files, List<String> systemClasses, List<byte[]> jars) throws ProjectCompilerException {
        final Map<String, byte[]> res = new HashMap<String, byte[]>();
        final Map<String, byte[]> classpath = new HashMap<String, byte[]>();

        for (byte[] jar : jars) {
            for (Map.Entry<String, byte[]> e : JarUtil.unpack(jar).entrySet()) {
                classpath.put(e.getKey(), e.getValue());
            }
        }
        for (String systemClass : systemClasses) {
            String path = systemClass.replace(".", "/") + ".class";
            try {
                byte[] b = IOUtils.toByteArray(Class.forName(systemClass).getClassLoader().getResourceAsStream(path));
                classpath.put(path, b);
            } catch (Exception e) {
                try {
                    byte[] b = IOUtils.toByteArray(ClassLoader.getSystemClassLoader().getResourceAsStream(path));
                    classpath.put(path, b);
                } catch (Exception e1) {
                    // don't add
                }
            }
        }

        INameEnvironment env = new NameEnv(files, classpath);
        ICompilerRequestor requestor = new ICompilerRequestor() {
            @Override
            public void acceptResult(CompilationResult result) {
                if (result.hasErrors()) {
                    StringBuilder sb = new StringBuilder();
                    for (CategorizedProblem problem : result.getErrors()) {
                        sb.append(problem.toString()).append("\n");
                    }
                    throw new ProjectCompilerException("compile error", sb.toString());
                }
                for (ClassFile cf : result.getClassFiles()) {
                    StringBuilder sb = new StringBuilder();
                    for (char[] p : cf.getCompoundName()) {
                        sb.append(new String(p));
                        sb.append(".");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    res.put(sb.toString(), cf.getBytes());
                }
            }
        };

        CompilerOptions opt = new CompilerOptions();
        opt.sourceLevel = ClassFileConstants.JDK1_6;
        opt.produceDebugAttributes = ClassFileConstants.ATTR_SOURCE | ClassFileConstants.ATTR_LINES;
        opt.complianceLevel = ClassFileConstants.JDK1_6;
        opt.targetJDK = ClassFileConstants.JDK1_6;
        //opt.suppressOptionalErrors = true;
        opt.treatOptionalErrorAsFatal = false;
        Compiler compiler = new Compiler(env, DefaultErrorHandlingPolicies.exitAfterAllProblems(),
                opt, requestor, new DefaultProblemFactory());

        List<ICompilationUnit> cu = new ArrayList<ICompilationUnit>();
        for (Map.Entry<String, String> e : files.entrySet()) {
            cu.add(new CompilationUnit(e.getValue().toCharArray(), e.getKey(), null));
        }

        compiler.compile(cu.toArray(new ICompilationUnit[cu.size()]));
        return res;
    }
}