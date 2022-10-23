package com.cleanroommc.bogosorter.core;

import com.cleanroommc.bogosorter.core.visitor.EntityPlayerVisitor;
import com.cleanroommc.bogosorter.core.visitor.PIMVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class BogoSorterTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] classBytes) {
        switch (name) {
            case PIMVisitor.CLASS_NAME: {
                ClassWriter classWriter = new ClassWriter(0);
                new ClassReader(classBytes).accept(new PIMVisitor(classWriter), 0);
                return classWriter.toByteArray();
            }
            case EntityPlayerVisitor.CLASS_NAME: {
                ClassWriter classWriter = new ClassWriter(0);
                new ClassReader(classBytes).accept(new EntityPlayerVisitor(classWriter), 0);
                return classWriter.toByteArray();
            }
        }
        return classBytes;
    }
}
