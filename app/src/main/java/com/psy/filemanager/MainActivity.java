package com.psy.filemanager;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.animation.AnimationUtils;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.psy.filemanager.adapters.filesAdapter;
import com.psy.filemanager.model.FStools;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

//    public View mCurrentViewMode;
    boolean mCopyOrCut;
    File[] selectedFilesArr;
    protected File mExternalRoot;
    File mInternalRoot;

    File mCurrentDir;
    public static ArrayList <File> mSelectedFiles;

    //Widgets & Adapters
    ArrayAdapter<File> mAdapter;
    View mCurView;
    GridView mGridView;
    ListView mListView;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    int mCurrentViewMode; // 0 grid/ 1 list
    private AdapterView.OnItemClickListener mClickListener;
    private AdapterView.OnItemLongClickListener mLongClickListener;
    TextView tvPath;

    FloatingActionButton mCopyBtn;
    FloatingActionButton mCutBtn;
    FloatingActionButton mDeleteBtn;
    FloatingActionButton mPasteBtn;
    FloatingActionButton mCancelBtn;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);


        mCopyBtn = findViewById(R.id.btnCopy);
        mCutBtn = findViewById(R.id.btnCut);
        mDeleteBtn = findViewById(R.id.btnDelete);
        mPasteBtn = findViewById(R.id.btnPaste);
        mCancelBtn = findViewById(R.id.btnCancel);

        mDrawerLayout = findViewById(R.id.drawer);
        mNavigationView = findViewById(R.id.navMenu);

        tvPath = findViewById(R.id.tvPath);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                TextView progress = view.findViewById(R.id.progressBar);
                TextView txtSpace = view.findViewById(R.id.tvHeaderFreeSpace);

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(calculateWidth(txtSpace), FrameLayout.LayoutParams.MATCH_PARENT);
                progress.setLayoutParams(lp);

                long used = Environment.getExternalStorageDirectory().getTotalSpace() - Environment.getExternalStorageDirectory().getFreeSpace();
                String msg = FStools.getSize(used) + " / " +
                       FStools.getSize(Environment.getExternalStorageDirectory().getTotalSpace() );

                txtSpace.setText(msg);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.menu_create_file:
                        createDialog(R.string.create_file);
                        break;
                    case R.id.menu_create_folder:
                        createDialog(R.string.create_folder);
                        break;
                }

                mDrawerLayout.closeDrawer(Gravity.START,true);

                return false;
            }
        });




//        if(Environment.getExternalStorageState().contentEquals(Environment.MEDIA_MOUNTED)&&
//        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
//        {
//
//        }


        mCurrentDir = Environment.getExternalStorageDirectory();
        mExternalRoot = Environment.getExternalStorageDirectory();

//        Toolbar toolbar = findViewById(R.id.toolBar);
////        toolbar.setNavigationIcon(R.drawable.mCancelBtn);
//        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.toolbar,null,false);
//        toolbar.addView(v);
//        toolbar.gr.
        final ImageView switchView = findViewById(R.id.switchView);
        switchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentViewMode == R.layout.grid_cell)
                {
                    mCurrentViewMode = R.layout.list_item;
                    switchView.setImageResource(R.drawable.grid);
                }
                else {
                    mCurrentViewMode = R.layout.grid_cell;
                    switchView.setImageResource(R.drawable.list);
                }
                restartAdapter();
            }
        });
        ImageView showMenu = findViewById(R.id.showMenu);
        showMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.START,true);
            }
        });
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        mCurrentViewMode = R.layout.list_item;
        mCurrentViewMode = R.layout.grid_cell;

//
        mAdapter = new filesAdapter(this,mCurrentViewMode,R.id.tvName, FStools.dir(mCurrentDir),mCurrentDir.getParentFile());



        mGridView = findViewById(R.id.gvFilesView);
        mListView = findViewById(R.id.lvFilesView);
        refresh();

