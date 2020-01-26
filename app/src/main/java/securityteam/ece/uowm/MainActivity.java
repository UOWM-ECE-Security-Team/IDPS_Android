package securityteam.ece.uowm;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    WeakReference<Activity> activityreference ;
    Button start_button;
    Button stop_button;
    TextView captureCount;
    Capture_Root captureroot;
    tcpdumpExecutor exec = new tcpdumpExecutor();
    Capture_Root capture_root;
    PieChart pieChart ;
    final Object lock = new Object();
    volatile  List<PieEntry> entries = new ArrayList<>();
    List<Integer> colors = new ArrayList<>();
    AsyncTask a;
//    ReadWriteLock lock = new ReadWriteLock();



    private class test extends AsyncTask<Object,Object,Object>{
        float calculatePercentage ( int count, int total){
            if (total == 0) return 0.0f;
            return (((float) count / total) * 100.0f);
        }
        String initMsg = "Waiting for the first few packets";
        PieDataSet pieDataSet;

        PieChart pieChart = activityreference.get().findViewById(R.id.pieChart);
        test(){
            pieChart.setCenterTextSize(20);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(40);
            pieChart.setDrawEntryLabels(false);
            pieChart.setEntryLabelTextSize(16f);
            pieChart.setTouchEnabled(true);

            pieChart.setUsePercentValues(false);
            pieChart.setTransparentCircleRadius(50);


            Legend legend = pieChart.getLegend();
            legend.setTextSize(18);
            legend.setEnabled(true);
            legend.setWordWrapEnabled(true);
            legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        }

        @Override
        protected Object doInBackground (Object[]objects){
            boolean f;

            while (true) {
                if (Thread.currentThread().isInterrupted()) break;
                if (this.isCancelled()) break;


                PieEntry tmp;
                for (int i = 0; i < exec.protocolCount.length; i++) {
                    if (exec.protocolCount[i] > 0 && exec.getPacketCount() > 0) {
//                        PieEntry tmp = new PieEntry(calculatePercentage(exec.protocolCount[i], exec.getPacketCount()), exec.protocolNames[i]);
                        tmp = new PieEntry(exec.protocolCount[i], exec.protocolNames[i]);

//                        Log.d("NEWENTRY", "CREATED ENTRY " + tmp.getLabel() + "PERCENTAGE " + tmp.getValue() + " TOTAL PACKETS " + exec.getPacketCount());
                        f = false;

                        synchronized (lock) {
                            for (PieEntry entry : entries) {
                                if (entry.getLabel().equals(tmp.getLabel())) {
                                    entries.set(entries.indexOf(entry), tmp);
                                    f = true;
                                }
                            }
                            if (!f) entries.add(tmp);
                        }
                    }

                }


                publishProgress();
                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate (Object[]values){
            super.onProgressUpdate(values);
//            captureCount.setText(String.valueOf(exec.getPacketCount()));

            if (Thread.currentThread().isInterrupted()) return;
            if (this.isCancelled()) return;


                pieDataSet = new PieDataSet(entries, "");

                pieDataSet.setDrawValues(false);
//                pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                pieDataSet.setSliceSpace(2);
//                int[] arr = colors.stream().mapToInt(i -> i).toArray();
                pieDataSet.setColors(Ints.toArray(colors));

                PieData data = new PieData(pieDataSet);
                pieChart.setData(data);



                if (exec.captureCount == 0){
                    pieChart.setCenterTextSize(16);
                    if (pieChart.getCenterText().toString().equals(initMsg))pieChart.setCenterText(initMsg+System.getProperty("line.separator")+".");
                    else if (pieChart.getCenterText().toString().equals(initMsg+System.getProperty("line.separator")+"."))pieChart.setCenterText(initMsg+System.getProperty("line.separator")+"..");
                    else if (pieChart.getCenterText().toString().equals(initMsg+System.getProperty("line.separator")+".."))pieChart.setCenterText(initMsg+System.getProperty("line.separator")+"...");
                    else pieChart.setCenterText(initMsg+System.getProperty("line.separator")+".");
                }
                else {
                    pieChart.setCenterTextSize(20);
                    pieChart.setCenterText("Packets:"+System.getProperty("line.separator")+exec.getPacketCount());
                }

                pieChart.getDescription().setEnabled(false);
                pieChart.notifyDataSetChanged();
                pieChart.invalidate(); // refresh
            }
//        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityreference = new WeakReference<Activity>(this);
        capture_root = new Capture_Root(activityreference);
        captureroot = capture_root;
        start_button = findViewById(R.id.start_tcpdump);
        stop_button = findViewById(R.id.stop_tcpdump);
//        captureCount = ((TextView) findViewById(R.id.textView));
        pieChart = findViewById(R.id.pieChart);
        pieChart.setVisibility(View.GONE);




        colors.add(ContextCompat.getColor(this, R.color.crimson));
        colors.add(ContextCompat.getColor(this, R.color.orange));
        colors.add(ContextCompat.getColor(this, R.color.sea_green));
        colors.add(ContextCompat.getColor(this, R.color.royal_blue));
        colors.add(ContextCompat.getColor(this, R.color.slate_blue));
//        final PieChart pieChart = findViewById(R.id.pieChart);
//        PieDataSet pieDataSet;
//        List<PieEntry> dummy = new ArrayList<>();
//        dummy.add(new PieEntry(1,""));
//        pieDataSet = new PieDataSet(dummy, "");
//        PieData data = new PieData(pieDataSet);
//
//
//
//        pieDataSet.setSliceSpace(5);
//        pieChart.getDescription().setEnabled(false);
//        pieDataSet.setColors(new int[]{ R.color.light_blue}, this);
//        pieChart.setCenterTextSize(20);
//        pieChart.setDrawHoleEnabled(true);
//        pieChart.setHoleRadius(40);
//        pieChart.setDrawEntryLabels(false);
//        pieChart.setEntryLabelTextSize(16f);
//        pieChart.setTouchEnabled(true);
//        pieDataSet.setDrawValues(false);
//        pieChart.setUsePercentValues(false);
//        pieChart.setTransparentCircleRadius(50);
//        pieChart.
//
//        Legend legend = pieChart.getLegend();
//        legend.setTextSize(18);
//        legend.setEnabled(false);
//        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
//        pieChart.setData(data);
//        pieChart.notifyDataSetChanged();
//        pieChart.invalidate();
//        pieChart.invalidate(); // refresh
//
//
//

        CheckBox all=findViewById(R.id.checkBoxAll);

        all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CheckBox all,tcp,udp,icmp;
                tcp=findViewById((R.id.checkBoxTcp));
                udp=findViewById((R.id.checkBoxUdp));
                icmp=findViewById((R.id.checkBoxIcmp));

                if(b==true){
                    tcp.setChecked(false);
                    tcp.setEnabled(false);

                    udp.setChecked(false);
                    udp.setEnabled(false);

                    icmp.setChecked(false);
                    icmp.setEnabled(false);
                }
                else{

                    tcp.setEnabled(true);


                    udp.setEnabled(true);


                    icmp.setEnabled(true);
                }


            }
        });






        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            exec.protocolCount = new int[255];
                            entries.clear();
                            PieDataSet pieDataSet = new PieDataSet(entries, "");
                            pieDataSet.setDrawValues(false);
                            pieDataSet.setSliceSpace(2);
                            pieDataSet.setColors(colors);
                            PieData data = new PieData(pieDataSet);
                            pieChart.getDescription().setEnabled(false);

                            pieChart.setData(data);
