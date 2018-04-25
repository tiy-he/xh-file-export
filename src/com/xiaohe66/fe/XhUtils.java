package com.xiaohe66.fe;


import java.awt.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xh
 * @date 18-01-31 031
 */
public class XhUtils {

    /**
     * 复制文件，从sourcePath复制到targetPath
     * @param sourcePath    源路径
     * @param targetPath    目标路径
     */
    public static void copy(String sourcePath,String targetPath){
        System.out.println(sourcePath);
        System.out.println(targetPath);

        if(!(new File(sourcePath)).exists()) {
            System.out.println("待复制的文件不存在");
            return;
        }

        File targetFile = new File(targetPath);

        //目标文件存在时，删除目标文件，为重新复制作准备
        if (targetFile.exists()) {
            targetFile.delete();
        }

        if(targetPath.endsWith(File.separator)) {
            return;
        }

        if(!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
            System.out.println("创建目标文件所在目录失败！");
            return;
        }

        try {
            if(!targetFile.createNewFile()) {
                System.out.println("创建单个文件" + targetPath + "失败！");
                return;
            }
        } catch (IOException var10) {
            var10.printStackTrace();
            System.out.println("创建单个文件" + targetPath + "失败！" + var10.getMessage());
            return;
        }

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        BufferedInputStream bufferedInput = null;
        BufferedOutputStream bufferedOutput = null;
        try{
            inputStream = new FileInputStream(sourcePath);
            outputStream = new FileOutputStream(targetFile);
            bufferedInput = new BufferedInputStream(inputStream);
            bufferedOutput = new BufferedOutputStream(outputStream);
            byte[] buff = new byte[1024];
            int size;
            while((size = bufferedInput.read(buff, 0, buff.length)) != -1) {
                bufferedOutput.write(buff, 0, size);
            }
            bufferedOutput.flush();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            closeIo(bufferedInput,bufferedOutput,outputStream,inputStream);
        }
    }

    public static void closeIo(Closeable... closeables){
        if(closeables == null){
            return;
        }
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除一个文件/文件夹（支持不为空的文件夹的删除）
     * @param file file
     */
    public static void deleteFile(File file){
        if(!file.exists()){
            System.out.println("文件/文件夹不存在");
            return;
        }
        //文件夹
        if(file.isDirectory()){
            //文件夹下所有文件
            File[] sonFiles = file.listFiles();
            if (sonFiles != null) {
                for (File sonFile : sonFiles) {
                    deleteFile(sonFile);
                }
            }

        }
        file.delete();
    }

    public static void openDirectory(String path){
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentDateStr(){
        return new SimpleDateFormat("yyyy.MM.dd").format(new Date());
    }
}
