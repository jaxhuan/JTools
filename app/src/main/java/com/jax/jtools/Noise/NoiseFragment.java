package com.jax.jtools.Noise;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jax.jtools.R;
import com.jax.jtools.Util.FileUtil;

import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by userdev1 on 3/24/2017.
 */

public class NoiseFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {

    public static final int RC_AUDIO_SD = 0;

    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

    @BindView(R.id.fram_noise_db)
    TextView mFramNoiseDb;
    @BindView(R.id.fram_noise_nav)
    BottomNavigationView mFramNoiseNav;
    @BindString(R.string.permission_audio)
    String permission_audio;
    @BindString(R.string.permission_audio_denied)
    String permission_audio_denied;
    @BindString(R.string.noise_db)
    String unit_db;

    NoiseType mNoiseType = NoiseType.Media;
    //Media
    MediaRecorder mMediaRecorder;
    boolean isRecorder = false;
    boolean isMeDiaRecordering = false;
    //Audio
    AudioRecord mAudioRecord;
    boolean isAudioRecorder = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fram_noise, container, false);
        ButterKnife.bind(this, rootView);
        mFramNoiseNav.setOnNavigationItemSelectedListener(this);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();

        requestAudioAndStorge();
    }

    @Override
    public void onPause() {
        super.onPause();
        isRecorder = false;
        isAudioRecorder = false;
        if (null != mAudioRecord) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (null != mMediaRecorder) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(getActivity(), permission_audio_denied, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fram_noise_nav_media:
                mNoiseType = NoiseType.Media;
                break;
            case R.id.fram_noise_nav_audio:
                mNoiseType = NoiseType.Audio;
                break;
        }
        requestAudioAndStorge();
        return true;
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        requestAudioAndStorge();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            new AppSettingsDialog.Builder(this)
                    .build()
                    .show();
    }

    @AfterPermissionGranted(RC_AUDIO_SD)
    private void requestAudioAndStorge() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            //录音，计算音量
            switch (mNoiseType) {
                case Media:
                    isRecorder = true;
                    isAudioRecorder = false;
                    if (null != mAudioRecord) {
                        mAudioRecord.stop();
                        mAudioRecord.release();
                        mAudioRecord = null;
                    }
                    if (isMeDiaRecordering)
                        break;
                    if (null == mMediaRecorder)
                        mMediaRecorder = new MediaRecorder();
                    mMediaRecorder.reset();
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mMediaRecorder.setOutputFile(FileUtil.createCacheFile("cache_audio.amr").getPath());
                    try {
                        mMediaRecorder.prepare();
                        mMediaRecorder.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (isRecorder) {
                                isMeDiaRecordering = true;
                                int BASE = 5;
                                double radio = mMediaRecorder.getMaxAmplitude() / BASE;
                                double db = 0;
                                if (radio > 1)
                                    db = 20 * Math.log10(radio);
                                Flowable.just(db)
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Double>() {
                                            @Override
                                            public void accept(Double aDouble) throws Exception {
                                                mFramNoiseDb.setText(String.format("%.2f " + unit_db, aDouble));
                                            }
                                        });
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            isMeDiaRecordering = false;
                        }
                    }).start();
                    break;
                case Audio:
                    isRecorder = false;
                    isAudioRecorder = true;
                    if (null != mMediaRecorder) {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        mMediaRecorder.release();
                        mMediaRecorder = null;
                    }
                    if (null == mAudioRecord)
                        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                8000, AudioFormat.CHANNEL_IN_DEFAULT,
                                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
                    if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                        break;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mAudioRecord.startRecording();
                            short[] buffer = new short[BUFFER_SIZE];
                            while (isAudioRecorder) {
                                //r是实际读取的数据长度，一般而言r会小于buffersize
                                int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                                long v = 0;
                                // 将 buffer 内容取出，进行平方和运算
                                for (int i = 0; i < buffer.length; i++) {
                                    v += buffer[i] * buffer[i];
                                }
                                // 平方和除以数据总长度，得到音量大小。注：如果去绝对值的平均数则下面计算乘以20
                                double mean = v / (double) r;
                                if (mean == 0.0)
                                    mean = 1;
                                double volume = 10 * Math.log10(mean);
                                Flowable.just(volume)
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Double>() {
                                            @Override
                                            public void accept(Double aDouble) throws Exception {
                                                mFramNoiseDb.setText(String.format("%.2f " + unit_db, aDouble));
                                            }
                                        });
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    break;
            }
        } else {
            EasyPermissions.requestPermissions(this, permission_audio, RC_AUDIO_SD, perms);
        }
    }
}
