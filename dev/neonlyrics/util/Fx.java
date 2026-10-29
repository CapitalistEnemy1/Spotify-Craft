/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1074
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 */
package dev.neonlyrics.util;

import dev.neonlyrics.NeonLyrics;
import net.minecraft.class_1074;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class Fx {
    private Fx() {
    }

    public static String t(String key, Object ... args) {
        return class_1074.method_4662((String)key, (Object[])args);
    }

    public static void sound(Sfx s) {
        if (!NeonLyrics.cfg().sounds) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.method_1483() == null) {
            return;
        }
        switch (s.ordinal()) {
            case 0: {
                mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)((class_3414)class_3417.field_15015.comp_349()), (float)1.4f, (float)0.35f));
                break;
            }
            case 1: {
                mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)class_3417.field_14709, (float)1.0f, (float)0.6f));
                break;
            }
            case 2: {
                mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)((class_3414)class_3417.field_14624.comp_349()), (float)0.6f, (float)0.7f));
                break;
            }
            case 3: {
                mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)((class_3414)class_3417.field_15204.comp_349()), (float)1.8f, (float)0.4f));
            }
        }
    }

    public static void info(String text) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1705 == null) {
            return;
        }
        mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)"[SpotifyCraft] ").method_27692(class_124.field_1075).method_10852((class_2561)class_2561.method_43470((String)text).method_27692(class_124.field_1068)));
    }

    public static void error(String text) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1705 == null) {
            return;
        }
        mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)"[SpotifyCraft] ").method_27692(class_124.field_1076).method_10852((class_2561)class_2561.method_43470((String)text).method_27692(class_124.field_1061)));
        Fx.sound(Sfx.ERROR);
    }

    public static void clipboard(String s) {
        class_310.method_1551().field_1774.method_1455(s);
    }

    public static String myName() {
        return class_310.method_1551().method_1548().method_1676();
    }

    public static enum Sfx {
        CLICK,
        WIN,
        ERROR,
        TICK;

    }
}