//        mCurView.setAdapter(mAdapter);
        mClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File clickedFile =((ArrayAdapter<File>) parent.getAdapter()).getItem(position);
                // Selection mode - true / false
                if(mSelectedFiles==null||position==0) {
                    //todo if clicked root element

                    if (clickedFile.isDirectory()) {
                        if(Environment.getExternalStorageDirectory().getAbsolutePath().contentEquals(mCurrentDir.getAbsolutePath())&&position==0)
                        {
                            return;
                        }
                        Log.e("TAG", clickedFile.getAbsolutePath());
                        Log.e("TAG", mExternalRoot.getAbsolutePath());
                        mCurrentDir = mAdapter.getItem(position);

                        /*
                        mAdapter = new filesAdapter(MainActivity.this, mCurrentViewMode, R.id.tvName, FStools.dir(mCurrentDir), mCurrentDir.getParentFile());
                        getCurView().setAdapter(mAdapter);
                        tvPath.setText(mCurrentDir.getAbsolutePath());
                        */
                        restartAdapter();
                    }
                    else{
                        //todo add intent to open file
                        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String mimeType = mimeTypeMap.getMimeTypeFromExtension(FStools.getExtansion(clickedFile));
                        Log.d("MIME" , "Mime type : " +mimeType);
                        intent.setDataAndType(Uri.fromFile(clickedFile),mimeType);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try{
                            startActivity(intent);
                        }catch (ActivityNotFoundException ex)
                        {
                            Toast.makeText(MainActivity.this, R.string.handlerNotFound, Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
//                    if(position!=0) {//do not select parent
                    if (!FStools.isSelected(clickedFile)) { // add Selection
                        mSelectedFiles.add(clickedFile);
                    } else { // Remove selection
                        mSelectedFiles.remove(clickedFile);
                    }
                    ((ArrayAdapter<File>) parent.getAdapter()).notifyDataSetInvalidated();
//                    }
                }
//                ((TextView)findViewById(R.id.breadCrumbs)).setText(mCurrentDir.getAbsolutePath());

            }
        };

        mLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(position==0)
                {
                    //do not select parent
                    return true;
                }
                if(mSelectedFiles == null||mSelectedFiles.isEmpty()) { //First selection
                    mSelectedFiles = new ArrayList<>();
                    File selection = ((ArrayAdapter<File>) parent.getAdapter()).getItem(position);
                    mSelectedFiles.add(selection);

                }else{ //Clear selection
                    mSelectedFiles = null;
                }
                toggleFabMenu();
                ((ArrayAdapter<File>) parent.getAdapter()).notifyDataSetInvalidated();

                return true;
            }




        };

        mCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCopyOrCut = FStools.COPY;
                selectedFilesArr = new File[mSelectedFiles.size()];
                mSelectedFiles.toArray(selectedFilesArr);
                mSelectedFiles = null;
                showPaste();
            }
        });

        mCutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCopyOrCut = FStools.CUT;
                selectedFilesArr = new File[mSelectedFiles.size()];
                mSelectedFiles.toArray(selectedFilesArr);
                mSelectedFiles = null;
                showPaste();
            }
        });

        mPasteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FStools fStools = new FStools(MainActivity.this);
                fStools.copyOrMoveFile(mCurrentDir,mCopyOrCut,selectedFilesArr);
                toggleFabMenu();
                mSelectedFiles = null;
                selectedFilesArr = null;

                restartAdapter();
                Toast.makeText(MainActivity.this,((mCopyOrCut)?"Copied ": "Moved ") + fStools.getCnt() + " files", Toast.LENGTH_LONG).show();
                fStools= null;
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FStools fStools = new FStools(MainActivity.this);
                File[] files = new File[mSelectedFiles.size()];
                mSelectedFiles.toArray(files);
                mSelectedFiles=null;
                fStools.delete(files);
                restartAdapter();
                toggleFabMenu();
                Toast.makeText(MainActivity.this,"Deleted " + fStools.getCnt() + " files", Toast.LENGTH_LONG).show();
                fStools= null;
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedFiles = null;
                mAdapter.notifyDataSetInvalidated();
                toggleFabMenu();
            }
        });
        getCurView().setOnItemClickListener(mClickListener);

        //Toggle selection mode
        getCurView().setOnItemLongClickListener(mLongClickListener);

    }

    void refresh ()
    {
        mAdapter = new filesAdapter(MainActivity.this, mCurrentViewMode, R.id.tvName, FStools.dir(mCurrentDir), mCurrentDir.getParentFile());

        if(mCurrentViewMode == R.layout.grid_cell)
        {
            mListView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            mGridView.setAdapter(mAdapter);
        }
        else
        {
            mGridView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(mAdapter);
        }
        if(getCurView().getOnItemClickListener()==null)
        {
            getCurView().setOnItemClickListener(mClickListener);

            //Toggle selection mode
            getCurView().setOnItemLongClickListener(mLongClickListener);
        }
        tvPath.setText(getPath(mCurrentDir));

//        getCurView().setAdapter(mAdapter);
    }
    AbsListView getCurView(){
        if(mCurrentViewMode == R.layout.grid_cell)
        {
            return (GridView)mGridView;
        }
        else{
            return (ListView)mListView;
        }
    }
