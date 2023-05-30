package com.tapon.storageandroid11;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.tapon.storageandroid11.databinding.ActivityStorageFrameworkBinding;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static com.tapon.storageandroid11.Utils.REQ_DOCUMENT_FILE_CODE;
import static com.tapon.storageandroid11.Utils.REQ_DOCUMENT_TREE_CODE;

public class SAFrameworkActivity extends AppCompatActivity implements DocumentFileAdapter.Callback {

    DocumentFile openFile;

    private ActivityStorageFrameworkBinding binding;
    private DocumentFileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityStorageFrameworkBinding.inflate(this.getLayoutInflater());
        this.setContentView(this.binding.getRoot());

        /*Uri.Builder datnt = new  Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(PATH_DOCUMENT)
                .appendPath(parentDocumentId)
                .appendPath(PATH_CHILDREN)
                .build();

        new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(PATH_DOCUMENT)
                .appendPath(documentId)
                .build();*/


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            File dataDir = this.getDataDir();
            Log.d("datnt", "--------- " + dataDir.getAbsolutePath());
            for (File file : Objects.requireNonNull(dataDir.listFiles())) {
                Log.d("datnt", "--------- " + file.getAbsolutePath());
            }
        }

        StringBuilder volume = new StringBuilder();

        for (File externalCacheDir : this.getExternalFilesDirs(null)) {
            Log.d("datnt", "--------- " + externalCacheDir.getAbsolutePath());
            volume.append(" | ").append(externalCacheDir.getAbsolutePath());
            for (File file : Objects.requireNonNull(externalCacheDir.listFiles())) {
                Log.d("datnt", "--------- " + file.getAbsolutePath());
            }
        }

        this.binding.tvVolume.setText(volume.toString());

        this.adapter = new DocumentFileAdapter(this);
        this.adapter.setCallback(this);
        this.binding.rvFolder.setLayoutManager(new LinearLayoutManager(this));
        this.binding.rvFolder.setAdapter(this.adapter);

        this.binding.tvOpenDir.setOnClickListener(v -> this.openDirectory());

        this.binding.tvOpenFile.setOnClickListener(v -> this.openDocumentFile());
        this.binding.tvActionPick.setOnClickListener(v -> this.actionPick());
        this.binding.tvGetContent.setOnClickListener(v -> this.actionGetContent());
        this.binding.tvCreateFile.setOnClickListener(v -> this.createFile());

        this.binding.ivDelete.setOnClickListener(v -> {
            if (this.openFile == null) {
                return;
            }

            boolean delete = this.openFile.delete();
            Toast.makeText(this, "Deleted: " + delete, Toast.LENGTH_SHORT).show();
        });

        this.binding.ivShare.setOnClickListener(v -> {
            if (this.openFile == null) {
                return;
            }

            this.onClickShare(this.openFile);
        });

        this.binding.ivRename.setOnClickListener(v -> {
            if (this.openFile == null) {
                return;
            }

            this.onClickEditItem(this.openFile);
        });


        this.binding.ivOpen.setOnClickListener(v -> {
            if (this.openFile == null) {
                return;
            }

            this.onClickItem(this.openFile);
        });
    }

    private void openDirectory() {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.

        this.startActivityForResult(intent, REQ_DOCUMENT_TREE_CODE);
    }

    private void openDocumentFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        this.startActivityForResult(intent, REQ_DOCUMENT_FILE_CODE);
    }

    private void actionPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        this.startActivityForResult(intent, REQ_DOCUMENT_FILE_CODE);
    }

    private void actionGetContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        this.startActivityForResult(intent, REQ_DOCUMENT_FILE_CODE);
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("*/*");
        this.startActivityForResult(intent, REQ_DOCUMENT_FILE_CODE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DOCUMENT_TREE_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri uri = data.getData();
            if (uri == null) {
                return;
            }

            DocumentFile directory = DocumentFile.fromTreeUri(this, uri);
            if (directory == null) {
                return;
            }

            this.adapter.setListItem(Arrays.asList(directory.listFiles()));

        }

        if (requestCode == REQ_DOCUMENT_FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri uri = data.getData();
            if (uri == null) {
                return;
            }

            this.binding.tvUri.setText(uri.toString());
            this.openFile = DocumentFile.fromSingleUri(this, uri);
            if (this.openFile == null) {
                return;
            }

            String name = "Name: " + this.openFile.getName();
            String size = "Size: " + Utils.convertBytes(this.openFile.length());
            String mimetype = "Mime: " + App.getAppProvider().getType(uri);
            String modifiedDate = "Modifi: " + Utils.formatDate(this.openFile.lastModified());
            /*String type = "Type: " + this.openFile.getType(); tra ve mime cua file*/

            this.binding.tvInfo.setText(name + " | " + size + " | " + mimetype + " | " + modifiedDate);
            this.binding.tvAccess.setText("CanRead: " + this.openFile.canRead() + " | " + "CanWrite: " + this.openFile.canWrite());

            Glide.with(this).load(uri).into(this.binding.ivPreview);

        }
    }

    @Override
    public void onClickItem(DocumentFile item) {
        Utils.openNomarlFile(this, item.getUri());
    }

    @Override
    public void onClickEditItem(DocumentFile item) {
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

                    try {
                        item.renameTo(text.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onClickShare(DocumentFile item) {
        Utils.sendfile(this, item.getUri());
    }

    @Override
    public void onClickDeleteItem(DocumentFile item) {
        System.out.println(item.delete());
        this.adapter.notifyDataSetChanged();
    }
}