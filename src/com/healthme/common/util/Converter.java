
package com.healthme.common.util;


/**
 * <p>Title: Converter</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broadbus Technologies, Inc.</p>
 * @author not attributable
 * @version 1.0
 */

import java.math.BigInteger;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * The <code>Converter</code> class provides utilities to convert a String
 * formatted IP address to Dotted-Decimal form and vice versa.
 *
 * Also provides conversion of MAC address to a byte array.
 */

public class Converter
{
    private static Logger log = Logger.getLogger(Converter.class);

    private Converter()
    {
    }

    public static byte[] toByteArray(Integer value)
    {
        return BigInteger.valueOf(value.intValue()).toByteArray();
    }

    public static byte[] to4ByteArray(int value)
    {
        byte[] ret = new byte[4];
        byte[] arr = toByteArray(value);
        int x = arr.length;
        for(int i=0;i<4;i++)
        {
            if(i < x)
                ret[i] = arr[i];
            else
                ret[i] = 0x00;
        }
        return ret;
    }

    public static byte[] toByteArray(int value)
    {
        return BigInteger.valueOf(value).toByteArray();
    }

    public static Integer fromByteArray(byte[] array)
    {
        return new BigInteger(array).intValue();
    }

    // Alex: added to support IDs as Long 
    public static Long longFromByteArray(byte[] array)
    {
	return new BigInteger(array).longValue();
    }

    public static byte[] longToByteArray(long value)
    {
	return BigInteger.valueOf(value).toByteArray();
    }

    public static String intToDottedDecimal(String ipAddr)
    {
        String dottedDecimal = "0.0.0.0";

        try
        {
            int value = Integer.parseInt(ipAddr);
            dottedDecimal = ipAddrToDottedDecimal(value);
        }
        catch (NumberFormatException nfe)
        {
        }

        return dottedDecimal;
    }

    public static String ipAddrToDottedDecimal(long ipAddr)
    {
        short[] addr = new short[4];

        addr[0] = (short) ((ipAddr >>> 24) & 0xFF);
        addr[1] = (short) ((ipAddr >>> 16) & 0xFF);
        addr[2] = (short) ((ipAddr >>> 8) & 0xFF);
        addr[3] = (short) (ipAddr & 0xFF);

        String dottedString = new String(addr[0] + "." + addr[1] + "."
                + addr[2] + "." + addr[3]);
        return dottedString;
    }

    public static int dottedDecimalToInt(String dottedString)
    {
        int i = 0;
        int addr[] = new int[4];

        for (StringTokenizer st = new StringTokenizer(dottedString, "."); st
                .hasMoreTokens()
                && i < addr.length; i++)
        {
            addr[i] = Integer.parseInt(st.nextToken());
        }

        addr[0] = addr[0] << 24;
        addr[1] = addr[1] << 16;
        addr[2] = addr[2] << 8;
        addr[3] = addr[3];

        return addr[0] + addr[1] + addr[2] + addr[3];
    }

    public static int toUnsignedInt(byte b)
    {
        if (b < 0)
            return (int) (256 + b);
        else
            return b;
    }

    public static String toHex(int i)
    {
        String hex = (Integer.toHexString(i)).toUpperCase();
        if (i < 16)
            hex = "0" + hex;
        return hex;
    }

    public static String toHex(byte b)
    {
        String hex = (Integer.toHexString(toUnsignedInt(b))).toUpperCase();
        if (toUnsignedInt(b) < 16)
            hex = "0" + hex;
        return hex;
    }

    /**
     * Converts MAC address string to a byte array of size 6.
     *
     * @param macAddrStr MAC address string
     * @return six bytes array
     * @throws IllegalArgumentException
     */
    public static byte[] macAddressStrTo6ByteArray(String macAddrStr) { // VS051105
        final int MAC_ADDR_LENGTH = 6;
        final int MAC_ADDR_RADIX = 16;
        byte[] byteArray = new byte[6];

        if (macAddrStr != null) {
            StringTokenizer tk = new StringTokenizer(macAddrStr, "-");
            if (tk.countTokens() != MAC_ADDR_LENGTH) {
                String msg = "Wrong format of MAC address - less than 6 tokens in " + macAddrStr;
                throw new IllegalArgumentException(msg);
            }
            try {
                // we cannot work on byte level, because bytes are signed and MAX byte value is 0x7F.
                // See com.broadbus.common.util.ConverterTest for proof that this will work.
                byteArray[0] = (byte) Integer.parseInt(tk.nextToken(), MAC_ADDR_RADIX);
                byteArray[1] = (byte) Integer.parseInt(tk.nextToken(), MAC_ADDR_RADIX);
                byteArray[2] = (byte) Integer.parseInt(tk.nextToken(), MAC_ADDR_RADIX);
                byteArray[3] = (byte) Integer.parseInt(tk.nextToken(), MAC_ADDR_RADIX);
                byteArray[4] = (byte) Integer.parseInt(tk.nextToken(), MAC_ADDR_RADIX);
                byteArray[5] = (byte) Integer.parseInt(tk.nextToken(), MAC_ADDR_RADIX);
            } catch (java.lang.NumberFormatException ex) {
                String msg = "Wrong format of MAC address " + macAddrStr;
                throw new IllegalArgumentException(msg);
            }
        } else {
            String msg = "MAC address string cannot be null";
            throw new IllegalArgumentException(msg);
        }

        return byteArray;
    }

      private static int OFFSET_2 = 65536;
      private static int MAXINT_2 = 32767;


     public static long  unsignedToInteger( long value)
     {
       long ret = 0;
       if (value < 0 || value >= OFFSET_2) {
         throw new IllegalArgumentException("Conversion Overflow");
       }
       return ret = (value <= MAXINT_2)? value:(value - OFFSET_2);
     }

     public static long integerToUnsigned(int value)
     {
       long ret = 0;
       return ret = (value < 0)?(value + OFFSET_2) : value;
    }
     
     public static int byteToInt(byte[] b) {
         int s = 0;
         int s0 = b[0] & 0xff;
         int s1 = b[1] & 0xff;
         int s2 = b[2] & 0xff;
         int s3 = b[3] & 0xff;
         s3 <<= 24;
         s2 <<= 16;
         s1 <<= 8;
         s = s0 | s1 | s2 | s3;
          return s;
     }

}
