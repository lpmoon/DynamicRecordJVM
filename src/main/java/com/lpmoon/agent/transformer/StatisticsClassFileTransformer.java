package com.lpmoon.agent.transformer;

import com.lpmoon.agent.reporter.Summary;
import com.lpmoon.agent.util.OldClassHolder;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by zblacker on 2017/5/9.
 */
public class StatisticsClassFileTransformer implements ClassFileTransformer {

    private OldClassHolder oldClassHolder;

    public StatisticsClassFileTransformer(OldClassHolder oldClassHolder) {
        this.oldClassHolder = oldClassHolder;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        oldClassHolder.storeClass(className, classfileBuffer);

        System.out.println("before transform " + className + " bytecode length is " + byteCode.length);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.get(className);
            java.lang.System.out.println(className);
            CtMethod[] methods = cc.getDeclaredMethods();
            for (CtMethod m : methods) {
                if (m != null) {
                    m.addLocalVariable("elapsedTime", CtClass.longType);
                    m.insertBefore("elapsedTime = System.currentTimeMillis();");
                    m.insertAfter("{elapsedTime = System.currentTimeMillis() - elapsedTime;"
                        + "((StatisticCommand)CommandManager.getCommand(\"statistics\")).getSummary().report(\"" + className + "\", \"" + m.getName() + "\", elapsedTime);}");
                    byteCode = cc.toBytecode();
                    cc.detach();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("after transform " + className + " bytecode length is " + byteCode.length);
        return byteCode;
    }
}
