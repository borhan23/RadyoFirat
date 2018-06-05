package co.mbo.radyofirat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.vodyasov.amr.AudiostreamMetadataManager;
import com.vodyasov.amr.OnNewMetadataListener;
import com.vodyasov.amr.UserAgent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.mbo.library.radio.RadioListener;
import co.mbo.library.radio.RadioManager;


public class RadioActivity extends Activity implements RadioListener{
    private static boolean userPressedBackAgain = false;
    private final String[] RADIO_URL = {"http://radyofirat.radyotvyayini.com:8920"};
    FloatingActionMenu fam;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;
    Button mButtonControlStart;
    TextView mTextViewControl;
    RadioManager mRadioManager;
    IcyStreamMeta streamMeta;
    //MetadataTask2 metadataTask2;
    String title_artist;
    TextView textView;
    private SeekBar volumeBar;
    private AudioManager audioManager;
    MaterialPlayPauseButton materialPlayPauseButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        streamMeta = new IcyStreamMeta();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initControls();

        materialPlayPauseButton = (MaterialPlayPauseButton) findViewById(R.id.PlayPauseButton);

        materialPlayPauseButton.setColor(Color.BLACK);

        materialPlayPauseButton.setAnimDuration(300);

        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.floating_facebook);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.floating_twitter);
        floatingActionButton3 = (FloatingActionButton) findViewById(R.id.floating_instagram);
        fam = (FloatingActionMenu) findViewById(R.id.fab_menu);

        fam.setIconAnimated(false);
        fam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fam.isOpened()) {
                    fam.close(true);
                }
            }
        });

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu first item clicked
                Intent facebookIntent = getOpenFacebookIntent(RadioActivity.this);
                startActivity(facebookIntent);

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu second item clicked
                Intent twitterIntent = getOpenTwitterIntent(RadioActivity.this);
                startActivity(twitterIntent);

            }
        });

        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO something when floating action menu second item clicked
                Intent instagramIntent = getOpenInstagramIntent(RadioActivity.this);
                startActivity(instagramIntent);
            }
        });
        textView = (TextView) findViewById(R.id.main_activity_text_view);

        mRadioManager = RadioManager.with(getApplicationContext());
        mRadioManager.registerListener(this);
        mRadioManager.setLogging(true);

        initializeUI();
    }



    private void initControls() {
        try{
            volumeBar = (SeekBar) findViewById(R.id.sesBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch (Exception e){

        }
    }
    OnNewMetadataListener listener = new OnNewMetadataListener() {
        @Override
        public void onNewHeaders(String stringUri, List<String> name, List<String> desc,
                                 List<String> br, List<String> genre, List<String> info) {
        }

        @Override
        public void onNewStreamTitle(String stringUri, String streamTitle) {
            streamTitle = streamTitle.replace("StreamUrl=","");
            streamTitle = streamTitle.replace("_"," ");
            textView.setText(streamTitle);
        }
    };
    public void initializeUI() {
        materialPlayPauseButton = (MaterialPlayPauseButton) findViewById(R.id.PlayPauseButton);
        mTextViewControl = (TextView) findViewById(R.id.textviewControl);
        final String streamUrl = "http://radyofirat.radyotvyayini.com:8920/";


            materialPlayPauseButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!InternetKontrol()) {
                    /*AlertDialog alertDialog = new AlertDialog.Builder(RadioActivity.this).create();
                    alertDialog.setMessage("İnternet Bağlantınızı Kontrol Edin.");
                    alertDialog.show();*/
                        materialPlayPauseButton.setToPlay();
                        mTextViewControl.setText("İnternet Bağlantısı Yok.");
                        textView.setText("Elazığ 89.2 Radyo Fırat");
                    } else {
                        Timer timer= new Timer();
                        if (!mRadioManager.isPlaying()) {
                            mRadioManager.startRadio(RADIO_URL[0]);
                            materialPlayPauseButton.setToPause();
                            Uri uri = Uri.parse(streamUrl);
                            AudiostreamMetadataManager.getInstance()
                                    .setUri(uri)
                                    .setOnNewMetadataListener(listener)
                                    .setUserAgent(UserAgent.WINDOWS_MEDIA_PLAYER)
                                    .start();
                        } else {
                            mRadioManager.stopRadio();
                            materialPlayPauseButton.setToPlay();
                            AudiostreamMetadataManager.getInstance().stop();
                            textView.setText("Elazığ 89.2 Radyo Fırat");
                        }

                    }
                }


            });
    }
                           /* timer.schedule(new TimerTask() {

                                public void run() {

                                    String streamUrl = "http://radyofirat.radyotvyayini.com:8920/";
                                    if(InternetKontrol()){
                                    try {
                                            streamMeta.setStreamUrl(new URL(streamUrl));
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }
                                        metadataTask2 = new MetadataTask2();
                                        try {
                                            metadataTask2.execute(new URL(streamUrl));
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }

                            }, 0, 6000);*/

    public boolean InternetKontrol() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mRadioManager.connect();
    }

    @Override
    public void onRadioLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here.
                mTextViewControl.setText("RADYO DURUMU : YÜKLENİYOR...");
            }
        });
    }

    @Override
    public void onRadioConnected() {

    }

    @Override
    public void onRadioStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here.
                mTextViewControl.setText("RADYO DURUMU : ÇALIYOR...");
            }
        });
    }

    @Override
    public void onRadioStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO Do UI works here
                mTextViewControl.setText("RADYO DURUMU : DURDU");
            }
        });
    }

    @Override
    public void onMetaDataReceived(String s, String s1) {
        //TODO Check metadata values. Singer name, song name or whatever you have.
    }

    @Override
    public void onError() {

    }
    /*protected class MetadataTask2 extends AsyncTask<URL, Void, IcyStreamMeta>
    {
        @Override
        protected IcyStreamMeta doInBackground(URL... urls)
        {

            try
            {
                streamMeta.refreshMeta();
                Log.e("Retrieving MetaData","Refreshed Metadata");
            }
            catch (IOException e)
            {
                Log.e(MetadataTask2.class.toString(), e.getMessage());
            }
            return streamMeta;


        }

        @Override
        protected void onPostExecute(IcyStreamMeta result)
        {

            try
            {

                title_artist=streamMeta.getStreamTitle();
                byte[] textBytes = title_artist.getBytes("UTF-8");
                String value = new String(textBytes , "Windows-1252");
                title_artist= value;
                Log.e("Retrieved title_artist", title_artist);
                if(title_artist.length()>0)
                {
                    title_artist = CharConvert(title_artist);
                    textView.setText(title_artist);

                }

            }
            catch (IOException e)
            {
                Log.e(MetadataTask2.class.toString(), e.getMessage());
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }*/
    public static Intent getOpenFacebookIntent(Context context) {

        try {
            context.getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0); //Checks if FB is even installed.
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/radyofirat89.2")); //Trys to make intent with FB's URI
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/radyofirat89.2")); //catches and opens a url to the desired page
        }
    }

    public static Intent getOpenTwitterIntent(Context context) {

        try {
            context.getPackageManager()
                    .getPackageInfo("com.twitter.android", 0); //Checks if Twitter is even installed.
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/firatradyo")); //Trys to make intent with Twitter's's URI
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/firatradyo")); //catches and opens a url to the desired page
        }
    }

    public static Intent getOpenInstagramIntent(Context context) {

        try {
            context.getPackageManager()
                    .getPackageInfo("com.instagram.android", 0); //Checks if Instagram is even installed.
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/firatradyo/")); //Trys to make intent with Instagram's URI
        } catch (Exception e) {
            return new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/firatradyo/")); //catches and opens a url to the desired page
        }
    }


    /*public String CharConvert(String str){
        str = str.replace("Ã„", "Ä");
        str = str.replace("Ã…", "Å");
        str = str.replace("Â", "");
        str = str.replace("ƒ", "");
        str = str.replace("Åž", "Ş");
        str = str.replace("ÅŸ", "ş");
        str = str.replace("ÄŸ", "ğ");
        str = str.replace("Äž", "Ğ");
        str = str.replace("Ã‡", "Ç");
        str = str.replace("Ã§", "ç");
        str = str.replace("Ã¼", "ü");
        str = str.replace("Ãœ", "Ü");
        str = str.replace("Ã¶", "ö");
        str = str.replace("Ã–", "Ö");
        str = str.replace("Ä±", "ı");
        str = str.replace("Ä°", "İ");
        str = str.replace("_", " ");
        return str;
    }*/
    @Override
    public void onBackPressed() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Radyo Fırat");
            alertDialogBuilder
                    .setMessage("Çıkmak istiyor musunuz?")
                    .setCancelable(false)
                    .setPositiveButton("Evet",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    moveTaskToBack(true);
                                    mRadioManager.disconnect();
                                    finish();

                                }
                            })

                    .setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        /*if (!userPressedBackAgain) {
            Toast.makeText(this, "Çıkmak için tekrar geri tuşuna basın.", Toast.LENGTH_SHORT).show();
            userPressedBackAgain = true;
        } else {
            onBackPressed();
        }

        new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                userPressedBackAgain = false;
            }
        }.start();*/

    }
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
