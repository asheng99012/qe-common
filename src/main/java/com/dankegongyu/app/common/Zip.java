package com.dankegongyu.app.common;

import java.io.*;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    ZipOutputStream zos;
    ByteArrayOutputStream baos;

    private Zip() {
    }

    public static Zip create() {
        Zip zip = new Zip();
        ZipOutputStream zos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        zos = new ZipOutputStream(baos);
        zip.zos = zos;
        zip.baos = baos;
        return zip;
    }

    public void addZipEntry(String zipEntryName, InputStream inputStream) throws IOException {
        zos.putNextEntry(new ZipEntry(zipEntryName));
        byte[] bufs = new byte[1024 * 10];
        int read = 0;
        while ((read = inputStream.read(bufs, 0, 1024 * 10)) != -1) {
            zos.write(bufs, 0, read);
        }
        zos.flush();
        zos.closeEntry();
    }

    public InputStream getInputStream() {
        try {
            this.zos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public String getMd5() {
        String tempPath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + ".zip";
        OutputStream os = null;
        try {
            os = new FileOutputStream(tempPath);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            InputStream ins = getInputStream();
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            File file = new File(tempPath);
            FileInputStream fis = new FileInputStream(file);
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
            fis.close();
            file.delete();
            return md5;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clese() {
        try {
            this.baos.close();
            this.zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
