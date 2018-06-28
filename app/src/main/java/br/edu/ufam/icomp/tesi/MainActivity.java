package br.edu.ufam.icomp.tesi;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.edu.ufam.icomp.tesi.model.Lines;
import br.edu.ufam.icomp.tesi.model.Recognized;
import br.edu.ufam.icomp.tesi.service.RetrofitService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final String DEBUG_TAG = "MainActivity";
    private static final int TTS_REQUEST_CODE = 10;
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int CAMERA = 0;
    private static final int TAKE_PICTURE = 1;

    @BindView(R.id.picture)
    ImageView picture;
    @BindView(R.id.camera)
    CameraView cameraView;

    private Unbinder unbinder;

    private HashMap<String, Integer> keys = new HashMap<>();
    private TextToSpeech repeatTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        unbinder = ButterKnife.bind(this);

        initCamera();
        initTTS();
    }

    private void initCamera() {
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap resized = Bitmap.createScaledBitmap(cameraKitImage.getBitmap(), 768, 1024, false);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 100, os);

                byte[] array = os.toByteArray();

                sendImageBinary(array);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }

    private void initTTS() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, TTS_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
//        displaySpeechRecognizer();
        cameraView.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        repeatTTS.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TTS_REQUEST_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                repeatTTS = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void sendImageBinary(byte[] contents) {
        repeatTTS.speak("Analyzing...", TextToSpeech.QUEUE_ADD, null);
        Toast.makeText(getApplicationContext(), "Analyzing...", Toast.LENGTH_SHORT).show();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), contents);

        retrofit2.Call<ResponseBody> req = RetrofitService.getInstance(this).tesiService().sendImage(requestBody);

        Log.d(DEBUG_TAG, req.request().url().toString());

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 202) {
                  Log.d(DEBUG_TAG, "Response 202!");

                  final retrofit2.Call<Recognized> req2 = RetrofitService.getInstance(MainActivity.this)
                          .tesiService().getRecognized(response.headers().get("Operation-Location"));

                  Log.d(DEBUG_TAG, req2.request().url().toString());
                  Log.d(DEBUG_TAG, response.headers().get("Operation-Location"));

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            req2.enqueue(new Callback<Recognized>() {
                                @Override
                                public void onResponse(Call<Recognized> call2, Response<Recognized> response2) {
                                    if (response2.code() == 200) {
                                        Recognized recognized = response2.body();
                                        processedText(recognized);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Recognized> call2, Throwable t2) {

                                }
                            });
                        }
                    }, 1500);


              }else{
                    Toast.makeText(getApplicationContext(), "Error in recognition service!", Toast.LENGTH_SHORT).show();
                    Log.d(DEBUG_TAG, "" + response.code());
                    Log.d(DEBUG_TAG, response.headers().toString());
                    Log.d(DEBUG_TAG, response.raw().toString());
                    try {
                        Log.d(DEBUG_TAG, response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void processedText(Recognized recognized) {
        Log.d(DEBUG_TAG, "Recognized" + recognized);
        if (recognized != null && recognized.getRecognitionResult() != null) {
            List<Lines> lines = recognized.getRecognitionResult().getLines();

            StringBuilder text = new StringBuilder();

            for (Lines line : lines) {
                text.append(line.getText()).append(" ");
            }

            if (text.length() > 0) {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                repeatTTS.speak(text.toString(), TextToSpeech.QUEUE_ADD, null);
            } else {
                reportError();
            }
        } else {
            reportError();
        }
    }

    private void reportError() {
        Toast.makeText(getApplicationContext(), "Oops, there is problem. Try again!", Toast.LENGTH_SHORT).show();
        repeatTTS.speak("Oops, there is problem. Try again!", TextToSpeech.QUEUE_ADD, null);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            cameraView.captureImage();
        }
        return true;
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            repeatTTS.setLanguage(new Locale("en", "US"));
        }
    }

    @OnClick(R.id.button_camera)
    public void openCamera() {
        cameraView.captureImage();
    }
}
