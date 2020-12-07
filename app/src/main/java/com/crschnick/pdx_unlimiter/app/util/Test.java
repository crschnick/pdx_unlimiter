package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static jdk.incubator.foreign.CLinker.*;

public class Test {

    public static void test() {
        var lib =
                LibraryLookup.ofPath(PdxuInstallation.getInstance().getSavegameLocation().getParent().resolve("rakaly.dll"));;

        MethodHandle r = CLinker.getInstance().downcallHandle(
                lib.lookup("rakaly_eu4_melt").get(),
                MethodType.methodType(long.class, MemoryAddress.class, long.class),
                FunctionDescriptor.of(C_LONG_LONG, C_POINTER, C_LONG_LONG)
        );

        MethodHandle rakaly_melt_error_code = CLinker.getInstance().downcallHandle(
                lib.lookup("rakaly_melt_error_code").get(),
                MethodType.methodType(int.class, long.class),
                FunctionDescriptor.of(C_INT, C_LONG_LONG)
        );

        MethodHandle rakaly_melt_data_length = CLinker.getInstance().downcallHandle(
                lib.lookup("rakaly_melt_data_length").get(),
                MethodType.methodType(long.class, long.class),
                FunctionDescriptor.of(C_LONG_LONG, C_LONG_LONG)
        );

        try {
            long handle = (long) r.invokeExact(CLinker.toCString("a=2").address(), 5L);
            int result = (int) rakaly_melt_error_code.invokeExact(handle);
            long length = (long) rakaly_melt_data_length.invoke(handle);
            int b = 0;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return;
    }
}
