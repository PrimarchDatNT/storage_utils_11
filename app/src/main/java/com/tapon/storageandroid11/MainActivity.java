package com.tapon.storageandroid11;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tapon.storageandroid11.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding mBinding = ActivityMainBinding.inflate(this.getLayoutInflater());
        this.setContentView(mBinding.getRoot());

        mBinding.btManagerStorage.setOnClickListener(v -> this.startActivity(new Intent(this, FileManagerActivity.class)));

        mBinding.btMediaStorage.setOnClickListener(v -> this.startActivity(new Intent(this, MediaStorageActivity.class)));

        mBinding.btStorageAccessFramework.setOnClickListener(v -> this.startActivity(new Intent(this, SAFrameworkActivity.class)));

    }
}