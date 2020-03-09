package securityteam.ece.uowm;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PacketDisplayActivity extends AppCompatActivity {

    ArrayList<Packet> packetList = new ArrayList<Packet>();
    Button showPackets;
    TableLayout table_layout;
    String command = "";
    private Capture_Root captureroot;
    Boolean haveFileReaded = false;
    WeakReference<Activity> activityreference;
    ArrayList<View> views = new ArrayList<>();
    UsersAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet_display3);
        activityreference = new WeakReference<Activity>(this);
        Capture_Root capture_root = new Capture_Root(activityreference);


        captureroot = capture_root;
        showPackets = findViewById(R.id.showPacket);
//        table_layout = findViewById(R.id.tableLayout);


        command += "su -c " + Capture_Root.tcpdump.getAbsolutePath() + " -qns 0 -r " + Capture_Root.captureLocation + "/capture.pcap";

        if(savedInstanceState==null){
            new reader().execute();
        }
        else showPackets.setEnabled(true);

//        showPackets.setOnClickListener(v -> buildTable(10));
        adapter = new UsersAdapter(this,packetList);
        ((ListView)findViewById(R.id.listView)).setAdapter(adapter);

    }

    void buildTable(int packets){
        int loopTimes;

        if(packets>packetList.size())
            loopTimes = packetList.size();
        else {
            loopTimes = packets;
        }


        //Packet Loop
        for(int i=0;i<loopTimes;i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView num = new TextView(this);
            TextView src = new TextView(this);
            TextView dest = new TextView(this);
            TextView protocol = new TextView(this);

            num.setText(String.valueOf(i));
            src.setText(packetList.get(i).getSource());
            dest.setText(packetList.get(i).getDestination());
            protocol.setText(packetList.get(i).getProtocol());
            num.setPadding(10, 0, 10, 0);
            src.setPadding(10, 0, 10, 0);
            dest.setPadding(10, 0, 10, 0);
            protocol.setPadding(10, 0, 10, 0);

            row.addView(num);
            row.addView(src);
            row.addView(dest);
            row.addView(protocol);

//            .addView(row);





        }
    }


    private class TableBuilder extends AsyncTask<Integer, Integer, String> {
        TableBuilder() {
        }

        @Override
        protected String doInBackground(Integer[] objects) {
//            buildTable(objects[0]);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            //setFinish(result);
        }

        }

    private class reader extends AsyncTask<Object, Object, String> {
         WeakReference<Activity> weakReference = null;
        reader() {
            weakReference = activityreference;
        }

        @Override
        protected String doInBackground(Object[] objects) {
            String[] lines = null;
            try {
                lines = ReadFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("msg","Do back ground started");
            if (lines != null) {
                Packet p =null;
                for (int i = 1; i < lines.length; i++) {
                    //Log.d("Line",lines[i]);
                    String[] data = lines[i].split(" ");
                    if(data.length>=6){
                        p = new Packet(i,data[5], data[2], data[4]);
                        float prog = (i/(float)(lines.length-1))*100;
                        publishProgress((int)prog,i,p);
                    }
                }

            }
            return "Progress finished successful";
        }

        protected void onProgressUpdate(Object... progress) {
            setProgressPercent((int)progress[0]);
            adapter.add((Packet) progress[2]);
        }

        protected void onPostExecute(String result) {
            setFinish(result);
        }

    }


    private String[] ReadFile() throws IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A");
        String[] lines = (s.hasNext() ? s.next() : "").split("\n");
        return lines;
    }

    void setProgressPercent(int prog){
        TextView t = (TextView) findViewById(R.id.textView6);
        t.setText(prog+"%");
    }

    void setFinish(String result){
        TextView t = findViewById(R.id.textView7);
        t.setText(result);
        haveFileReaded = true;
        showPackets.setEnabled(true);
    }

}

