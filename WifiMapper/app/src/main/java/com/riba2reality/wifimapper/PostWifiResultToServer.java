package com.riba2reality.wifimapper;

import android.net.wifi.ScanResult;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class PostWifiResultToServer extends AsyncTask<String, String, String> {

    TrackerScanner _trackerScanner;

    WifiScanResult wifiScanResult;

    public PostWifiResultToServer(TrackerScanner trackerScanner){

        _trackerScanner = trackerScanner;
    }

    public InputStream is;

//    LocalBroadcastManager broadcaster;
//
//    static final public String POST_TO_SERVER_RESULT = "com.riba2reality.wifimapper.PostToServer.REQUEST_PROCESSED";
//
//    static final public String POST_TO_SERVER_MESSAGE = "com.riba2reality.wifimapper.PostToServer.POST_TO_SERVER_MSG";
//
//    public void sendResult(String message) {
//        Intent intent = new Intent(POST_TO_SERVER_RESULT);
//        if(message != null)
//            intent.putExtra(POST_TO_SERVER_MESSAGE, message);
//        broadcaster.sendBroadcast(intent);
//    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected  void onPostExecute(String result){
        super.onPostExecute(result);

        if(_trackerScanner!=null){
            _trackerScanner.sendResult(result);
        }

    }

    @Override
    protected String doInBackground(String... params) {

        final String address = params[0];

        String protocol = params[1];

        String useSSLString = params[2];
        boolean useSSL = Boolean.valueOf(useSSLString);

        String deviceID = params[3];

        String dataBase = params[4];

        String port = "";

        final String endpoint = "/wifi/";

        String urlString = protocol + "://" + address + port + endpoint;

        //------------------------------------------------------------------
        // check if wifiScanResult null


        //------------------------------------------------------------------

        List<String> macAddressList = new ArrayList<>();
        List<String> signalStrengths = new ArrayList<>();

        for (WifiResult wifiResult : wifiScanResult.wifiResult) {

            macAddressList.add(wifiResult.macAddress);
            signalStrengths.add(Integer.toString(wifiResult.signalStrength));
        }

        //------------------------------------------------------------------
        // build message...

        Map<String, String> parameters = new HashMap<>();


        parameters.put("MAGIC_NUM",Constants.verificationCode);

        parameters.put("UUID",deviceID);

        parameters.put("DATABASE",dataBase);

        parameters.put("TIME",wifiScanResult.dateTime);

//        parameters.put("X",Double.toString(latitude));
//        parameters.put("Y",Double.toString(longitude));
//        parameters.put("ALTITUDE",Double.toString(altitude));
//        parameters.put("ACC",Double.toString(accuracy));

        String macAddressJson = new Gson().toJson(macAddressList );


        //parameters.put("MacAddresses",macAddressList.toString());
        parameters.put("MacAddressesJson",macAddressJson);

        parameters.put("signalStrengthsJson",new Gson().toJson(signalStrengths ));


        String message = new JSONObject(parameters).toString();







        //------------------------------------------------------------------
        try {

            //------------------------------------------------------------------

            //System.out.println(System.getProperty("user.dir"));


            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt

            //InputStream caInput = new BufferedInputStream(new FileInputStream("cert.pem"));

            //InputStream is = this.getResources().openRawResource(R.raw.sample);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(is));




            //InputStream caInput = new BufferedInputStream(new FileInputStream("cert.pem"));
            InputStream caInput = new BufferedInputStream(this.is);




            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            //---------------------------------



            // Create an HostnameVerifier that hardwires the expected hostname.
            // Note that is different than the URL's hostname:
            // example.com versus example.org
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    //new StrictHostnameVerifier();

                    //return hv.verify(address, session);

                    //return hv.verify("192.168.0.10", session);


                    //return hv.verify("82.46.100.70", session);

                    //System.out.println(hostname);

                    if(
                        //hostname.equals("192.168.0.10") ||
                        //        hostname.equals("10.0.2.2")
                            hostname.equals(address)

                    )
                        return true;
                    else
                        return false;


                }
            };








            //---------------------------------
            BufferedReader reader = null;
            String uri = urlString;
            String method = "GET";
            //Map<String, String> parameters = new HashMap<>();

            method = "POST";

            //---------
            /*
            // dummy data

            List<String> macAddressList = new ArrayList<>();

            macAddressList.add("000.111.222.444");
            macAddressList.add("777");
            macAddressList.add("ABC");

            parameters.put("TIME","12:01");
            parameters.put("X","42");
            parameters.put("Y","7");

            String macAddressJson = new Gson().toJson(macAddressList );


            //parameters.put("MacAddresses",macAddressList.toString());
            parameters.put("MacAddressesJson",macAddressJson);


*/

            //---------


            if (method.equals("GET")) {
                uri += "?" + this.getEncodedParams(parameters);
                //As mentioned before, this only executes if the request method has been
                //set to GET
            }

            try {
                URL url = new URL(uri);
                //HttpURLConnection con = (HttpURLConnection) url.openConnection();

                URLConnection con;
                if (useSSL) {
                    con = (HttpsURLConnection) url.openConnection();
                    ((HttpsURLConnection)con).setRequestMethod(method);
                }else{
                    con = (HttpURLConnection) url.openConnection();
                    ((HttpURLConnection)con).setRequestMethod(method);
                }


                // set time outs
                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);

                if(useSSL) {
                    // set certificate
                    ((HttpsURLConnection)con).setSSLSocketFactory(context.getSocketFactory());

                    // set host name
                    ((HttpsURLConnection)con).setHostnameVerifier(hostnameVerifier);


                }


                if (method.equals("POST")) {
                    con.setDoOutput(true);
                    OutputStreamWriter writer =
                            new OutputStreamWriter(con.getOutputStream());
                    //writer.write(this.getEncodedParams(parameters));

                    //writer.write(new JSONObject(parameters).toString());
                    writer.write(message);

                    writer.flush();
                }

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;

                //System.out.println("blah");

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                    //System.out.println(line);
                }

                //System.out.println("blah...");




                return sb.toString();

            } catch (Exception e) {


                // put it back in the queue
                this._trackerScanner.wifiScanResultResendQueue.add(this.wifiScanResult);


                e.printStackTrace();
                //return null;
                return "Exception: "+e.getMessage();
            } finally {
                if (reader != null) {
                    try {

                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }




            //return "someting";
        } catch (Exception e) {

            // put it back in the queue
            this._trackerScanner.wifiScanResultResendQueue.add(this.wifiScanResult);

            System.out.println(e.getMessage());
            return "Exception: "+e.getMessage();
        }
    }// end of do in background method


    //The method below is only called if the request method has been set to GET
    //GET requests sends data in the url and it has to be encoded correctly in order
    //for the server to understand the request. This method encodes the data in the
    //params variable so that the server can understand the request

    public String getEncodedParams(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = null;
            //try {
            //value = URLEncoder.encode(params.get(key), "UTF-8");
            value = params.get(key);
            //} catch (UnsupportedEncodingException e) {
            //    e.printStackTrace();
            //}

            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key + "=" + value);
        }
        return sb.toString();
    }


}// end of class PostWifiResultToServer
