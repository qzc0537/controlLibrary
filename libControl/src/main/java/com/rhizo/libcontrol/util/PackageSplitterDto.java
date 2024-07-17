package com.rhizo.libcontrol.util;


/**
 * 拆包DTO
 */
public class PackageSplitterDto {
    /**
     * 返回的字节数组
     */
    private byte[] out;
    /**
     * 剩下的字节数组
     */
    private byte[] next;

    public byte[] getOut() {
        return out;
    }

    public void setOut(byte[] out) {
        this.out = out;
    }

    public byte[] getNext() {
        return next;
    }

    public void setNext(byte[] next) {
        this.next = next;
    }
}
