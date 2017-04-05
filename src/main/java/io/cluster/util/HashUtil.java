/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.codec.binary.Hex;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.cluster.http.controller.NodeController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author trieu
 *
 * Hash function util
 *
 */
public class HashUtil {

    private static final Logger LOGGER = LogManager.getLogger(HashUtil.class.getName());

    /**
     * hash url to long number <br>
     *
     * MurmurHash3 is the successor to MurmurHash2. <br>
     * It comes in 3 variants - a 32-bit version that targets low latency for
     * hash table use and two 128-bit versions for generating unique identifiers
     * for large blocks of data, one each for x86 and x64 platforms.
     *
     * @param url
     * @return long number
     */
    public static void main(String[] args) {
//        String test = "    /**\n" +
//"     * hash url to long number <br>\n" +
//"     *\n" +
//"     * MurmurHash3 is the successor to MurmurHash2. <br>\n" +
//"     * It comes in 3 variants - a 32-bit version that targets low latency for\n" +
//"     * hash table use and two 128-bit versions for generating unique identifiers\n" +
//"     * for large blocks of data, one each for x86 and x64 platforms.\n" +
//"     *\n" +
//"     * @param url\n" +
//"     * @return long number\n" +
//"     */\n" +
//"    ";
//        long s = System.currentTimeMillis();
//        long hashUrlCrc64 = hashUrlCrc64(test);
//        System.out.println("64 bit requires " + (System.currentTimeMillis() - s));
//        s = System.currentTimeMillis();
//        long hashUrl128Bit = hashUrl128Bit(test);
//        System.out.println("128 bit requires " + (System.currentTimeMillis() - s));
//        System.out.println("64 bit " + hashUrlCrc64);
//        System.out.println("128 bit " + hashUrl128Bit);
        System.out.println(hashUrlCrc64("https://www.youtube.com/watch?v=C76bqVhjxXw") / 1000);
    }

    public static long hashUrl128Bit(String url) {
        HashFunction hf = Hashing.murmur3_128();
        HashCode hc = hf.newHasher().putString(url, Charset.forName(StringPool.UTF_8)).hash();
        return hc.asLong();
    }

    public static long hashUrlCrc64(final String url) {
        return CRC64.hashByAlgo2(url.getBytes());
    }

    public static String crc32(String s) {
        try {
            CRC32 crc32 = new CRC32();
            crc32.update(s.getBytes());
            byte[] bytesOfMessage = s.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            return new String(Hex.encodeHex(thedigest));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long crc32Number(String s) {
        try {
            // Convert string to bytes
            byte bytes[] = s.getBytes();
            Checksum checksum = new CRC32();
            checksum.update(bytes, 0, bytes.length);

            /*
             * Get the generated checksum using getValue method of CRC32 class.
             */
            long lngChecksum = checksum.getValue();
            return lngChecksum;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String md5(Object... args) {
        StringBuilder s = new StringBuilder();
        for (Object arg : args) {
            s.append(arg);
        }
        return md5(s.toString());
    }

    public static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());

            byte byteData[] = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }

            // convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringPool.BLANK;
    }

    public static String sha1(String s) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(s.getBytes());
            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            String hashedSha1 = formatter.toString();
            formatter.close();
            return hashedSha1;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return StringPool.BLANK;
    }

    public static long parseIP2Long(String ip) {
        long num = 0;
        try {
            String[] addrArray = ip.split("\\.");
            for (int i = 0; i < addrArray.length; i++) {
                int power = 3 - i;
                num += ((Integer.parseInt(addrArray[i]) % 256) * Math.pow(256, power));
            }
        } catch (Exception ex) {
            LOGGER.error("HashUtil.parseIP2Long error parse", ex);
        }
        return num;
    }

    public static String parseIP2String(long ip) {
        StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                sb.insert(0, '.');
            }
            ip >>= 8;
        }
        return sb.toString();
    }

}
