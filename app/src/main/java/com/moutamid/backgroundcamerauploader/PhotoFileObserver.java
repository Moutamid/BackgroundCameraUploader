package com.moutamid.backgroundcamerauploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PhotoFileObserver extends FileObserver {
// Add URL
    private static final String ROOT_URL = "";
    private static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";

    private static final String CAMERA_IMAGE_BUCKET_ID =
            String.valueOf(CAMERA_IMAGE_BUCKET_NAME.toLowerCase().hashCode());

    private Context mContext;

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

                String filePath = newImageFile.getAbsolutePath();

                Log.d("CameraApp", "filePath: " + filePath);
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                uploadBitmap(bitmap);
            }
        }
    }

    private void uploadBitmap(final Bitmap bitmap) {

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
                        //Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("CameraApp", "" + error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(mContext).add(volleyMultipartRequest);
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /*

    // Upload the image file to the API
                try {
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
                        Log.d("CameraApp", "Image uploaded successfully!"  + connection.getResponseMessage());
                    } else {
                        // There was an error uploading the image file
                        Log.e("CameraApp", "Error uploading image: " + connection.getResponseMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

    * */


}
