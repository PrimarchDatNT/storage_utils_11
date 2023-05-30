package com.tapon.storageandroid11;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.tapon.storageandroid11.Utils.REQ_DOCUMENT_FILE_CODE;
import static com.tapon.storageandroid11.Utils.REQ_DOCUMENT_TREE_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tapon.storageandroid11.databinding.ActivityFileManagerBinding;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class FileManagerActivity extends AppCompatActivity implements ManagerFileAdapter.Callback {

    private static final int REQ_FILE_MANAGER_ACCESS_CODE = 1593;
    private static final int REQ_STORAGE_PERMISSION_CODE = 1489;

    private final String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private List<File> listItem;
    private List<String> folderPaths;
    private ManagerFileAdapter mAdapter;
    private CompositeDisposable disposable;
    private ActivityFileManagerBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBinding = ActivityFileManagerBinding.inflate(this.getLayoutInflater());
        this.setContentView(this.mBinding.getRoot());

        /*File externalFilesDir = this.getExternalFilesDir(null);
        File file = new File(externalFilesDir, System.currentTimeMillis()+"_text.txt");
        try {
            System.out.println(file.createNewFile());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(this.getPackageName().getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            this.reqStoreMananger();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.isNotStoragePmsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, REQ_STORAGE_PERMISSION_CODE);
                return;
            }
        }

        this.init();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void reqStoreMananger() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        this.startActivityForResult(intent, REQ_FILE_MANAGER_ACCESS_CODE);
    }

    private void init() {
        /*try {
            FileOutputStream fileOutputStream = this.openFileOutput("img.png", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        this.initView();
        this.initData();
        this.initTool();
    }

    private void initView() {
        this.mBinding.tvFolder.setText(ROOT);
        this.mAdapter = new ManagerFileAdapter(this);
        this.mAdapter.setCallback(this);
        this.mBinding.rvFolder.setLayoutManager(new LinearLayoutManager(this));
        this.mBinding.rvFolder.setAdapter(this.mAdapter);
    }

    private void initData() {
        this.listItem = new ArrayList<>();
        this.folderPaths = new ArrayList<>();
        this.folderPaths.add(ROOT);
        this.openFolder(ROOT);
    }

    private void initTool() {
        this.mBinding.ivCreateFolder.setOnClickListener(v -> {
            String currentPath = this.getCurrentPath();
            File folder = new File(currentPath + File.separator + "new_folder_" + System.currentTimeMillis());
            if (!folder.exists()) {
                System.out.println(folder.mkdirs());
            }

            this.openFolder(currentPath);
        });

        this.mBinding.ivCreateFile.setOnClickListener(v -> {
            String currentPath = this.getCurrentPath();
            File file = new File(currentPath + File.separator + "new_file_test" + System.currentTimeMillis() + ".txt");

            try {
                FileUtils.writeStringToFile(file, this.getPackageName() + "\n" + "Write string " + System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.openFolder(currentPath);
        });
    }

    private String getCurrentPath() {
        if (this.folderPaths == null || this.folderPaths.isEmpty()) {
            this.folderPaths = new ArrayList<>();
            this.folderPaths.add(ROOT);
            this.openFolder(ROOT);
            return ROOT;
        }
        return this.folderPaths.get(this.folderPaths.size() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE_MANAGER_ACCESS_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                this.init();
            }
        }

        if (requestCode == REQ_DOCUMENT_TREE_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            DocumentFile file = DocumentFile.fromTreeUri(this, uri);
            if (file == null) {
                return;
            }
            if (file.delete()) {
                this.openFolder(this.getCurrentPath());
            }
        }

        if (requestCode == REQ_DOCUMENT_FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri uri = data.getData();
            if (uri == null) {
                return;
            }

            DocumentFile file = DocumentFile.fromSingleUri(this, uri);
            if (file == null) {
                return;
            }

            if (file.delete()) {
                this.openFolder(this.getCurrentPath());
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE_PERMISSION_CODE) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                    if (permission.equals(WRITE_EXTERNAL_STORAGE)) {
                        this.initView();
                        this.initData();
                        break;
                    }
                }
            }
        }
    }

    private void openFolder(String folder) {
        Single.create((SingleOnSubscribe<List<File>>) emitter -> {

            File folderDir = new File(folder);
            if (!folderDir.exists() || folderDir.length() == 0) {
                emitter.onError(new NullPointerException());
                return;
            }

            File[] childs = folderDir.listFiles();
            if (childs == null || childs.length == 0) {
                emitter.onError(new NullPointerException());
                return;
            }

            List<File> listItem = new ArrayList<>();
            for (File file : childs) {
                if (file.exists()) {
                    File item = new File(file.getPath());
                    listItem.add(item);
                }
            }

            if (listItem.isEmpty()) {
                emitter.onSuccess(listItem);
                return;
            }

            Collections.sort(listItem, Utils.SORT_BY_NAME);
            Collections.sort(listItem, (f1, f2) -> {
                if (f1.isDirectory() && f2.isDirectory()) {
                    return 0;
                } else if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else {
                    return 1;
                }
            });

            emitter.onSuccess(listItem);

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<File>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        subcribeTask(d);
                    }

                    @Override
                    public void onSuccess(@NonNull List<File> items) {
                        showItems(items);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        showItems(new ArrayList<>());
                    }
                });
    }

    private void showItems(List<File> items) {
        this.listItem = new ArrayList<>(items);
        this.mAdapter.setListItem(this.listItem);
    }

    @Override
    public void onBackPressed() {
        if (this.folderPaths == null || this.folderPaths.isEmpty() || this.folderPaths.size() == 1) {
            super.onBackPressed();
            return;
        }

        String posPath = this.folderPaths.get(folderPaths.size() - 2);
        int currentPostion = this.folderPaths.size() - 1;
        this.mBinding.tvFolder.setText(posPath);
        this.folderPaths.remove(currentPostion);
        this.openFolder(posPath);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClickItem(@NotNull File item) {
        if (item.isDirectory()) {
            this.openFolder(item.getAbsolutePath());
            this.folderPaths.add(item.getPath());
            this.mBinding.tvFolder.setText(item.getAbsolutePath());
        } else {

            Intent intent = new Intent(this, DocumentViewerActivity.class);
            intent.putExtra(DocumentViewerActivity.EXTRA_DATA, item.getAbsolutePath());
            this.startActivity(intent);
/*            DialogPreviewBinding dialogBinding = DialogPreviewBinding.inflate(this.getLayoutInflater());

            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(dialogBinding.getRoot());
            dialog.setCancelable(true);

            Glide.with(this)
                    .load(item.getAbsoluteFile())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.drawable.ic_sharp_image_24)
                    .into(dialogBinding.ivPreview);

            dialogBinding.tvFileName.setText("Name: " + item.getName());
            dialogBinding.tvFileSize.setText("Size: " + Formatter.formatFileSize(this, item.length()));
            dialogBinding.tvFileModify.setText("Last modify: " + Utils.formatDate(item.lastModified()));
            dialogBinding.tvFilePath.setText("Path: " + item.getPath());
            dialogBinding.btOpen.setOnClickListener(v -> {
                dialog.dismiss();
                Utils.openNomarlFile(this, Utils.createUri(this, item.getAbsolutePath()));
            });

            dialog.create();

            Window window = dialog.getWindow();
            if (window != null) {
                dialog.show();
                window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
            }*/

        }
    }

    @Override
    public void onClickEditItem(File item) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Edit")
                .setMessage("Input new name")
                .setView(input)
                .setPositiveButton("Ok", (dialog, whichButton) -> {
                    Editable text = input.getText();
                    if (TextUtils.isEmpty(text)) {
                        dialog.dismiss();
                        return;
                    }

                    String currentName = item.getName();
                    String pathname = item.getAbsolutePath().replaceAll(currentName, text.toString());
                    File tempt = new File(pathname);

                    if (tempt.exists()) {
                        Toast.makeText(this, "File exists!!!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    boolean b = item.renameTo(new File(pathname));
                    System.out.println(b);
                    this.openFolder(this.mBinding.tvFolder.getText().toString());
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onClickShare(@NotNull File item) {
        Utils.sendfile(this, Utils.createUri(this, item.getAbsolutePath()));
    }

    @Override
    public void onClickDeleteItem(@NotNull File item) {
        if (item.canWrite()) {
            try {
                if (item.isDirectory()) {
                    FileUtils.deleteDirectory(item);
                } else {
                    FileUtils.forceDelete(item);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.openFolder(this.getCurrentPath());
            return;
        }

        if (item.isDirectory()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            this.startActivityForResult(intent, REQ_DOCUMENT_TREE_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            this.startActivityForResult(intent, REQ_DOCUMENT_FILE_CODE);
        }
    }

    private boolean isNotStoragePmsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void subcribeTask(Disposable disposable) {
        if (this.disposable == null) {
            this.disposable = new CompositeDisposable();
        }
        this.disposable.add(disposable);
    }

}