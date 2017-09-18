package kr.co.zesinc.blueworks.javaagent;

import kr.co.zesinc.blueworks.javaagent.asm.ScouterClassWriter;
import kr.co.zesinc.blueworks.javaagent.asm.probe.ServletServiceProbe;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2016. 9. 4.
 */
public class AgentTransformer implements ClassFileTransformer {
    public static ThreadLocal<ClassLoader> hookingCtx = new ThreadLocal<ClassLoader>();

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if(!"javax/servlet/http/HttpServlet".equals(className)) {
                return null;
            }

            hookingCtx.set(loader);

            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ScouterClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new ServletServiceProbe().transform(cw, className);

            cr.accept(cv, ClassReader.SKIP_FRAMES);

            System.out.println(className + "\t[" + loader + "]");

            return cw.toByteArray();

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            hookingCtx.set(null);
        }
        return null;
    }
}
