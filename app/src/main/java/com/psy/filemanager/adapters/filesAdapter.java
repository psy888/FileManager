package com.psy.filemanager.adapters;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.psy.filemanager.MainActivity;
import com.psy.filemanager.R;
import com.psy.filemanager.model.FStools;

import java.io.File;

public class filesAdapter extends ArrayAdapter<File> {
    Context mContext;
    File[] mFiles;
    File mParent;


    public filesAdapter(Context context, int resource, int textViewResourceId, File[]parentDir, File parent) {
        super(context, resource, textViewResourceId, parentDir);
        mContext = context;
        mFiles = parentDir;
        mParent = parent;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v =  super.getView(position, convertView, parent);
        File f = this.getItem(position);


        return setDataToView(v,f);
    }

    View setDataToView (View v, File f)
    {
        TextView tvName = v.findViewById(R.id.tvName);
        ImageView ivType = v.findViewById(R.id.ivType);
        TextView tvInnerItemCnt = v.findViewById(R.id.tvInnerItemCnt);
        TextView tvSize = v.findViewById(R.id.tvSize);
        TextView tvDate = v.findViewById(R.id.tvDate);

        //reset Visibility
        tvInnerItemCnt.setVisibility(View.VISIBLE);
        tvSize.setVisibility(View.VISIBLE);
        tvDate.setVisibility(View.VISIBLE);

        if(f.getAbsolutePath().contentEquals(mParent.getAbsolutePath()))
        {
//            Log.d("PATH", "Root: " + Environment.getExternalStorageDirectory().getParentFile().getAbsolutePath());
//            try {
//                Log.d("PATH", "file: " + f.getCanonicalPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Log.d("PATH", "parent: " + mParent.getAbsolutePath());
            if(Environment.getExternalStorageDirectory().getParentFile().getAbsolutePath().contentEquals(f.getAbsolutePath()))
            {
                ivType.setImageResource(R.drawable.mobile);
            }
            else
            {
                ivType.setImageResource(R.drawable.undo2);

            }
            tvName.setText(f.getName());
            tvInnerItemCnt.setVisibility(View.GONE);
            tvSize.setVisibility(View.GONE);
            tvDate.setVisibility(View.GONE);
//            ivType.setMaxWidth();
//            tvName.setText("  ...  ");
//            tvName.setGravity(Gravity.CENTER);

            return v;
        }

        //todo add drawables


        tvName.setText(f.getName());
        if(!f.isFile())
        {
            ivType.setImageResource(R.drawable.folder);
            tvSize.setVisibility(View.GONE);
            tvDate.setVisibility(View.GONE);
            //get inner items count and set it to TextView
            tvInnerItemCnt.setText(innerItemCnt(f));

        }
        else
        {
            ivType.setImageResource(getFileIcon(f));
            tvInnerItemCnt.setVisibility(View.GONE);

            tvSize.setText(FStools.getSize(f));
            tvDate.setText(FStools.getDate(f));
        }

        if(MainActivity.mSelectedFiles!=null&&FStools.isSelected(f)){
            v.setBackgroundColor(mContext.getResources().getColor(R.color.selectedColor));
        }
        else {v.setBackgroundColor(mContext.getResources().getColor(R.color.normalColor));}

        return v;
    }


    String innerItemCnt(File f){
        int itemsCnt = f.listFiles().length;
        String str = String.valueOf(itemsCnt);
        if(itemsCnt == 0)
        {
            str = mContext.getResources().getString(R.string.empty);
        }
        if(itemsCnt > 1 && itemsCnt < 5)
        {
            str += " " + mContext.getResources().getString(R.string.item234);
        }
        if(itemsCnt == 1)
        {
            str += " " + mContext.getResources().getString(R.string.item);
        }
        if(itemsCnt > 4) {
            str += " " + mContext.getResources().getString(R.string.items);
        }
        return str;
    }

    int getFileIcon(File fileName)
    {

        String ext = FStools.getExtension(fileName);
        if (ext != null) {

//            Log.e("EXT" , "Extensions :"+  ext);
            if (ext.contentEquals("jpg") ||
                    ext.contentEquals("png") ||
                    ext.contentEquals("jpeg") ||
                    ext.contentEquals("svg") ||
                    ext.contentEquals("ico")) {
                return R.drawable.file_picture;
            }
            if (ext.contentEquals("mp3") ||
                    ext.contentEquals("wav") ||
                    ext.contentEquals("flac") ||
                    ext.contentEquals("acc")) {
                return R.drawable.file_music;
            }
            if (ext.contentEquals("txt") ||
                    ext.contentEquals("info") ||
                    ext.contentEquals("readme")) {
                return R.drawable.file_text2;
            }
            if (ext.contentEquals("xls") ||
                    ext.contentEquals("xlsx") ||
                    ext.contentEquals("xlsm")) {
                return R.drawable.file_excel;
            }
            if (ext.contentEquals("doc") ||
                    ext.contentEquals("docx") ||
                    ext.contentEquals("rtf")) {
                return R.drawable.file_word;
            }
            if (ext.contentEquals("pdf")) {
                return R.drawable.file_pdf;
            }
            if (ext.contentEquals("zip") ||
                    ext.contentEquals("rar")) {
                return R.drawable.file_zip;
            }
            if (ext.contentEquals("mov") ||
                    ext.contentEquals("3gp") ||
                    ext.contentEquals("mp4") ||
                    ext.contentEquals("mkv") ||
                    ext.contentEquals("tc")) {
                return R.drawable.file_video;
            }
        }

        return R.drawable.file_empty;
    }

//    String getExtension(String name){
//        return name.substring(name.lastIndexOf(".")+1);
//    }
}
