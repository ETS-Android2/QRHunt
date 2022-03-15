package com.bigyoshi.qrhunt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.zxing.Result;

/**
 * Definition: Scanner with camera - Scans and decodes QR code
 * Note: NA
 * Issues: TBA
 */
public class FragmentScanner extends Fragment {
    private CodeScanner codeScanner;
    private AugmentedCamera camera;
    private String playerId;

    /**
     * Sets up fragment to be loaded in, finds all views, sets onClickListener for buttons
     * @param inflater Inflater
     * @param container Where the fragment is contained
     * @param savedInstanceState SavedInstanceState
     * @return root
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getActivity().getSupportFragmentManager().setFragmentResultListener("getPlayer",
                this,
                new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Player player = (Player) result.getSerializable("player");
                playerId = player.getPlayerId();
            }
        });

        final Activity activity = getActivity();
        View root = inflater.inflate(R.layout.scanner_fragment, container, false);

        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        assert activity != null;
        codeScanner = new CodeScanner(activity, scannerView);

        codeScanner.setCamera(CodeScanner.CAMERA_BACK);
        codeScanner.setScanMode(ScanMode.PREVIEW);
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        codeScanner.setFlashEnabled(false);
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        camera = new AugmentedCamera(FragmentScanner.this, result.getText(), playerId);
                        codeScanner.setScanMode(ScanMode.PREVIEW);
                        camera.processQRCode();
                    }
                });
            }
        });

        codeScanner.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(@NonNull Throwable thrown) {
                Log.e("CAMERA", "Camera has failed: ", thrown );
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeScanner.startPreview();
                codeScanner.setScanMode(ScanMode.SINGLE);
            }
        });
        return root;
    }

    /**
     * Handles when the state is resumed (starts camera previous)
     */
    @Override
    public void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    /**
     * Handles when the state is paused (release resources)
     */
    @Override
    public void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }
}

