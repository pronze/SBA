package io.github.pronze.sba;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.screamingsandals.lib.nms.accessors.*;
import org.screamingsandals.lib.utils.reflect.Reflect;

public class PacketTests {

    @BeforeEach
    public void compulsory() {
        Reflect.setField(AccessorUtils.class, "MAJOR_VERSION", 1);
        Reflect.setField(AccessorUtils.class, "MINOR_VERSION", 17);
        Reflect.setField(AccessorUtils.class, "PATCH_VERSION", 1);
        Reflect.setField(AccessorUtils.class, "craftBukkitImpl", "v_1_17_R1");
        Reflect.setField(AccessorUtils.class, "craftBukkitBased", true);
    }

    @SneakyThrows
    @Test
    public void test() {

    }


}
