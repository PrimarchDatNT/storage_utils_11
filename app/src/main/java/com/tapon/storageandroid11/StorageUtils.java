package com.tapon.storageandroid11;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import androidx.documentfile.provider.DocumentFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


@TargetApi(Build.VERSION_CODES.Q)
public class StorageUtils {

    public static final int REQ_URI_PMS_CODE = 1001;
    public static final int REQ_DOCUMENT_TREE_CODE = 1368;

    public static final String FILE_MIME = "file/*";
    public static final String IMAGE_MIME = "image/*";
    public static final String VIDEO_MIME = "video/*";
    public static final String AUDIO_MIME = "audio/*";

    public static final String DEFAULT_ROOT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();

    /**
     * @param activity start system file manager activity
     *                 and handle result uri of permited direcoty
     */
    public static void requestDirectoryPermission(Activity activity) {
        StorageManager storageManager = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume storageVolume = storageManager.getStorageVolume(Environment.getExternalStorageDirectory());

        Intent intent = storageVolume.createOpenDocumentTreeIntent();

        try {
            activity.startActivityForResult(intent, StorageUtils.REQ_DOCUMENT_TREE_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param activity start system file manager activity and handle
     *                 result uri of permited files
     * @param mimes    filter file by mimes for request
     */
    public static void requestFilePermissionUris(Activity activity, String[] mimes) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (mimes != null) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimes);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "Select file"), StorageUtils.REQ_URI_PMS_CODE);
    }

    /**
     * @param data intent data result in request files permission
     * @return list granted permssion uri of file
     */
    public static List<Uri> getAccessPermitedUris(Intent data) {
        if (data == null) {
            return null;
        }

        List<Uri> uris = new ArrayList<>();

        if (data.getData() == null) {
            ClipData clipData = data.getClipData();
            if (clipData == null) {
                return null;
            }

            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item itemAt = clipData.getItemAt(i);
                Uri orgUri = itemAt.getUri();
                uris.add(orgUri);
            }
            return uris;
        } else {
            uris.add(data.getData());
        }

        return uris;
    }

    /**
     *
     * @param context for get app content resolver
     * @param src source file
     * @param dst uri of result file
     * @throws IOException open stream may have IOExeceptioon
     */
    public static void writeFromFile(@NotNull Context context, File src, @NotNull Uri dst) throws IOException {
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        OutputStream outputStream = contentResolver.openOutputStream(dst);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = bis.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        bis.close();
        bos.flush();
        bos.close();
    }

    /**
     *
     * @param context for get app content resolver
     * @param src uri of source file
     * @param dst uri of result file
     * @throws IOException open stream may have IOExeceptioon
     */
    public static void writeFileFromUri(@NotNull Context context, Uri src, @NotNull DocumentFile dst) throws IOException {
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        OutputStream outputStream = contentResolver.openOutputStream(dst.getUri());
        BufferedInputStream bis = new BufferedInputStream(contentResolver.openInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = bis.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        bis.close();
        bos.flush();
        bos.close();
    }

    /**
     *
     * @param activity activity for create DocumentFile and take permistion for directory
     * @param data result of request direcoty permistion
     * @return DocumentFile of granted directory
     */
    public static @Nullable DocumentFile requestDirectoryUriPemissionResult(Activity activity, Intent data) {
        if (data == null) {
            return null;
        }

        Uri uriDir = data.getData();
        if (uriDir == null) {
            return null;
        }

        takeUriPermssion(activity, uriDir);

        return DocumentFile.fromTreeUri(activity, uriDir);
    }

    public static @Nullable Uri getAvailabeAccessDirectoryUri(@NotNull Context context, String path) {
        List<UriPermission> uriPermissions = context.getApplicationContext().getContentResolver().getPersistedUriPermissions();
        if (uriPermissions == null || uriPermissions.isEmpty()) {
            return null;
        }

        for (UriPermission uriPermission : uriPermissions) {
            DocumentFile treeUri = getAccessDocumentFile(context, uriPermission.getUri(), path);
            if (treeUri != null && treeUri.exists()) {
                return uriPermission.getUri();
            }
        }
        return null;
    }

    /**
     *
     * @param context for get ContentResolver
     * @param path of file for check permission
     * @return  DocumentFile that application can access
     */
    public static @Nullable DocumentFile getAvailabeAccessDocumentDirectory(@NotNull Context context, String path) {
        List<UriPermission> uriPermissions = context.getApplicationContext().getContentResolver().getPersistedUriPermissions();
        if (uriPermissions == null || uriPermissions.isEmpty()) {
            return null;
        }

        for (UriPermission uriPermission : uriPermissions) {
            return getAccessDocumentFile(context, uriPermission.getUri(), path);
        }
        return null;
    }

    /**
     *
     * @param context for get ContentResolver
     * @param rootDir uri of file that Application can access
     * @param path  of file for check permission
     * @return  DocumentFile that application can access
     */
    public static @Nullable DocumentFile getAccessDocumentFile(Context context, Uri rootDir, String path) {
        File file = new File(path);
        if (file.exists()) {
            DocumentFile diretory = DocumentFile.fromTreeUri(context, rootDir);
            if (diretory == null || TextUtils.isEmpty(diretory.getName())) {
                return null;
            }

            try {
                takeUriPermssion(context, rootDir);
                return diretory;
            } catch (SecurityException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static void takeUriPermssion(@NotNull Context context, Uri rootDir) {
        int modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getApplicationContext().getContentResolver().takePersistableUriPermission(rootDir, modeFlags);
    }

}
