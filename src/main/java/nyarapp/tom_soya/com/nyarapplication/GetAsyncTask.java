package nyarapp.tom_soya.com.nyarapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class GetAsyncTask extends AsyncTask<Void,Void,String> {

    interface Listener{
        void onSuccess(String str);
    }

    private Listener listener;
    final int HTTP_OK = 200;

    public GetAsyncTask() {
        super();
    }

    @Override
    protected String doInBackground(Void... voids) {
        Log.d(ARActivity.TAG,"Start Get AsyncTask");
        Socket connection;
        String domain = "10.0.2.2";
        int port = 80;
        String path = "/getText.json";
        String readSt = "";

        try{
            connection = new Socket(domain,port);
            OutputStream out = null;
            InputStream in = null;
            try {
                out = connection.getOutputStream();
                in = connection.getInputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                writer.write("GET "+path+" HTTP/1.1\r\n");
                writer.write("Host: "+domain+":"+port+"\r\n");
                writer.write("User-Agent:"+"NyARApplicaton/Android7.0\r\n");
                writer.write("\r\n");
                writer.flush();

                String status = reader.readLine().substring(9,12);
                if(Integer.parseInt(status)==HTTP_OK)
                {
                    Log.d(ARActivity.TAG,"Success Getting Data");
                    readSt = readInputStream(reader);
                }
                else {
                    Log.d(ARActivity.TAG,"Failed: Code="+status);
                }
                writer.close();
                reader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally {
                if(out!=null && in!=null)
                {
                    out.close();
                    in.close();
                }
                connection.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            return readSt;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d(ARActivity.TAG,"Post Execute");
        listener.onSuccess(s);
    }

    private String readInputStream(BufferedReader br)throws Exception
    {
        StringBuffer sb = new StringBuffer();
        String st;
        while((st = br.readLine())!=null && !st.isEmpty());
        while((st=br.readLine())!=null)
        {
            sb.append(st);
        }
            return sb.toString();
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }
}
