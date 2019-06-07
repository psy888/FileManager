package com.psy.filemanager.model;

import android.content.Context;
import android.util.Log;

import com.psy.filemanager.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

public class FStools {

    String TAG = this.getClass().getSimpleName();

    public static final boolean COPY = true;
    public static final boolean CUT = false;


    public static final int BUFFER_LENGTH = 1024;

    private Context mContext;

    int mCnt = 0;

    public FStools(Context context)
    {
        mContext = context;
    }

    /**
     * get dir content list (sorted)
     * @param f
     * @return
     */
    public static File[] dir(File f)
    {

        File[] list = null;
        if(f.isDirectory())
        {
            list = f.listFiles();
            Arrays.sort(list, mComparator); //dirs first!!!
        }
        if(f.getParentFile()!=null) {
            File[] list1 = new File[list.length + 1];
            for (int i = 0; i < list1.length; i++) {
                if (i == 0) {
                    list1[i] = f.getParentFile();
                } else {
                    list1[i] = list[i - 1];
                }
            }
            return list1;
        }

        return list;
    }

    /**
     * get String simplified file size
     * @param f - file
     * @return Ex " 2.4 Gb "
     */
    public static String getSize(File f)
    {
        if (f.isFile()) {
            long size = f.length();

            if ((size/(1024 * 1024 * 1024)) > 0) { // Gb
                return DecimalFormat.getInstance().format(size / (1024 * 1024 * 1024)) + " Gb";
            }
            if (size / (1024 * 1024) > 0) {
                return DecimalFormat.getInstance().format(size / (1024 * 1024)) + " Mb"; // Mb
            }
            if (size / 1024 > 0) {
                return DecimalFormat.getInstance().format(size / (1024)) + " kB";// kb
            }
            if (size / 1024 == 0) {
                return size + " b";// b
            }
        }
        return "";
    }
 /**
     * get String simplified file size
     * @param size - file size
     * @return Ex " 2.4 Gb "
     */
    public static String getSize(long size)
    {


            if ((size/(1024 * 1024 * 1024)) > 0) { // Gb
                return DecimalFormat.getInstance().format(size / (1024 * 1024 * 1024)) + " Gb";
            }
            if (size / (1024 * 1024) > 0) {
                return DecimalFormat.getInstance().format(size / (1024 * 1024)) + " Mb"; // Mb
            }
            if (size / 1024 > 0) {
                return DecimalFormat.getInstance().format(size / (1024)) + " kB";// kb
            }
            if (size / 1024 == 0) {
                return size + " b";// b
            }

        return "";
    }

