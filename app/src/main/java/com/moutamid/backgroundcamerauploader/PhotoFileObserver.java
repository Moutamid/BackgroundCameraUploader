package com.moutamid.backgroundcamerauploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class PhotoFileObserver extends FileObserver {
// Add URL
    private static final String ROOT_URL = "";
    private static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";

    private static final String CAMERA_IMAGE_BUCKET_ID =
            String.valueOf(CAMERA_IMAGE_BUCKET_NAME.toLowerCase().hashCode());

    private Context mContext;

    String type;

    public PhotoFileObserver(Context context) {
        super(CAMERA_IMAGE_BUCKET_NAME, FileObserver.CREATE);
        mContext = context;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        if (event == FileObserver.CREATE && path != null) {
            // Check if the created file is an image file
            if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".png")) {
                String[] pp = path.split("-");
                // A new photo has been taken!
                Log.d("CameraApp", "New photo taken: " + path);
                Log.d("CameraApp", "ll photo taken: " + pp[pp.length - 1]);
                // Get the new image file
                File newImageFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM) + "/Camera/" + pp[pp.length - 1]);

                type = pp[pp.length - 1];
                Log.d("CameraApp", "t: " + type);

                String filePath = newImageFile.getAbsolutePath();



                Log.d("CameraApp", "filePath: " + filePath);
//                Uri uri = Uri.fromFile(new File(filePath));
//                uploadBitmap(uri);

                File file = new File(filePath);

                OkHttpClient client = new OkHttpClient.Builder()
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();


                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(8080);
                    serverSocket.setSoTimeout(12000);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id", "PD9L0YBKIO6WUFG8VME47XYZ_test_by_moutamid")
                        .addFormDataPart("key", "2d53ecdbbacf09343fe99a147929af9e")
                        .addFormDataPart("url", "http://foo.com")
                        .addFormDataPart("app", "Chrome")
                        .addFormDataPart("image", file.getName(), RequestBody.create(file, MediaType.parse("image/*")))
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(ROOT_URL)
                        .post(requestBody)
                        .build();



// Send the HTTP request and handle the response as needed
                try {
                    okhttp3.Response response = client.newCall(request).execute();
                    Log.d("CameraApp", "response: " +response.networkResponse().code());
                    // Handle the response
                } catch (IOException e) {
                    // Handle the exception
                    e.printStackTrace();
                }


            }
        }
    }

    private void uploadBitmap(final Uri bitmap) {

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, ROOT_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            //Toast.makeText(mContext, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            Log.d("CameraApp", obj.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show()

                        if (error == null || error.networkResponse == null) {
                            Log.e("CameraApp", "Error : " + error.getMessage());
                            error.printStackTrace();
                            return;
                        }

                        String body = "";
                        //get status code here
                        final String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        try {
                            body = new String(error.networkResponse.data,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            // exception
                        }
                        Log.e("CameraApp", "Body : " + body + " Status Code : " + statusCode);
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                String t = "";
                if (type.toLowerCase().endsWith(".png")){
                    t = ".png";
                } else if (type.toLowerCase().endsWith(".jpg")){
                    t = ".jpg";
                }
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        volleyMultipartRequest.setRetryPolicy(
                new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 50000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 50000;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {

                    }
                }
        );

        //adding the request to volley
        Volley.newRequestQueue(mContext).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Uri imageUri) {

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//        if (type.toLowerCase().endsWith(".png")){
//            bmp.compress(Bitmap.CompressFormat.PNG, 80, baos);
//        } else if (type.toLowerCase().endsWith(".jpg")){
//            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
//        }

        bmp.compress(Bitmap.CompressFormat.PNG, 80, baos);

        byte[] fileInBytes = baos.toByteArray();

        return fileInBytes;
    }

    // Upload the image file to the API
                /*try {
                    // Open a connection to the API endpoint
                    URL url = new URL(ROOT_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    // Create the request body
                    String boundary = "---------------------------" + System.currentTimeMillis();
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    OutputStream outputStream = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; id=\"PD9L0YBKIO6WUFG8VME47XYZ_test_by_moutamid\"; key=\"2d53ecdbbacf09343fe99a147929af9e\"; url=\"http://foo.com\"; app=\"Chrome\"; image=\"" + newImageFile.getName() + "\"").append("\r\n");
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(newImageFile.getName())).append("\r\n");
                    writer.append("\r\n");
                    writer.flush();

                    // Write the image file to the request body
                    FileInputStream inputStream = new FileInputStream(newImageFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    inputStream.close();

                    // Finish the request body
                    writer.append("\r\n").flush();
                    writer.append("--" + boundary + "--").append("\r\n");
                    writer.close();

                    // Read the response from the API
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // The image file was uploaded successfully!
                        Log.d("CameraApp", "Image uploaded successfully!"  + connection.getResponseMessage() + " Respond Code " + responseCode);
                    } else {
                        // There was an error uploading the image file
                        Log.e("CameraApp", "responseCode: " + responseCode);
                        Log.e("CameraApp", "Error uploading image: " + connection.getErrorStream().toString());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

}
