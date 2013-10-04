package com.matt.vehiclecheck.activity;

import java.util.Vector;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.matt.vehiclecheck.R;

public class CheckBoxListActivity extends ListActivity {

	private CheckListTask checkTask = null;
	private CheckboxListAdapter adapter;
	private EditText vnEditView;
	private EditText smEditView;
	private EditText emEditView;
	private SharedPreferences preferences;

	private final String NAMESPACE = "http://service.web.vehicle.events.logik.com/";
	private final String URL = "http://88.208.205.225:8080/vehiclecheck/ws/soapservice?wsdl";
	private final String SOAP_ACTION = "http://service.web.vehicle.events.logik.com/getVehicleByRegNo";
	private final String METHOD_NAME = "getVehicleByRegNo";
	private String questionId = "";
	private String answers = "" ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_check_list);

		vnEditView = (EditText) findViewById(R.id.vnEditView);
		smEditView = (EditText) findViewById(R.id.smEditView);
		emEditView = (EditText) findViewById(R.id.emEditView);

		retreiveCheckList();
		findViewById(R.id.confirmbutton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String vNumber = vnEditView.getText().toString();
						String vehicleId = getVehicleByRegNo(vNumber);
						if (vehicleId == null) {
							AlertDialog.Builder alertDialog = new AlertDialog.Builder(
									CheckBoxListActivity.this);
							alertDialog
									.setMessage("Vehicle Regestration number is invalid.");
							alertDialog.setTitle("Alert");
							alertDialog.setNegativeButton("Close",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});

							alertDialog.show();
						} else {
							// userId-1 vehicleId-2 companyId-3 startMileage-4
							// endMileage-5 questionId[]-6 answer[]-7
							// vehicleId-2 mStartMileageString-4
							// mEndMileageString-5
							preferences = PreferenceManager
									.getDefaultSharedPreferences(CheckBoxListActivity.this);
							String companyId = preferences.getString(
									"companyId", "null");// -3
							String userId = preferences.getString("userId",
									"null");// -1

							String mStartMileageString = smEditView.getText()
									.toString();
							String mEndMileageString = emEditView.getText()
									.toString();
							Log.e("Vehicle Number:", vNumber, null);
							Log.e("Start Mileage:", mStartMileageString, null);
							Log.e("End Mileage:", mEndMileageString, null);
							for (int i = 0; i < adapter.getCount(); i++) {
								CheckListData sampleData = (CheckListData) adapter
										.getItem(i);
								if(i>0){
									questionId += "-"+sampleData.getQuestionId();// -6
									answers += "-"+sampleData.isSelected();// -7
								} else {
									questionId += sampleData.getQuestionId();// -6
									answers += sampleData.isSelected();// -7
								}
								
							}
							
							saveCheckList(userId, vehicleId, companyId, mStartMileageString, mEndMileageString, questionId, answers);
							questionId = "";
							answers = "";
						}
					}
				});

	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class CheckListTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return checkListAdapter();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			getListView().setAdapter(adapter);
			checkTask = null;
		}

		@Override
		protected void onCancelled() {
			checkTask = null;
		}
	}

	public Boolean checkListAdapter() {
		adapter = new CheckboxListAdapter(getLayoutInflater(), getBaseContext());
		return true;
	}

	public void retreiveCheckList() {
		if (checkTask != null) {
			return;
		} else {
			checkTask = new CheckListTask();
			checkTask.execute((Void) null);
		}

	}

	private PropertyInfo regNoProp;
	private SoapObject request;

	private String getVehicleByRegNo(String regNo) {
		String vehicleId = null;
		request = new SoapObject(NAMESPACE, METHOD_NAME);

		regNoProp = new PropertyInfo();
		regNoProp.setName("regNo");
		regNoProp.setValue(regNo);
		regNoProp.setType(String.class);
		request.addProperty(regNoProp);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapObject vehicle = (SoapObject) envelope.getResponse();
			// anyType{id=41; name=abc; regNo=abc; }
			Object property = vehicle.getProperty("id");
			vehicleId = property.toString();

			Log.i("myApp", vehicle.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return vehicleId;
	}

	// userId-1 vehicleId-2 companyId-3 startMileage-4 endMileage-5
	// questionId[]-6 answer[]-7
	private PropertyInfo userIdProp;
	private PropertyInfo vehicleIdProp;
	private PropertyInfo companyIdProp;
	private PropertyInfo startMileageProp;
	private PropertyInfo endMileageProp;
	private PropertyInfo questionIdProp;
	private PropertyInfo answersProp;
	
	private final String _NAMESPACE = "http://service.web.vehicle.events.logik.com/";
	private final String _URL = "http://88.208.205.225:8080/vehiclecheck/ws/soapservice?wsdl";
	private final String _SOAP_ACTION = "http://service.web.vehicle.events.logik.com/checkOut";
	private final String _METHOD_NAME = "checkOut";
	
	private void saveCheckList(String userId, String vehicleId, String companyId, String startMileage, String endMileage, String questionId, String answer) {
		request = new SoapObject(_NAMESPACE, _METHOD_NAME);

		userIdProp = new PropertyInfo();
		userIdProp.setName("userId");
		userIdProp.setValue(userId);
		userIdProp.setType(String.class);
		request.addProperty(userIdProp);
		
		vehicleIdProp = new PropertyInfo();
		vehicleIdProp.setName("vehicleId");
		vehicleIdProp.setValue(vehicleId);
		vehicleIdProp.setType(String.class);
		request.addProperty(vehicleIdProp);
		
		companyIdProp = new PropertyInfo();
		companyIdProp.setName("companyId");
		companyIdProp.setValue(companyId);
		companyIdProp.setType(String.class);
		request.addProperty(companyIdProp);
		
		startMileageProp = new PropertyInfo();
		startMileageProp.setName("startMileage");
		startMileageProp.setValue(startMileage);
		startMileageProp.setType(String.class);
		request.addProperty(startMileageProp);
		
		endMileageProp = new PropertyInfo();
		endMileageProp.setName("endMileage");
		endMileageProp.setValue(endMileage);
		endMileageProp.setType(String.class);
		request.addProperty(endMileageProp);
		
		questionIdProp = new PropertyInfo();
		questionIdProp.setName("questionId");
		questionIdProp.setValue(questionId);
		questionIdProp.setType(String.class);
		request.addProperty(questionIdProp);
		
		answersProp = new PropertyInfo();
		answersProp.setName("answer");
		answersProp.setValue(answer);
		answersProp.setType(String.class);
		request.addProperty(answersProp);
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(_URL);
		try {
			androidHttpTransport.call(_SOAP_ACTION, envelope);
			String result =  envelope.getResponse().toString();
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					CheckBoxListActivity.this);
			if("true".equals(result)){
				result = "Your Checklist sent.";
			} else {
				result = "Problem occured while proccesing your request.";
			}

			alertDialog
			.setMessage(result);
			alertDialog.setTitle("Alert");
			alertDialog.setNegativeButton("Close",
					new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog,
								int which) {
							dialog.cancel();
						}
					});
			alertDialog.show();


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}