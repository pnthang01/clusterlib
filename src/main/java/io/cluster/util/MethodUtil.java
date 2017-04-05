/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class MethodUtil {

    private static final Logger LOGGER = LogManager.getLogger(MethodUtil.class.getName());

    public static final int BITSET_SIZE = 400000000;

    private static final Gson gson = new Gson();

    private static final Type MAP_JSON_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static Map<String, String> fromJsonToMap(String json) {
        return gson.fromJson(json, MAP_JSON_TYPE);
    }

    public static <V> V fromJson(String json, Class<V> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static String toJson(Object o, Type type) {
        return gson.toJson(o, type);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static <V> V fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public static List<Long> parseRawToLongList(List<Object> list) {
        List<Long> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(StringUtil.safeParseLong(o));
        }
        return resultList;
    }

    public static List<Double> parseRawToDoubleList(List<Object> list) {
        List<Double> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(StringUtil.safeParseDouble(o.toString()));
        }
        return resultList;
    }

    public static List<String> parseRawToStringList(List<Object> list) {
        List<String> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(o.toString());
        }
        return resultList;
    }

    public static List<Integer> parseRawToIntegerList(List<Object> list) {
        List<Integer> resultList = new ArrayList();
        for (Object o : list) {
            resultList.add(StringUtil.safeParseInt(o));
        }
        return resultList;
    }

    public static Set<String> writeBitSetsToTempFiles(Map<String, BitSet> bitsetMap, String outputFolder) {
        Set<String> result = new HashSet();
        BufferedOutputStream bos = null;
        OutputStream out = null;
        try {
            for (Map.Entry<String, BitSet> entry : bitsetMap.entrySet()) {
                File outputFile = new File(outputFolder + entry.getKey());
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                out = Files.newOutputStream(Paths.get(URI.create("file:" + outputFile.getAbsolutePath())),
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                bos = new BufferedOutputStream(out);
                bos.write(entry.getValue().toByteArray());
                out.close();
                bos.close();
                result.add(outputFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            LOGGER.error("Error happened when call writeBitSetsToTempFiles with error: ", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ex) {
                }
            }
        }
        return result;
    }

    public static Map<String, BitSet> readBitSetFromTempFiles(Collection<String> tempFiles) {
        InputStream fis = null;
        try {
            //File pattern: rmk:{conversionId}:{remarketingId}:{startIndex}:{endIndex}
            Map<String, BitSet> result = new HashMap();
            //Read
            byte[] bytes = new byte[2048];
            int le = 0;
            for (String fileName : tempFiles) {
                File file = new File(fileName);
                if (!file.exists()) {
                    continue;
                }
                fis = Files.newInputStream(Paths.get(URI.create("file:" + fileName)), StandardOpenOption.READ);
                ByteBuffer allocate = ByteBuffer.allocate(BITSET_SIZE);
                while ((le = fis.read(bytes)) != -1) {
                    allocate.put(bytes, 0, le);
                }
                allocate.flip();
                BitSet bitsetRead = BitSet.valueOf(allocate);
                result.put(file.getName(), bitsetRead);
                fis.close();
            }
            return result;
        } catch (Exception ex) {
            LOGGER.error("Error happended when readBitSetFromTempFiles with error: ", ex);
            return null;
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static Set<String> unzipFileToTempFiles(String fileName, String dest) {
        byte[] buffer = new byte[2048];
        InputStream inp = null;
        OutputStream out = null;
        ZipInputStream zis = null;
        Set<String> result = new HashSet();
        try {
            File outputFolder = new File(dest);
            if (!outputFolder.exists()) {
                outputFolder.mkdir();
            }
            //get the zip file content
            inp = Files.newInputStream(Paths.get(URI.create("file:" + fileName)), StandardOpenOption.READ);
            zis = new ZipInputStream(inp);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fullFileName = outputFolder.getAbsolutePath() + File.separator + ze.getName();
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                out = Files.newOutputStream(Paths.get(URI.create("file:" + fullFileName)),
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                int len;
                while ((len = zis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                result.add(fullFileName);
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (java.io.FileNotFoundException ex) {
            LOGGER.error("Error happended when unzipFileToTempFiles with error: ", ex);
        } catch (Exception ex) {
            LOGGER.error("Error happended when unzipFileToTempFiles with error: ", ex);
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
            if (null != inp) {
                try {
                    inp.close();
                } catch (Exception ex) {
                }
            }
            if (zis != null) {
                try {
                    zis.closeEntry();
                    zis.close();
                } catch (IOException ex) { //No need to catch exception
                }
            }
        }
        return result;
    }

    public static boolean zipTempFilesToFile(Collection<String> fileNames, String fileDesName) {
        byte[] buffer = new byte[2048];
        OutputStream out = null;
        ZipOutputStream zos = null;
        InputStream fis = null;
        boolean result = false;
        try {
            File fileDes = new File(fileDesName);
            if (!fileDes.getParentFile().exists()) {
                fileDes.getParentFile().mkdirs();
            }
            out = Files.newOutputStream(Paths.get(URI.create("file:" + fileDesName)), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            zos = new ZipOutputStream(out);
            int len = 0;
            for (String fileName : fileNames) {
                File file = new File(fileName);
                if (!file.exists()) {
                    continue;
                }
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);
                fis = Files.newInputStream(Paths.get(URI.create("file:" + fileName)), StandardOpenOption.READ);
                while ((len = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
                fis.close();
            }
            zos.closeEntry();
            result = true;
        } catch (Exception ex) {
            LOGGER.error("Error happended when zipTempFilesToFile with error: ", ex);
            result = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException ex) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }
        return result;
    }

    public static boolean deleteTempFiles(Set<String> fileList) {
        boolean result = false;
        try {
            for (String fileName : fileList) {
                File file = new File(fileName);
                if (!file.exists()) {
                    continue;
                }
                file.delete();
            }
            result = true;
        } catch (Exception ex) {
            LOGGER.error("Error happended when deleteTempFiles with error: ", ex);
            result = false;
        }
        return result;
    }
}
