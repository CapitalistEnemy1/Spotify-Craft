/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package dev.neonlyrics;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import net.minecraft.client.Minecraft;

public final class License {
    private static final String PUBLIC_KEY = "MCowBQYDK2VwAyEAZaOcfcCWGx2W1XbJQHiYcWkIYjkcpsZY1C+xOupbBAk=";
    private static final Set<String> OWNERS = Set.of("7335a129e0834053a118108585f76e34");
    private static final Set<String> REDEEM = Set.of("40cf30c56999738c1abcf25e21bb5e0399927bee0ab899d5103c01de5fb46ad2", "111b3a5e98d18f406562bacc8925717f7ee424297ee620c074cfd854f8aecbb5", "6f349596fef93c9aea6cf5f55c0ee7fcf590d9a083a1afc84f8a69be03327bbb", "672e9c0f0c336d3650ed4983a2bee402c3e6c7914dec5f193969f27dbe4a6ff2", "ae7dec6aa5880b203a8c68080fef23819a5be4c798d8dbf0a84242a1474af845", "ec9d42d29b02e8d00047d9e8776e347b37ef10a7bffaad9d58b9728eecc2d654", "2a400cec1160ce0ed4ed07f6c4e8b935baf16b24917062832e33f08a6f4c8611", "f02283a96cda1b77ed5b0d3087ee53f9c8b04847ab42cf5ac1a907cde8cbd895", "6cd977f63ad8105885a8609b58e5bd65698040ead292ab1e9a463c1fe44e1060", "5e2daf6a924f2d8a31ea4c9cee8be47800596290e19124eab3978b70f9c2f841", "20f27596004e394d920023ee6464355f0115dcf7e38f66ab29b91e9f5a8d9437", "dae519afc1fc08b494e084c190a4539364d22765170930a431a4d099d775932b", "9483963445bd96e5696475809b8c5ba39d604e22d99aa5085b9b08e1da8e917d", "2a78fd70f914cb339f03d07cd95c70b8e0fea73691d9cf1183cd717e965773fe", "5b7c0cf96388643fd4ccaf1676a6084cd26002ab7a3758c743021f527f138345", "af5079a04fa65524a8c315d10430d985115ddbce1797f422d4a75e7d6ff345ce", "69000e89193eeed295dc934821e90dcde0c13636b2c50e3f202e4edb439af1f0", "1a7d4dbf7f91cec03a7150c4eacc61f6e637e06696c510b333e56973dbdd7d16", "a7aa47414ccb661670ff80c2283e25f742cadb6d892f26cf4f74945ec9a6a6a9", "3771d0d6567f041357bb000e1da2fd563852f6425894e085cbcd33ae8b9eaa01", "c295c3fbb40ecb4a51dbb37934fdd7369c1995e6e5740c0f378e3a5ccf92f473", "7f104ac53384eab641b8c243c52b125f319de3f9f06852966f6998acc23adcbe", "f03c09d2ca1fadee24f8bc5617f2dacbfd8d697bd94630a2b9c1a7a74f0d730c", "a03edd9be34b5f8674304529c8d4617279cac537bee3bc9df4f2c13a2e72361f", "7c6b0ca4dce6472c83dd50c8e2fd1fbd6891ce14366642556e576809afdd5070", "b088a79bd08d293fc8a04a25c107fd42e067e2ce00bff76e6fd44454e2458de4", "ef1a0c8f50f14c17743a4db7892b45bf0d365ce20f2918d4b357ee86bc6969af", "f9ce130765535f5950be2bc2a8b962b55bc3a44e75f8adb35bb519321f2c6f66", "37a6fcd84c852c1307ab232de3868bbd7891e34b57e5ff702aae9672f5e2dcc9", "51fbc7addeda64f15bfb1148f42f11988f28ad7c0ab8ac7d74f418d7df692470", "60c6ea9c5c7bd5d082c154f03745dcc36b18d54d426af458bf542a8d0321eaed", "813e0a23528df4daa281c260f36abf8a725f0a0635a5fc693291d066de5fd087", "1872d1a805eb47665e6a630c75a4076a18b8268e98a8a6027ebc753e6bff2535", "8a84be64facd0f2eaea623d4c965abe806e523a4e29e1b4226708c7fefda8730", "866952d434d14f43981a81ea251b1beb1e916a2bf7d85b54c66ca51566fdda26", "7ead495c624fa8fea3c916856aee0de144b8a359c173fa4349e1ae6c2b2332a3", "e50da7086270c578d14929e300d21088a47161e689b9ad03f830358fe0b2ef15", "eeefe857468ede6b5c8a9539a2d12867f92700e14073c794f9adde5410c3e887", "d476a1e2e9f67fcd24942552d7c62109ab1d3cc039bf722e8c6faf9888f47788", "2da61cca1021274a9a3fdcc2cc48d60105b5f785af225e800b80617c570f989d");
    private static String checkedFor;
    private static String checkedCode;
    private static boolean valid;

    private License() {
    }

    public static boolean ok() {
        return true;
    }

    public static String uuid() {
        try {
            return Minecraft.getInstance().getUser().getProfileId().toString().replace("-", "").toLowerCase();
        }
        catch (Throwable t) {
            return "";
        }
    }

    public static String name() {
        try {
            return Minecraft.getInstance().getUser().getName();
        }
        catch (Throwable t) {
            return "?";
        }
    }

    public static String normalize(String code) {
        String c;
        String string = c = code == null ? "" : code.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (c.startsWith("LCORD")) {
            c = c.substring(5);
        }
        if (c.length() != 12) {
            return c;
        }
        return "LCORD-" + c.substring(0, 4) + "-" + c.substring(4, 8) + "-" + c.substring(8, 12);
    }

    public static boolean isRedeemCode(String code) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(("LyricsCord|" + License.normalize(code)).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) {
                sb.append(String.format("%02x", b));
            }
            return REDEEM.contains(sb.toString());
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean check(String string, String string2) {
        return true;
    }
}

