package util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


/**
 * Created by willian on 15/04/2016.
 */

public class UtilConnection {
    private static String pathApplicationRequest = "http://192.168.0.168:8080/SlumServer/slum/";

    private static boolean isInternetAvalaible(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private static String converterInputStreamToString(InputStream is) throws IOException {
        StringBuffer buffer = new StringBuffer();

            BufferedReader br;
            String linha;
            br = new BufferedReader(new InputStreamReader(is));
            while((linha = br.readLine())!=null){
                buffer.append(linha);
            }
            br.close();

        return buffer.toString();
    }

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static String buildRequest(String urlResource, String metodo, List<NameValuePair> parametros, Context context) throws IOException {
        /*
        Verifica se o device está conectado a rede
         */
        if (!isInternetAvalaible(context)) {
            return "errorConnection";
        }


        URL url = new URL(pathApplicationRequest + urlResource);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream is = null;

        try {
            conn.setReadTimeout(30000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod(metodo);
            conn.setDoInput(true);
            conn.setDoOutput(true);/* define if have data to posting*/
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "Accept", "*/*" );
            if("GET".equals(metodo)){

            }else {

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(parametros));
                writer.flush();
                writer.close();
                os.close();
            }

            // Starts the request
            conn.connect();
            Log.d("HoraCerta", "The response is: " + conn.getResponseCode());
                if(conn.getResponseCode() == 200) {
                    is = conn.getInputStream();
                    return converterInputStreamToString(is);
                }else if(conn.getResponseCode() == 401){
                    return "errorAutenticacao";
                }else {
                    return "errorResponse";
                }
        } finally {

            if(is != null) {
                is.close();
            }

            conn.disconnect();
        }
    }
}
