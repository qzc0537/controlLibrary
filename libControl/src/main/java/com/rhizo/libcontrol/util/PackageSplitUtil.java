package com.rhizo.libcontrol.util;


public class PackageSplitUtil {

    private static final byte[] FFFF = new byte[]{-1, -1};
    private static final byte[] FF = new byte[]{-1};

    /**
     * 完全字节的切割方法
     */
    public static PackageSplitterDto doSplit(byte[] source) {
        return doSplit(source, 0);
    }

    /**
     * 完全字节的切割方法
     *
     * @param from 偏移位置
     */
    public static PackageSplitterDto doSplit(byte[] source, int from) {

        int startIndex = com.rhizo.libcontrol.util.BytesUtil.indexOf(source, FFFF, from);
        if (startIndex == -1) {
            return null;
        }
        int endIndex = com.rhizo.libcontrol.util.BytesUtil.indexOf(source, FF, startIndex + FFFF.length);
        if (endIndex == -1) {
            return null;
        }
        //六个FFFFFF相连的情况下，抛弃前面的FF
        if (endIndex - startIndex - FFFF.length == 0) {
            return doSplit(source, startIndex + 1);
        }
        //endIndex要加1是因为要把结束符的FF也加进来
        int offset = endIndex + 1;
        PackageSplitterDto back = new PackageSplitterDto();
        back.setOut(com.rhizo.libcontrol.util.BytesUtil.subBytes(source, startIndex, offset));
        back.setNext(BytesUtil.subBytes(source, offset));
        return back;
    }
}
