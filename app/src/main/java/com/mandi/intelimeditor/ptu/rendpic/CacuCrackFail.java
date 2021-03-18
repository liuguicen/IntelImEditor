package com.mandi.intelimeditor.ptu.rendpic;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/29
 *      version : 1.0
 * <pre>
 */
public class CacuCrackFail extends Exception {
    public static int failNumber = 0;
    public CacuCrackFail(String msg) {
        super(msg);
        failNumber++;
    }
}
