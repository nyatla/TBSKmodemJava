package jp.nyatla.kokolink.utils;



/**
 * 正規化された浮動小数点値と固定小数点値を相互変換します。
 */
public class FloatConverter
{
    public static double byteToDouble(byte b)
    {
        return (double)b / 255 - 0.5;
    }
    public static double int16ToDouble(int b)
    {
        if (b >= 0)
        {
            return ((double)b) / Short.MAX_VALUE;
        }
        else
        {
            return -(((double)b) / Short.MIN_VALUE);
        }
    }

    public static byte doubleToByte(double b)
    {
        return (byte)(b * 127 + 128);
    }
    public static short doubleToInt16(double b)
    {
        assert(1 >= b && b >= -1);
        if (b >= 0)
        {
            return (short)(Short.MAX_VALUE * b);
        }
        else
        {
            return (short)(-Short.MIN_VALUE * b);
        }

    }
}


