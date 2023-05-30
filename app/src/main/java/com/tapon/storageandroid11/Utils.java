package com.tapon.storageandroid11;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {


    public static final int REQ_DOCUMENT_TREE_CODE = 1368;
    public static final int REQ_DOCUMENT_FILE_CODE = 1369;

    private static final char EXTENSION_SEPARATOR = '.';

    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final String PRIMARY_VOLUME_NAME = "primary";
    public static Comparator<MediaFile> SORT_BY_DATE = (left, right) -> Long.compare(right.time, left.time);
    public static Comparator<File> SORT_BY_NAME = (left, right) -> {
        String nameLeft = left.getName().toLowerCase();
        String nameRight = right.getName().toLowerCase();
        return nameLeft.compareTo(nameRight);
    };

    public static void delete(final Activity activity, final Uri[] uriList, final int requestCode) throws SecurityException, IntentSender.SendIntentException, IllegalArgumentException {
        final ContentResolver resolver = activity.getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // WARNING: if the URI isn't a MediaStore Uri and specifically
            // only for media files (images, videos, audio), the request
            // will throw an IllegalArgumentException, with the message:
            // 'All requested items must be referenced by specific ID'

            // No need to handle 'onActivityResult' callback, when the system returns
            // from the user permission prompt the files will be already deleted.
            // Multiple 'owned' and 'not-owned' files can be combined in the
            // same batch request. The system will automatically delete them using the
            // using the same prompt dialog, making the experience homogeneous.

            final List<Uri> list = new ArrayList<>();
            Collections.addAll(list, uriList);

            final PendingIntent pendingIntent = MediaStore.createDeleteRequest(resolver, list);
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, null, 0, 0, 0, null);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            try {
                // In Android == Q a RecoverableSecurityException is thrown if not-owned.
                // For a batch request the deletion will stop at the failed not-owned
                // file, so you may want to restrict deletion in Android Q to only
                // 1 file at a time, to make the experience less ugly.
                // Fortunately this gets solved in Android R.

                for (final Uri uri : uriList) {
                    resolver.delete(uri, null, null);
                }
            } catch (RecoverableSecurityException ex) {
                final IntentSender intent = ex.getUserAction()
                        .getActionIntent()
                        .getIntentSender();

                // IMPORTANT: still need to perform the actual deletion
                // as usual, so again getContentResolver().delete(...),
                // in your 'onActivityResult' callback, as in Android Q
                // all this extra code is necessary 'only' to get the permission,
                // as the system doesn't perform any actual deletion at all.
                // The onActivityResult doesn't have the target Uri, so you
                // need to catch it somewhere.
                activity.startIntentSenderForResult(intent, requestCode, null, 0, 0, 0, null);
            }
        } else {
            // As usual for older APIs

            for (final Uri uri : uriList) {
                resolver.delete(uri, null, null);
            }
        }
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void reqUriPermission(Activity activity, String path) {
        StorageManager storageManager = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume storageVolume = storageManager.getStorageVolume(new File(path));
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent = storageVolume.createOpenDocumentTreeIntent();
        } else {
            intent = storageVolume.createAccessIntent(null);
        }

        try {
            activity.startActivityForResult(intent, 1528);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void openNomarlFile(Context context, Uri uri) {
        if (uri == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType = context.getApplicationContext().getContentResolver().getType(uri);

        if (TextUtils.isEmpty(mimeType)) {
            intent.setDataAndType(uri, "*/*");
        } else {
            intent.setDataAndType(uri, mimeType);
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    public static void sendfile(@NotNull Context context, @NonNull Uri listInput) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Collections.singletonList(listInput)));
        intent.setType("*/*");

        try {
            Intent iShare = Intent.createChooser(intent, "Share");
            ((Activity) context).startActivityForResult(iShare, 1001);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Uri createUri(Context context, String path) {
        if (Build.VERSION.SDK_INT < 24) {
            return Uri.fromFile(new File(path));
        }

        try {
//            String provideName = BuildConfig.APPLICATIFON_ID + ".provider";
            String provideName = "com.tapon.storageandroid11" + ".provider";
            return FileProvider.getUriForFile(context, provideName, new File(path));
        } catch (IllegalArgumentException e) {
            return new Uri.Builder().build();
        }
    }

    @NotNull
    public static String getExtension(@NotNull File file) {
        if (!file.exists()) {
            return "Unknow";
        }

        if (file.isDirectory()) {
            return "FOLDER";
        }

        String ex = FilenameUtils.getExtension(file.getName());
        if (TextUtils.isEmpty(ex)) {
            return "Unknow";
        } else {
            return ex;
        }
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static int indexOfExtension(final String filename) {
        if (filename == null) {
            return -1;
        }

        final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    public static int indexOfLastSeparator(final String filename) {
        if (filename == null) {
            return -1;
        }

        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    public static long millisecsToDay(long millis) {
        return TimeUnit.MILLISECONDS.toDays(millis);
    }

    @SuppressLint("DefaultLocale")
    public static @NotNull String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        if (hours == 0) {
            return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }
        return String.format("%02d:%02d:%02d", hours,
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @NotNull
    public static String formatDate(long millis) {
        @SuppressLint("SimpleDateFormat") DateFormat formater = new SimpleDateFormat("dd/MMM/yyyy - HH:mm");
        return formater.format(new Date(millis));
    }

    @NotNull
    public static String convertBytes(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static @Nullable String getExtCardPath(boolean isFileDir) {
        ArrayList<ExDirItem> dirList = getDirList();
        if (dirList.size() < 2) {
            return null;
        }
        return isFileDir ? dirList.get(1).fileDir : dirList.get(1).rootDir;
    }

    public static @NotNull ArrayList<ExDirItem> getDirList() {
        String str;
        ArrayList<ExDirItem> arrayList = new ArrayList<>();

        File[] fileArr = null;
        try {
            fileArr = App.getInstance().getExternalFilesDirs(null);
        } catch (NullPointerException unused) {
            unused.printStackTrace();
        }

        if (fileArr == null) {
            return arrayList;
        }

        for (int i = 0; i < fileArr.length; i++) {
            try {
                File file = fileArr[i];
                if (file != null) {
                    str = Environment.getExternalStorageState(file);
                    if (str.equals("mounted") || str.equals("mounted_ro") || str.equals("shared")) {

                        ExDirItem exDirItem = new ExDirItem();
                        String absolutePath = file.getAbsolutePath();
                        exDirItem.fileDir = absolutePath;
                        exDirItem.usb = false;

                        if (Build.VERSION.SDK_INT >= 24) {
                            App context = App.getInstance();
                            exDirItem.usb = context.getSystemService(StorageManager.class).getStorageVolume(new File(exDirItem.fileDir)).getDescription(context).toUpperCase().contains("USB");
                        }

                        exDirItem.intCard = i == 0;
                        int indexOf = absolutePath.indexOf("/Android/");

                        if (indexOf != -1) {
                            exDirItem.rootDir = absolutePath.substring(0, indexOf);
                            arrayList.add(exDirItem);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    @Nullable
    public static String getFullPathFromTreeUri(Context con, @Nullable final Uri treeUri) {
        if (treeUri == null) return null;
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri), con);
        if (volumePath == null) return File.separator;
        if (volumePath.endsWith(File.separator))
            volumePath = volumePath.substring(0, volumePath.length() - 1);

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator))
            documentPath = documentPath.substring(0, documentPath.length() - 1);

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator))
                return volumePath + documentPath;
            else
                return volumePath + File.separator + documentPath;
        } else return volumePath;
    }

    private static String getVolumePath(final String volumeId, Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? getVolumePathForAndroid11AndAbove(context, volumeId) : getVolumePathBeforeAndroid11(context, volumeId);
    }

    private static String getVolumePathBeforeAndroid11(Context context, final String volumeId) {
        try {
            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);
            if (result == null) {
                return null;
            }

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary != null && primary && PRIMARY_VOLUME_NAME.equals(volumeId))
                    return (String) getPath.invoke(storageVolumeElement);

                if (uuid != null && uuid.equals(volumeId))    // other volumes?
                    return (String) getPath.invoke(storageVolumeElement);
            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    private static String getVolumePathForAndroid11AndAbove(Context context, final String volumeId) {
        try {
            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            List<StorageVolume> storageVolumes = mStorageManager.getStorageVolumes();
            for (StorageVolume storageVolume : storageVolumes) {
                // primary volume?
                if (storageVolume.isPrimary() && PRIMARY_VOLUME_NAME.equals(volumeId))
                    return storageVolume.getDirectory().getPath();

                // other volumes?
                String uuid = storageVolume.getUuid();
                if (uuid != null && uuid.equals(volumeId))
                    return storageVolume.getDirectory().getPath();

            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        } else {
            return null;
        }
    }

    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        } else {
            return File.separator;
        }
    }

    public static int dip2px(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int px2dip(Context context, float f) {
        return (int) ((f / context.getResources().getDisplayMetrics().density) + 0.5f);
    }


}