//                pieChart.clear();
                            pieChart.notifyDataSetChanged();
                            pieChart.invalidate();
                            Runtime.getRuntime().exec("su");
                            activityreference.get().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    start_button.setEnabled(false);
                                    stop_button.setEnabled(true);
                                    pieChart.setVisibility(View.VISIBLE);
                                }
                            });
                            UpdateSettings();
                            Log.e("SUCHECK","SUCCESS. Executing: " + capture_root.getCaptureCommand());

                            exec.executeCommand(capture_root.getCaptureCommand());


                        } catch (IOException e) {
                            Log.e("SUCHECK","FAIL");
                            e.printStackTrace();
                            activityreference.get().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activityreference.get(),"Root access not detected or not allowed.",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
                t.start();

                a = new test().execute();
            }
        });
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                stop_button.setEnabled(false);
                start_button.setEnabled(true);
                exec.stopRunningExecution();
                a.cancel(true);
                pieChart.setCenterText("Ready to capture.");
//                pieChart.setVisibility(View.GONE);
                exec.captureCount =0;
                pieChart.invalidate();




            }
        });
    }

    void UpdateSettings(){

        CheckBox all,tcp,udp,icmp;
        all=findViewById(R.id.checkBoxAll);
        tcp=findViewById((R.id.checkBoxTcp));
        udp=findViewById((R.id.checkBoxUdp));
        icmp=findViewById((R.id.checkBoxIcmp));

        int countChecks=0;

        String options = "";


        if(tcp.isChecked()){
            options+=" tcp";
            countChecks++;
        }
        if(udp.isChecked()){
            if(countChecks>0)
                options+=" or udp";
            else {
                options += " udp";
            }
            countChecks++;
        }
        if(icmp.isChecked()){

            if(countChecks>0)
                options+=" or icmp";
            else {
                options+=" icmp";
            }
            countChecks++;
        }

        EditText ed =  findViewById(R.id.host_text);

        if(ed.getText().toString().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")){
            options+=" host " +ed.getText().toString();
        }

        if(captureroot!=null)
            captureroot.updateCaptureCommand(options);

    }









}
