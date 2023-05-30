package com.tapon.storageandroid11;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NFCShareAcitvity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    boolean androidBeamAvailable  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcshare_acitvity);

        // NFC isn't available on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            /*
             * Disable NFC features here.
             * For example, disable menu items or buttons that activate
             * NFC-related features
             */
            // Android Beam file transfer isn't supported
        } else {
            androidBeamAvailable = true;
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }

    }

    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback {
        public FileUriCallback() {
        }
        /**
         * Create content URIs as needed to share with another device
         */
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            return new Uri[3];
        }
    }
}