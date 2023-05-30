package com.tapon.storageandroid11;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import office.IOffice;

public class DocumentViewerActivity extends AppCompatActivity {

    private IOffice office;

    public static final String EXTRA_DATA ="extra_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);
        this.office = new IOffice() {
            @Override
            public Activity getActivity() {
                return DocumentViewerActivity.this;
            }

            @Override
            public String getAppName() {
                return "----";
            }

            @Override
            public File getTemporaryDirectory() {
                return DocumentViewerActivity.this.getCacheDir();
            }

            @Override
            public void openFileFinish() {
                LinearLayout viewById = findViewById(R.id.llContent);
                viewById.postDelayed(() -> {
                    viewById.removeAllViews();
                    viewById.addView(this.getView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                },500);
            }

            @Override
            public void fullScreen(boolean fullscreen) {

            }
        };

        this.office.openFile(this.getIntent().getStringExtra(EXTRA_DATA));
    }
}