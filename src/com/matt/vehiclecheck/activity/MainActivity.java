package com.matt.vehiclecheck.activity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.matt.vehiclecheck.R;

public class MainActivity extends Activity implements OnClickListener {

	final int CAMERA_CAPTURE = 1;
	final int PIC_CROP = 2;
	private Uri picUri;
	private Bitmap numberPlatePic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		Button cameraBtn = (Button) findViewById(R.id.camera);
		cameraBtn.setOnClickListener(this);
		Button exitBtn = (Button) findViewById(R.id.exit);
		exitBtn.setOnClickListener(this);

		findViewById(R.id.check_vehicle).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						showCheckListActivity();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private void showCheckListActivity() {
		Intent checkBoxListActivityIntent = new Intent(MainActivity.this,
				CheckBoxListActivity.class);
		MainActivity.this.startActivity(checkBoxListActivityIntent);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.camera) {
			try {
				Intent captureIntent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(captureIntent, CAMERA_CAPTURE);
			} catch (ActivityNotFoundException anfe) {
				String errorMessage = "Whoops - your device doesn't support capturing images!";
				Toast toast = Toast.makeText(this, errorMessage,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		} else if (v.getId() == R.id.exit) {
			this.finish();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}

	}

	/**
	 * Handle user returning from both capturing and cropping the image
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == CAMERA_CAPTURE) {
				picUri = data.getData();
				performCrop();
			} else if (requestCode == PIC_CROP) {
				Bundle extras = data.getExtras();
				numberPlatePic = extras.getParcelable("data");
				Intent checkBoxListActivityIntent = new Intent(
						MainActivity.this, CheckBoxListActivity.class);
				MainActivity.this.startActivity(checkBoxListActivityIntent);
				new ImageUploadTask().execute();
			}
		}
	}

	/**
	 * Helper method to carry out crop operation
	 */
	private void performCrop() {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			cropIntent.setDataAndType(picUri, "image/*");
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, PIC_CROP);
		} catch (ActivityNotFoundException anfe) {
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast
					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	class ImageUploadTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... unsued) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				numberPlatePic.compress(CompressFormat.JPEG, 75, bos);
				byte[] data = bos.toByteArray();
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost(
						"http://172.16.1.66:8080/VCheckWebServices/ImageUpload?filename=NumberPlate"
								+ System.currentTimeMillis());
				ByteArrayBody bab = new ByteArrayBody(data, "NumberPlate.jpg");
				MultipartEntity reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("uploaded", bab);
				reqEntity.addPart("photoCaption", new StringBody(
						"Vehicle Number Plate Image"));
				postRequest.setEntity(reqEntity);
				HttpResponse response = httpClient.execute(postRequest);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String sResponse;
				StringBuilder stringBuilder = new StringBuilder();

				while ((sResponse = reader.readLine()) != null) {
					stringBuilder = stringBuilder.append(sResponse);
				}
				System.out.println("Response: " + stringBuilder);
			} catch (Exception e) {
				Log.e(e.getClass().getName(), e.getMessage());
			}

			return "doInBacground returned";

		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}

		@Override
		protected void onPostExecute(String sResponse) {
			try {
				// if (sResponse != null) {
				// JSONObject JResponse = new JSONObject(sResponse);
				// int success = JResponse.getInt("SUCCESS");
				// String message = JResponse.getString("MESSAGE");
				// if (success == 0) {
				// Toast.makeText(getApplicationContext(), message,
				// Toast.LENGTH_LONG).show();
				// } else {
				// Toast.makeText(getApplicationContext(),
				// "Photo uploaded successfully",
				// Toast.LENGTH_SHORT).show();
				// }
				// }
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.exception_message),
						Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}
	}

}
