package br.edu.ufam.icomp.tesi;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

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
        initKeys();
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
                sendImageBinary(cameraKitImage.getBitmap(), cameraKitImage.getJpeg());
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

    private void initKeys() {
        keys.put("ver câmera", CAMERA);
        keys.put("abrir câmera", CAMERA);
        keys.put("câmera", CAMERA);
        keys.put("showCamera", CAMERA);
        keys.put("ver showCamera", CAMERA);
        keys.put("abrir showCamera", CAMERA);
        keys.put("iniciar câmera", CAMERA);
        keys.put("iniciar showCamera", CAMERA);

        keys.put("tirar foto", TAKE_PICTURE);
        keys.put("fotografar", TAKE_PICTURE);
        keys.put("bater foto", TAKE_PICTURE);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            findWord(results);
        }

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

    private void sendImageBinary(Bitmap bitmap, byte[] contents) {
        File file = new File(getCacheDir(), "teste.jpeg");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] buf= null;
        try {
            buf = new byte[in.available()];
            while (in.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        repeatTTS.speak("Processando imagem... Por favor, aguarde.", TextToSpeech.QUEUE_ADD, null);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), contents);

        retrofit2.Call<Void> req = RetrofitService.getInstance(this).tesiService().sendImage(requestBody);
        req.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
              if (response.code() == 202) {
                  retrofit2.Call<Recognized> req2 = RetrofitService.getInstance(MainActivity.this)
                          .tesiService().getRecognized(response.headers().get("Operation-Location"));

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
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void processedText(Recognized recognized) {
        if (recognized != null && recognized.getRecognationResult() != null) {
            List<Lines> lines = recognized.getRecognationResult().getLines();

            StringBuilder text = new StringBuilder();

            for (Lines line : lines) {
                text.append(line.getText()).append(" ");
            }

            if (text.length() > 0) {
                repeatTTS.speak("A palavra identificada é: " + text, TextToSpeech.QUEUE_ADD, null);
            } else {
                reportError();
            }
        } else {
            reportError();
        }
    }

    private void reportError() {
        repeatTTS.speak("Houve um problema na identificação da palavra. Por favor, tente novamente.", TextToSpeech.QUEUE_ADD, null);
    }

    private void findWord(List<String> results) {
        for (String result : results) {
            Integer value = keys.get(result.toLowerCase());

            if (value != null) {
                switch (value) {
                    case CAMERA:
//                        if (repeatTTS != null) {
//                            repeatTTS.speak("Abrindo câmera... Por favor, aguarde.", TextToSpeech.QUEUE_ADD, null);
//                        }
//                        cameraView.start();
                        break;
                    case TAKE_PICTURE:
                        cameraView.captureImage();
                        break;
                }
            }
        }
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga: Abrir câmera/tirar foto");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech não suportado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            repeatTTS.setLanguage(new Locale("pt", "POR"));
        }
    }

    @OnClick(R.id.button_camera)
    public void openCamera() {
        cameraView.captureImage();
    }
}
