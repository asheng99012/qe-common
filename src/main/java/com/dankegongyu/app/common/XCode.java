package com.dankegongyu.app.common;


import java.math.BigDecimal;

import com.google.common.base.Strings;

/**
 * Created by asheng on 2015/8/7 0007.
 */
public class XCode {
    private static String strBaseNum = "03726915840372691584";
    private static String strBase = "ncxgywpzvarsbdmkhfuqetncxgywpzvarsbdmkhfuqet";
    private static String strBaseN = "d36ch2tf7mvxsruwba8ypnegkq5d36ch2tf7mvxsruwba8ypnegkq5";
    private static double key = 2543.5415412812;
    private static int LEN = 8;
    private static int precision = 15;

    public static String encode(int _num) {
        return encode(_num, 0, LEN);
    }

    public static String encode(int _num, int type) {
        return encode(_num, type, LEN);
    }

    private static String getOther(long _num) {
        String other = new BigDecimal(_num / key).toPlainString().replace(".", "") + "000000000000";
        other = other.substring(0, precision);
        return other;
    }

    //type 0 数字，1 字母 ,2 数字字母组合
    public static String encode(long _num, int type, int len) {
        String sBase = strBaseNum;
        if (type == 1) sBase = strBase;
        if (type == 2) sBase = strBaseN;

        String other = getOther(_num);
        String num = String.valueOf(_num);
        int numLen = num.length();
        String last = num.substring(numLen - 1);
        String[] base = sBase.substring(Integer.parseInt(last)).split("");
        last = sBase.substring(Integer.parseInt(last), Integer.parseInt(last) + 1);
        String begin = sBase.substring(numLen, numLen + 1);
        String value = begin;
        String[] nums = num.split("");
        for (String s : nums) {
            value = value + base[Integer.parseInt(s)];
        }
        value = value + last;
        String[] others = other.substring(other.length() - len + numLen + 2).split("");

        for (String k : others) {
            value = value + base[Integer.parseInt(k)];
        }
        return value;
    }

    public static Long decode(String str) {
        return decode(str, 0, LEN);
    }

    public static Long decode(String str, int type) {
        return decode(str, type, LEN);
    }

    public static Long decode(String str, int type, int len) {
        if (Strings.isNullOrEmpty(str)) return null;
        String sBase = strBaseNum;
        if (type == 1) sBase = strBase;
        if (type == 2) sBase = strBaseN;
        int numLen = sBase.indexOf(str.substring(0, 1));
        String[] value = split(str.substring(1, numLen + 1));
        String last = str.substring(numLen + 1, numLen + 2);
        String base = sBase.substring(sBase.indexOf(last));
        String num = "";
        for (String k : value) {
            num = num + base.indexOf(k);
        }
        String other = getOther(Long.parseLong(num));
        String[] others = split(other.substring(other.length() - len + numLen + 2));
        String[] bases = split(base);
        String v = "";
        for (String k : others) {
            v = v + bases[Integer.parseInt(k)];
        }
        return Long.parseLong(num);
//        if (str.substring(numLen + 2).equals(v))
//            return Long.parseLong(num);
//        return null;
    }

    public static String encodeStr(String str) {
        int len = str.length();
        int charCode;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            charCode = Integer.valueOf(str.charAt(i));
            sb.append(XCode.encode(charCode, 1, 6));
        }
        return sb.toString();
    }

    public static String decodeStr(String ret) {
        int len = ret.length();
        int charCode;
        StringBuffer sb = new StringBuffer();
        String sub = "";

        for (int i = 0; i < len; i = i + 6) {
            sub = ret.substring(i, i + 6);
            charCode = Integer.valueOf(XCode.decode(sub, 1, 6).toString());
            sb.append(String.valueOf((char) charCode));
        }
        return sb.toString();
    }

    private static String[] split(String num) {
        return num.split("");
    }

    public static void main(String[] args) {
        int id = 123456;
        String ret = XCode.encode(id, 2, 15);
        Long id2 = XCode.decode("2tf7mv2ruutrtmm", 2, 15);
    }

}