    /**
     * Get last modified date as string
     * @param f
     * @return "01/01/1999 11:02"
     */
    public static String  getDate(File f)
    {
        if (f.isFile())
        {
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(f.lastModified());

            Calendar currentDate = Calendar.getInstance();
            currentDate.setTimeInMillis(System.currentTimeMillis());

            if(lastModified.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR))
            {
                return (lastModified.get(Calendar.MONTH)+1) + "/" + lastModified.get(Calendar.YEAR);
            }
            if(lastModified.get(Calendar.MONTH) != currentDate.get(Calendar.MONTH))
            {
                return (lastModified.get(Calendar.MONTH)+1) + "/" + lastModified.get(Calendar.YEAR);
//                return lastModified.get(Calendar.DAY_OF_MONTH) + "/" + (lastModified.get(Calendar.MONTH)+1);
            }
            if(lastModified.get(Calendar.DAY_OF_MONTH) != currentDate.get(Calendar.DAY_OF_MONTH))
            {
                return lastModified.get(Calendar.DAY_OF_MONTH) + "/" + (lastModified.get(Calendar.MONTH)+1);
            }




            return  lastModified.get(Calendar.HOUR_OF_DAY) + ":" +
                    lastModified.get(Calendar.MINUTE);
        }
        return "";
    }

    public static String getInnerItemsCnt(File f)
    {

        return String.valueOf(f.listFiles().length);
    }


    /**
     * Comparator Dirs First!
     *   return :
     *   (Dir)  -  (File)  -> 1
     *   (File)  -  (Dir)  -> -1
     *   (Dir)  -  (Dir)  -> 0
     *   (File)  -  (File)  -> 0
     */
    static Comparator<File> mComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            if(o1.isFile()&&!o2.isFile())
            {
                return 1;
            }
            if(!o1.isFile()&&o2.isFile())
            {
                return -1;
            }
            if((!o1.isFile()&&!o2.isFile())||(o1.isFile()&&o2.isFile()))
            {
                return 0;
            }
            return 0;
        }
    };
    /**
     *  Copy or Move selected Files;
     * @param selectedFiles - File[] (selected Files)
     * @param destination - where to paste
     * @param CopyORCut - operation flag (true - copy, false cut)
     * @return number of copied/moved files, -1 - error
     */
    public void copyOrMoveFile (File destination, boolean CopyORCut,File... selectedFiles)
    {
        //todo dialog fragment with progress bar

        if(destination.isFile())
        {
            throw new IllegalArgumentException("Destination is not Directory : " + destination.getAbsolutePath());
        }
        for (File f: selectedFiles) {
            copy(f,destination);
            /*
            if(!f.isFile())
            {
                File[] subDirList= f.listFiles();
                copyOrMoveFile(new File(destination,), CopyORCut,subDirList);
            }
            */

        }
        if(CopyORCut == CUT)
        {
            delete(selectedFiles);
        }

    }


    /**
     * Copy file/directory
     * @param src - source File
     * @param dst - destination Directory
     * @return - boolean success
     */
    private boolean copy(File src, File dst){
        String srcName = src.getName();
        File output = new File(dst, srcName);
        if(output.exists()) // if output file exists add "_copy" to destination path
        {
            if(srcName.lastIndexOf(".")!=-1)
            {
                srcName = srcName.subSequence(0,srcName.lastIndexOf(".")) + "_copy" + srcName.subSequence(srcName.lastIndexOf("."), srcName.length());
            }else {
                srcName += "_copy";
            }
            output = new File(dst, srcName);
        }
        if(src.isFile()) {
            try {
//                if(output.createNewFile()) // output file created
//                {
//                    FileInputStream FIS = mContext.openFileInput(src.getPath());
                    FileInputStream FIS = new FileInputStream(src);
//                    FileOutputStream FOS = mContext.openFileOutput(output.getAbsolutePath(), mContext.MODE_PRIVATE);
                    FileOutputStream FOS = new FileOutputStream(output);
                    byte[] buf = new byte[BUFFER_LENGTH];
                    do {
                        int cnt = FIS.read(buf, 0, buf.length);
                        if (cnt == -1) {
                            break;
                        }
                        FOS.write(buf, 0, buf.length);
                    } while (FIS.available() > 0);

                    FIS.close();
                    FOS.close();
                    mCnt++;
                    return true;
             /*   }
                else
                {
                    Log.e(TAG, "Error file " + output.getAbsolutePath() + " not created");
                    return false;
                }*/
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }else{ // if src file is directory
            if(output.mkdir())
            {
                for(File f:src.listFiles())
                {
                    copy(f,output);
                }
//                mCnt++;
                return true; // dir created
            }
        }
        return false;
    }

    /**
     * Delete files and folders
     * @param files - selected files
     */
    public void delete(File... files)
    {
        for(File f: files)
        {
            if(!f.isFile())
            {
                delete(f.listFiles());
            }else{
                mCnt++;
            }
            f.delete();
        }
    }

    /**
     * get counter end reset
     * @return int count of files (copied/deleted/moved)
     */
    public int getCnt(){
        int tmp = mCnt;
        mCnt = 0;// reset counter
        return tmp;
    }

    public static boolean isSelected(File f){
        for (int i = 0; i < MainActivity.mSelectedFiles.size(); i++) {
            if(MainActivity.mSelectedFiles.get(i).equals(f)){return true;}
        }
        return false;
    }


    public static boolean mkDir(File dst, String dirName)
    {
        return new File(dst, dirName).mkdir();
    }

    public static boolean mkFile(File dst, String fileName){
        try {
            return new File(dst, fileName).createNewFile();
        }catch (IOException e)
        {
            return false;
        }
    }

    public static String getExtansion(File f){
        String name = f.getName();
        if(name.lastIndexOf(".") == -1){return null;}

        String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
        return ext.toLowerCase();
    }


}
