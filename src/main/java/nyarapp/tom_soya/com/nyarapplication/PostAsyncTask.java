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

public class PostAsyncTask extends AsyncTask<String,Void,String>{

    public PostAsyncTask() {
        super();
    }

    @Override
    protected String doInBackground(String... strings) {
        String status = "";
        Socket connection;
        String domain = "10.0.2.2";
        int port = 80;
        String path = "/post.php";
        String word = strings[0];
        try{
            connection = new Socket(domain,port);
            OutputStream out = null;
            InputStream in = null;
            try {
                out = connection.getOutputStream();
                in = connection.getInputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                writer.write("POST "+path+" HTTP/1.1\r\n");
                writer.write("Host: "+domain+":"+port+"\r\n");
                writer.write("User-Agent: "+"Dalvik/2.1.0 (Linux; U; Android 7.0; Android SDK built for x86 Build/NYC)\r\n");
                writer.write("Content-Length: "+word.length()+"\r\n");
                writer.write("\r\n");
                writer.write(word);
                writer.flush();

                status = reader.readLine().substring(9,12);
                //String s;
                //while((s = reader.readLine()) != null)                {
                 //   Log.d("nyar",s);
                //}
            }
            catch (IOException e){
                e.printStackTrace();
                Log.d(ARActivity.TAG,"Failed Post");
            }
            finally {
                if(out!=null && in!=null){
                    out.close();
                    in.close();
                }
                connection.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
            Log.d(ARActivity.TAG,"Connection Failed");
        }
        finally {
            return status;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d(ARActivity.TAG,"PostTask; Status="+s);
    }
}
