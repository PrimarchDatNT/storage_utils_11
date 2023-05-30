package com.tapon.storageandroid11;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tapon.storageandroid11.databinding.ActivityMediaStorageBinding;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MediaStorageActivity extends AppCompatActivity implements MediaAdapter.Callback {

    private static final int IMAGE = 0;
    private static final int VIDEO = 1;
    private static final int AUDIO = 2;
    private static final int NON_MEDIA = 3;

    private static final String TEST_URL = "https://cdna.artstation.com/p/marketplace/presentation_assets/000/168/608/large/file.png?1562800558";

    int currentLoader = -1;
    Uri curUri;

    ActivityMediaStorageBinding binding;
    MediaAdapter mAdapter;
    List<MediaFile> mediaFiles = new ArrayList<>();

    File shareFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMediaStorageBinding.inflate(this.getLayoutInflater());
        this.setContentView(this.binding.getRoot());

        /*this.shareFile = new File(this.getFilesDir(), "Text_" + System.currentTimeMillis() + ".txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.shareFile);
            String content = this.getPackageName() + " Hello World!!!" + "\n" + Utils.formatDate(System.currentTimeMillis());
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d("datnt", "--------- " + MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        }
        Log.d("datnt", "--------- " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Log.d("datnt", "--------- " + MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        Log.d("datnt", "--------- " + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        Log.d("datnt", "--------- " + MediaStore.Files.getContentUri("external"));

        final String[] standardDirectories = {
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_RINGTONES,
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_DOCUMENTS,
        };

        for (String standardDirectory : standardDirectories) {
            File publicDirectory = Environment.getExternalStoragePublicDirectory(standardDirectory);
            Log.d("datnt", "--------- " + publicDirectory.getAbsolutePath() + " | canWrite: " + publicDirectory.canWrite());
        }

        /*
        File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File f1 = new File(pic, "Text_" + System.currentTimeMillis() + ".pdf");
        try {
            Log.d("datnt", "--------- " + f1.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("datnt", "Can create file not in format" );
        }
        */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 140);
                return;
            }
        }

        this.init();
    }

    private void init() {
        this.mAdapter = new MediaAdapter(this);
        this.mAdapter.setCallback(this);
        this.binding.rvMedia.setAdapter(this.mAdapter);
        this.binding.rvMedia.setLayoutManager(new LinearLayoutManager(this));
        this.loadMedia(IMAGE);

        this.binding.tvImage.setOnClickListener(v -> this.loadMedia(IMAGE));
        this.binding.tvVideo.setOnClickListener(v -> this.loadMedia(VIDEO));
        this.binding.tvAudio.setOnClickListener(v -> this.loadMedia(AUDIO));
        this.binding.tvDownloaded.setOnClickListener(v -> this.loadMedia(NON_MEDIA));

        this.binding.tvWriteImage.setOnClickListener(v -> this.onClickWriteImage());
        this.binding.tvWriteVideo.setOnClickListener(v -> this.onClickWriteVideo());
        this.binding.tvWriteAudio.setOnClickListener(v -> this.onClickWriteAudio());
        this.binding.tvWriteFile.setOnClickListener(v -> this.onClickWriteNonMediaFile());
        this.binding.tvDownload.setOnClickListener(v -> this.onClickWriteOnlineMedia());
    }

    private void loadMedia(int mediaLoader) {
        Single.create((SingleOnSubscribe<List<MediaFile>>) emitter -> {
            List<MediaFile> listItem = new ArrayList<>();

            this.currentLoader = mediaLoader;

            Uri collection = this.getTableUri(mediaLoader);

            /*
            //query theo file media cua app tao ra
            String[] projection = new String[]{"_id", MediaStore.MediaColumns.DISPLAY_NAME", "duration", "_size", "owner_package_name"};
            String sortOrder = MediaStore.MediaColumns.DISPLAY_NAME ASC";

            String selection = "owner_package_name = ?";
            String[] selectionArgs = new String[]{this.getPackageName()};
            */

            String[] projection = new String[]{MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE};

            String sortOrder = "date_modified ASC";

            try (Cursor cursor = App.getAppProvider().query(collection, projection, null, null, sortOrder)) {
                // Cache column indices.
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    int size = cursor.getInt(sizeColumn);
                    String datapath = cursor.getString(dataColumn);

                    Uri contentUri = null;
                    String mime;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        switch (mediaLoader) {
                            case IMAGE:
                                contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                break;

                            case VIDEO:
                                contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                                break;

                            case AUDIO:
                                contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                                break;

                            case NON_MEDIA:
                                contentUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);
                                break;
                        }

                    } else {
                        contentUri = MediaStore.Files.getContentUri("external", id);
                    }

                    mime = App.getAppProvider().getType(contentUri);

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    MediaFile mediaFile = new MediaFile(id, contentUri, name, new File(datapath).lastModified(), size, mime);
                    mediaFile.path = datapath;
                    Log.d("datnt", "--------- " + new File(datapath).canRead());
                    listItem.add(mediaFile);
                }
            }

            Collections.sort(listItem, Utils.SORT_BY_DATE);

            emitter.onSuccess(listItem);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<MediaFile>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<MediaFile> listItem) {
                        mediaFiles = new ArrayList<>(listItem);
                        mAdapter.setListItem(mediaFiles);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mediaFiles = new ArrayList<>();
                        mAdapter.setListItem(mediaFiles);
                        e.printStackTrace();
                    }
                });
    }

    private Uri getTableUri(int mediaLoader) {
        Uri collection = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            switch (mediaLoader) {
                case IMAGE:
                    collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    break;

                case VIDEO:
                    collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    break;

                case AUDIO:
                    collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    break;

                case NON_MEDIA:
                    collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    break;
            }
        } else {
            switch (mediaLoader) {
                case IMAGE:
                    collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    break;

                case VIDEO:
                    collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    break;

                case AUDIO:
                    collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    break;

                case NON_MEDIA:
                    collection = MediaStore.Files.getContentUri("external");
                    break;
            }
        }
        return collection;
    }

    private void writeFile(int mediaType, String src, String dst) {
        Completable.create(emitter -> {
            ContentResolver resolver = App.getAppProvider();

            Uri collection = null;

            switch (mediaType) {
                case IMAGE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    } else {
                        collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    }
                    break;
                case VIDEO:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    } else {
                        collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    }

                    break;
                case AUDIO:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    } else {
                        collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    break;

                case NON_MEDIA:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    } else {
                        MediaStore.Files.getContentUri("external");
                    }
                    break;
            }

            ContentValues valueDetail = new ContentValues();
            valueDetail.put(MediaStore.MediaColumns.DISPLAY_NAME, dst);

            Uri uri = resolver.insert(collection, valueDetail);

            try {
                OutputStream outputStream = resolver.openOutputStream(uri);
                BufferedInputStream bis = new BufferedInputStream(this.openFile(src));
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = bis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                bis.close();
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            emitter.onComplete();

        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        loadMedia(mediaType);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void onClickWriteImage() {
        this.writeFile(IMAGE, "image.png", "demo_image_" + System.currentTimeMillis() + ".png");
    }

    private void onClickWriteVideo() {
        this.writeFile(VIDEO, "video.mp4", "demo_video" + System.currentTimeMillis() + ".mp4");
    }

    private void onClickWriteAudio() {
        this.writeFile(AUDIO, "audio.mp3", "demo_audio" + System.currentTimeMillis() + ".mp3");
    }

    private void onClickWriteNonMediaFile() {
        this.writeFile(NON_MEDIA, "config.txt", "text_" + System.currentTimeMillis() + ".txt");
    }

    private void onClickWriteOnlineMedia() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Save Media")
                .setMessage("Input url")
                .setView(input)
                .setPositiveButton("Ok", (dialog, whichButton) -> {
                    Editable text = input.getText();
                    if (TextUtils.isEmpty(text)) {
                        this.saveOnlineMedia(TEST_URL);
                        return;
                    }

                    this.saveOnlineMedia(text.toString());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss())
                .show();
    }

    private void saveOnlineMedia(String url) {
        Completable.create(emitter -> {
            ContentResolver resolver = App.getAppProvider();

            Uri collection;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            ContentValues valueDetail = new ContentValues();
            valueDetail.put(MediaStore.MediaColumns.DISPLAY_NAME, "online_mediaFile_" + System.currentTimeMillis() + ".jpg");

            Uri uri = resolver.insert(collection, valueDetail);

            try {
                OutputStream outputStream = resolver.openOutputStream(uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                InputStream inputStream = this.openURLMedia(url);

                int len;
                while ((len = inputStream.read()) != -1) {
                    stream.write(len);
                    stream.flush();
                }

                stream.toByteArray();
                outputStream.write(stream.toByteArray());
                outputStream.flush();

                emitter.onComplete();

            } catch (IOException e) {
                e.printStackTrace();
                emitter.onError(e);
            }

        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        loadMedia(IMAGE);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private InputStream openURLMedia(String url) throws IOException {
        return new URL(url).openStream();
    }

    private InputStream openFile(String fileName) throws IOException {
        return this.getAssets().open(fileName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 140) {
            this.init();
        }
    }

    @Override
    public void onItemClick(int position, @NotNull MediaFile item) {
        Utils.openNomarlFile(this, item.uri);
    }

    @Override
    public void onClickShareItem(int position, @NotNull MediaFile item) {


/*        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        Uri shareUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", this.shareFile);
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        intent.setType("text/plain*");
        Intent shareIntent = Intent.createChooser(intent, "Message");
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(shareIntent);*/

        Utils.sendfile(this, item.uri);
    }

    @Override
    public void onClickEditItem(int position, @NotNull MediaFile item) {
        if (item.uri.toString().contains("download")) {
            Toast.makeText(this, "Request perrmision via Storage Access Framework", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentResolver resolver = App.getAppProvider();
        List<Uri> urisToModify = new ArrayList<>();
        this.curUri = item.uri;
        urisToModify.add(item.uri);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            PendingIntent editPendingIntent = MediaStore.createWriteRequest(resolver, urisToModify);
            try {
                this.startIntentSenderForResult(editPendingIntent.getIntentSender(), 1001, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onClickDeleteItem(int position, @NotNull MediaFile item) {
        ContentResolver resolver = App.getAppProvider();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (item.uri.toString().contains("downloads")) {
                // TODO: 5/17/2021 chi xoa dc file non media do app tao ra
                resolver.delete(item.uri, null);
                this.loadMedia(NON_MEDIA);
                return;
            }

            List<Uri> urisToModify = new ArrayList<>();
            urisToModify.add(item.uri);
            PendingIntent editPendingIntent = MediaStore.createDeleteRequest(resolver, urisToModify);
            try {
                this.startIntentSenderForResult(editPendingIntent.getIntentSender(), 1000, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            try {
                resolver.delete(item.uri, null, null);
            } catch (SecurityException e) {
                RecoverableSecurityException recoverableSecurityException;

                if (e instanceof RecoverableSecurityException) {
                    recoverableSecurityException = (RecoverableSecurityException) e;
                } else {
                    throw new RuntimeException(e.getMessage(), e);
                }

                IntentSender intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
                try {
                    this.curUri = item.uri;
                    this.startIntentSenderForResult(intentSender, 1000, null, 0, 0, 0, null);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    App.getAppProvider().delete(this.curUri, null, null);
                }
                Toast.makeText(this, "Delete success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Delete faild", Toast.LENGTH_SHORT).show();
            }

            this.loadMedia(this.currentLoader);
        }

        if (requestCode == 1001) {
            // TODO: 4/12/2021  overwire content or rename file here

            /*resolver.update(
                    curUri,
                    updatedSongDetails,
                    selection,
                    selectionArgs);*/

            if (resultCode == Activity.RESULT_OK) {
                Single.create((SingleOnSubscribe<Integer>) emitter -> {
                    ContentResolver resolver = App.getAppProvider();

                    int media = IMAGE;
                    String overwriteFile = "";

                    if (this.curUri.toString().contains("image")) {
                        overwriteFile = "image.png";
                        media = IMAGE;
                    }

                    if (this.curUri.toString().contains("video")) {
                        overwriteFile = "video.mp4";
                        media = VIDEO;
                    }

                    if (this.curUri.toString().contains("audio")) {
                        overwriteFile = "audio.mp3";
                        media = AUDIO;
                    }

                    if (this.curUri.toString().contains("download")) {
                        overwriteFile = "Android11Note.txt";
                        media = NON_MEDIA;
                    }

                    try {
                        OutputStream outputStream = resolver.openOutputStream(this.curUri);
                        BufferedInputStream bis = new BufferedInputStream(this.openFile(overwriteFile));
                        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = bis.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        bis.close();
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    emitter.onSuccess(media);

                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                            }

                            @Override
                            public void onSuccess(@NonNull Integer media) {
                                curUri = null;
                                loadMedia(media);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }
}