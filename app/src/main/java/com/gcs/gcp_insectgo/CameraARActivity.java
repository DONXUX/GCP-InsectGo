package com.gcs.gcp_insectgo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

// AR Camera 클래스
// ARCore Sceneform SDK 사용
// 평면을 인식하여 선택된 곤충을 그래픽을 hit 된 평면위에 띄웁니다.

public class CameraARActivity extends AppCompatActivity {
    private static final String TAG = CameraARActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable insectRenderable;
    public String insect;
    ConstraintLayout container;
    VisionApi va = new VisionApi();

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this))
            return;
        setContentView(R.layout.activity_camera_ar);
        container =  findViewById(R.id.capture);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        assert arFragment != null;
        insect = "spider";
        createARObject();

        FloatingActionButton take = findViewById(R.id.take_btn);
        take.setOnClickListener(view -> {
           Intent move = new Intent(CameraARActivity.this,VisionApi.class);
           startActivity(move);
        });

    }

    // ARCore 를 지원하는 안드로이드 사양인지 확인
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        /* if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "지원하지 않는 Android 버전입니다.");
            Toast.makeText(activity, "지원하지 않는 Android 버전입니다.", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        } */ // minSdkVersion 이 24라 필요없음
        ActivityManager am = (ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        String openGlVersionString =
                am.getDeviceConfigurationInfo().getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public void createARObject(){
        /// 3D 모델 불러오기
        ModelRenderable.builder()
                .setSource(this, getResources().getIdentifier(insect,"raw", CameraARActivity.this.getPackageName()))
                .build()
                .thenAccept(renderable -> insectRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "곤충 랜더러를 불러올수 없습니다.", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        /// 평면을 인식하고 터치한 화면 좌표를 현실 3D 좌표로 변환하여 hit 합니다.
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (insectRenderable == null)
                        return;

                    /// Anchor 객체 생성
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(insectRenderable);
                    andy.select();
                }
        );
    }

    public void loadUser(){

    }
}

