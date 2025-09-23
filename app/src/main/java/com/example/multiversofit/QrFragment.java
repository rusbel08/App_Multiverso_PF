package com.example.multiversofit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrFragment extends Fragment {

    private PreviewView previewView;
    private LinearLayout layoutLoading;

    private ExecutorService cameraExecutor;
    private FirebaseFirestore db;
    private boolean procesando = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_qr, container, false);

        previewView = root.findViewById(R.id.previewView);
        layoutLoading = root.findViewById(R.id.layoutLoading);
        db = FirebaseFirestore.getInstance();

        cameraExecutor = Executors.newSingleThreadExecutor();
        //startCamera();

        return root;
    }

    /*private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScanner scanner = BarcodeScanning.getClient();

                analysis.setAnalyzer(cameraExecutor, (ImageProxy imageProxy) -> {
                    @androidx.camera.core.ExperimentalGetImage
                    android.media.Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                        scanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    if (!barcodes.isEmpty() && !procesando) {
                                        procesando = true;
                                        String dni = barcodes.get(0).getRawValue();
                                        registrarAsistencia(dni);
                                    }
                                })
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);

            } catch (Exception e) {
                Log.e("QR", "Error al iniciar cámara", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void registrarAsistencia(String dni) {
        requireActivity().runOnUiThread(() -> layoutLoading.setVisibility(View.VISIBLE));

        db.collection("asistencias")
                .add(new Asistencia(dni, new Date()))
                .addOnSuccessListener(documentReference -> {
                    requireActivity().runOnUiThread(() -> {
                        layoutLoading.setVisibility(View.GONE);
                        ToastUtils.showCustomToast(requireActivity(), "Asistencia registrada: " + dni);
                        procesando = false;
                    });
                })
                .addOnFailureListener(e -> {
                    requireActivity().runOnUiThread(() -> {
                        layoutLoading.setVisibility(View.GONE);
                        ToastUtils.showCustomToast(requireActivity(), "Error al registrar asistencia");
                        procesando = false;
                    });
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }*/
}
