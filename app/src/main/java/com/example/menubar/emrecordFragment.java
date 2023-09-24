package com.example.menubar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class emrecordFragment extends Fragment {

    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private boolean isRecording = false;
    private AudioRecord audioRecorder;
    private File audiofile;
    private Thread recordingThread;

    Button startButton, stopButton;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.get(Manifest.permission.RECORD_AUDIO) == Boolean.TRUE
                        && result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == Boolean.TRUE) {
                    startRecording();
                } else {
                    Log.d("AudioRecording", "Permission denied"); // Log permission denied
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emrecord, container, false);

        startButton = view.findViewById(R.id.start);
        stopButton = view.findViewById(R.id.stop);

        startButton.setOnClickListener(v -> {
            if (!isRecording) {
                requestPermissions();
            } else {
                stopRecording();
            }
        });

        return view;
    }

    private void requestPermissions() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissionLauncher.launch(permissions);
    }

    private void startRecording() {
        isRecording = true;
        startButton.setText("Stop Recording");

        // Check for RECORD_AUDIO and WRITE_EXTERNAL_STORAGE permissions
        int recordAudioPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO);
        int writeStoragePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (recordAudioPermission == PackageManager.PERMISSION_GRANTED && writeStoragePermission == PackageManager.PERMISSION_GRANTED) {
            // Initialize audio recorder
            int bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);
            audioRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    AUDIO_SAMPLE_RATE,
                    AUDIO_CHANNEL,
                    AUDIO_ENCODING,
                    bufferSize
            );

            // Create and start recording thread
            recordingThread = new Thread(() -> {
                byte[] buffer = new byte[bufferSize];
                audiofile = createAudioFile();

                try {
                    audioRecorder.startRecording();
                    FileOutputStream fos = new FileOutputStream(audiofile);

                    while (isRecording) {
                        int bytesRead = audioRecorder.read(buffer, 0, bufferSize);
                        if (bytesRead != AudioRecord.ERROR_INVALID_OPERATION) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }

                    fos.close();
                    audioRecorder.stop();
                    audioRecorder.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            recordingThread.start();
        } else {
            // Handle the case where permissions are not granted
            Log.d("AudioRecording", "Permission denied"); // Log permission denied
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            // You may want to disable the recording button here or take other appropriate action
        }
    }

    private void stopRecording() {
        isRecording = false;
        startButton.setText("Start Recording");

        try {
            recordingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (audiofile != null) {
            // Save the audio file and add it to the media library
            saveRecordingToMediaStore(audiofile);
            Toast.makeText(requireContext(), "Audio saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private File createAudioFile() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "sound_" + timestamp + ".wav";

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        return new File(dir, fileName);
    }

    private void saveRecordingToMediaStore(File audiofile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, audiofile.getName());
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav");
        values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC);

        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri audioUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if (audioUri != null) {
            try {
                OutputStream outputStream = contentResolver.openOutputStream(audioUri);

                if (outputStream != null) {
                    FileInputStream inputStream = new FileInputStream(audiofile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    // Check if deletion was successful
                    if (audiofile.delete()) {
                        // File deletion successful
                    } else {
                        // File deletion failed, handle the error
                        Log.e("AudioRecording", "Failed to delete audio file: " + audiofile.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireContext(), "Error creating audio file", Toast.LENGTH_SHORT).show();
        }
    }


}