void restartAdapter(){

//    mAdapter = new filesAdapter(MainActivity.this, mCurrentViewMode, R.id.tvName, FStools.dir(mCurrentDir), mCurrentDir.getParentFile());
//    mCurView.setAdapter(mAdapter);
    refresh();
}


void createDialog(final int titleId){
    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    builder.setTitle(titleId);
    final EditText etName = new EditText(MainActivity.this);
    etName.setHint(R.string.hint_create);

    builder.setView(etName);
    builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            String name = etName.getText().toString().trim();
            //TODO add error msg
            switch (titleId){
                case R.string.create_folder:
                    if(FStools.mkDir(mCurrentDir,name)){
                        restartAdapter();
                    }
                    break;
                case R.string.create_file:
                    if(FStools.mkFile(mCurrentDir,name))
                    {
                        restartAdapter();
                    }
                    break;
            }


        }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    });
    builder.create().show();
}
    String getPath(File f){
        String path = f.getAbsolutePath();
        return cutPath(path);
    }
    String cutPath(String path){
        int len = 20;
        String divider = "/";
        if(path.length()>len){
            int cnt= 0;
            while (true){
                if(path.length() - path.indexOf(divider,cnt)<len){
                    path="..." + path.substring(path.indexOf(divider,cnt));
                    break;
                }
                else{
                    cnt = path.indexOf(divider,cnt+1);
                }
                if (cnt==-1)
                {
                    path = "..." + path.substring(path.length()-len);
                    break;
                }

            }/*

            if(path.indexOf(divider) != -1 && (path.length() - path.indexOf(divider))<len){
                path="..." + path.substring(path.indexOf(divider));
            }
            else{
                cutPath(path.substring(path.indexOf(divider)));
            }*/
        }
        return path;
    }

int calculateWidth(View view){
        int width = view.getWidth();
        long total = Environment.getExternalStorageDirectory().getTotalSpace();
        long free = Environment.getExternalStorageDirectory().getFreeSpace();
        long used = total-free;
        Log.e("TAAG" , "Total " + FStools.getSize(total) + " , Free " + FStools.getSize(free) + " width :" + width);
        Log.e("TAAG" , "Return " + (int)((width/100) * (100/(total/used))));

        return (int)((width/100) * (100/(total/used)));
}

    void toggleFabMenu(){

        if(mCancelBtn.isOrWillBeHidden()) {
            showBtns();
        }else{
            hideBtns();
        }
    }

    void hideBtns(){
        mCopyBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();
        mCutBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();
        mDeleteBtn.animate().translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();

        mCopyBtn.hide();
        mCutBtn.hide();
        mDeleteBtn.hide();
        mCancelBtn.hide();
        mPasteBtn.hide();
    }
    void showBtns(){
        mCopyBtn.show();
        mCutBtn.show();
        mDeleteBtn.show();
        mCancelBtn.show();
        mPasteBtn.hide();
        mCopyBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -80,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();
        mCutBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50,getResources().getDisplayMetrics()))
                .translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();
        mDeleteBtn.animate().translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -80,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();

    }
    void showPaste(){
        mCopyBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();
        mCutBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();
        mDeleteBtn.animate().translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();

        mCopyBtn.hide();
        mCutBtn.hide();
        mDeleteBtn.hide();

        mPasteBtn.show();
        mPasteBtn.animate().translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50,getResources().getDisplayMetrics()))
                .translationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50,getResources().getDisplayMetrics()))
                .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setDuration(400).start();

    }


}
