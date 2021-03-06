package shank.shank.com.audiofrequency;


import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;


public class MainActivity extends Activity {


    TextView disp;
    private static int[] sampleRate = new int[]{44100, 22050, 11025, 8000};
    short audioData[];
    double finalData[];
    int bufferSize, srate;
    String TAG;
    public boolean recording;
    AudioRecord recorder;
    Complex[] fftArray;
    float freq;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disp = (TextView) findViewById(R.id.display);


    }


    public void onRecord(View v) {
        disp.setVisibility(View.VISIBLE);
        Thread t1 = new Thread(new Runnable() {

            public void run() {

                Log.i(TAG, "Setting up recording");
                for (int rate : sampleRate) {
                    try {

                        Log.d(TAG, "Attempting rate " + rate);

                        bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT) * 3; //get the buffer size to use with this audio record
                        Log.d(TAG, "Attempting rate BufferSize " + bufferSize);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {

                            recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, AudioFormat.CHANNEL_IN_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT, 2048); //instantiate the AudioRecorder
                            Log.d(TAG, "BufferSize " + bufferSize);
                            srate = rate;

                        }

                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.", e);
                    }
                }
                bufferSize = 2048;
                recording = true; //variable to use start or stop recording
                audioData = new short[bufferSize]; //short array that pcm data is put into.
                Log.e(TAG, "Got buffer size =" + bufferSize);
                while (recording) {  //loop while recording is needed
                    //  Log.i(TAG,"in while 1"+recorder.getState()+" "+android.media.AudioRecord.STATE_INITIALIZED);
                    if (recorder.getState() == android.media.AudioRecord.STATE_INITIALIZED) {// check to see if the recorder has initialized yet.
                        Log.i(TAG, "in Ini");
                        if (recorder.getRecordingState() == android.media.AudioRecord.RECORDSTATE_STOPPED) {
                            Log.i(TAG, "in Rec");
                            recorder.startRecording();  //check to see if the Recorder has stopped or is not recording, and make it record.
                        } else {
                            Log.i(TAG, "in else");

                            finalData = convert_to_double(audioData);
                            Findfft();
                            for (int k = 0; k < fftArray.length; k++) {
                                freq = ((float) srate / (float) fftArray.length) * (float) k;


                                if (freq >= 5000) {
                                    registerOnServer(String.valueOf(freq));
                                    recording = false;
                                    break;
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {

                                            if (freq < 5000) {
                                                disp.setText("The frequency is " + freq);
                                            }


                                        }
                                    });
                                }


                            }


                        }//else recorder started
                    }

                } //while recording

                if (recorder.getState() == android.media.AudioRecord.RECORDSTATE_RECORDING)
                    recorder.stop(); //stop the recorder before ending the thread
                recorder.release(); //release the recorders resources
                recorder = null; //set the recorder to be garbage collected.
            }//run

        });
        t1.start();
    }

    private void Findfft() {
        // TODO Auto-generated method stub
        Complex[] fftTempArray = new Complex[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            fftTempArray[i] = new Complex(finalData[i], 0);
        }
        fftArray = FFT.fft(fftTempArray);
    }


    private double[] convert_to_double(short data[]) {
        // TODO Auto-generated method stub
        double[] transformed = new double[data.length];

        for (int j = 0; j < data.length; j++) {
            transformed[j] = (double) data[j];
        }

        return transformed;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void registerOnServer(final String fre) {
        new AsyncTask<String, Integer, String>() {

            String resource = "/api/record";

            CallNetwork cn;

            protected void onPreExecute() {
                disp.setText("Maximum frequency reached! Sending data to server..");
            }

            @Override
            protected String doInBackground(String... params) {
                cn = new CallNetwork();

                try {

                    cn.postFields = new HashMap<String, String>();
                    cn.postFields.put("fre", fre);


                } catch (Exception e) {
                    e.printStackTrace();
                }

                cn.makeNetworkCall(resource, "POST");

                if (cn.responseString != null) {
                    Log.v("Response", cn.responseString);

                    return "recorded";

                } else {
                    Log.v("Response", "Null");
                }

                return null;
            }

            protected void onPostExecute(String result) {
              
                    disp.setText("The frequency is registered on server! ");

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

    }
}