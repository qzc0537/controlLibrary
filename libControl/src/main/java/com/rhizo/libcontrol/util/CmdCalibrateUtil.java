package com.rhizo.libcontrol.util;

import com.rhizo.libcontrol.util.BytesUtil;

/**
 * 指令校准工具
 *
 * @author Dawn
 */
public class CmdCalibrateUtil {
    private static final byte[] FE01 = new byte[]{-2, 1};
    private static final byte[] FE00 = new byte[]{-2, 0};
    private static final byte[] FF = new byte[]{-1};
    private static final byte[] FE = new byte[]{-2};


    /**
     * 接收调用
     * 校准数据 FE01 -> FF
     * 校准数据 FE00 -> FE
     * 注意先后循序，很重要
     *
     * @param command 注意要无FFFF FF
     * @author Dawn
     * @since 2021/11/23 11:03
     */
    public static byte[] calibrate(byte[] command) {
        return doCalibrate(command, FE01, FF, FE00, FE);
    }

    /**
     * 发送调用
     * 反校准数据 FE -> FE00
     * 反校准数据 FF -> FE01
     * 注意先后循序，很重要
     *
     * @param afterCrcCommand 注意要无FFFF FF 可以包含crc
     * @author Dawn
     * @since 2021/11/24 11:22
     */
    public static byte[] reverseCalibrate(byte[] afterCrcCommand) {
        return doCalibrate(afterCrcCommand, FE, FE00, FF, FE01);
    }

    /**
     * 执行校准
     *
     * @param src          原始数据
     * @param target1      目标一 eq.FE01
     * @param replacement1 转换符号一 eq.FF
     * @param target2      目标二 eq.FE00
     * @param replacement2 转换符号二 eq.FE
     * @return 转换后的数据
     */
    private static byte[] doCalibrate(byte[] src, byte[] target1, byte[] replacement1, byte[] target2, byte[] replacement2) {
        if (!com.rhizo.libcontrol.util.BytesUtil.contains(src, target1) && !com.rhizo.libcontrol.util.BytesUtil.contains(src, target2)) {
            return src;
        } else {
            //第一次校准
            byte[] next = algorithm(src, target1, replacement1);
            //第二次校准
            return algorithm(next, target2, replacement2);
        }
    }

    /**
     * 校准算法
     */
    private static byte[] algorithm(byte[] src, byte[] target, byte[] replacement) {
        int index;
        byte[] save = new byte[0];
        byte[] temp;
        while (true) {
            index = com.rhizo.libcontrol.util.BytesUtil.indexOf(src, target);
            if (index == -1) {
                save = com.rhizo.libcontrol.util.BytesUtil.mergeBytes(save, src);
                break;
            }
            temp = com.rhizo.libcontrol.util.BytesUtil.subBytes(src, 0, index);
            save = com.rhizo.libcontrol.util.BytesUtil.mergeBytes(save, temp, replacement);
            src = BytesUtil.subBytes(src, index + target.length);
        }
        return save;
    }


}
