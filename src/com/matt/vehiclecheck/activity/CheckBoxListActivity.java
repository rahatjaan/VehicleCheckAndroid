package com.matt.vehiclecheck.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.matt.vehiclecheck.R;

public class CheckBoxListActivity extends ListActivity implements LocationListener {
	
	protected LocationManager locationManager;
	protected LocationListener locationListener;

	private CheckListTask checkTask;
	private AutoComplete autoComplete;
	private CheckboxListAdapter adapter;
	private AutoCompleteTextView vnACTextView;
	private TextView latitude;
	private TextView langitude;
	private TextView date;
	private SharedPreferences preferences;
	private final String GET_VEHICLE_BY_REG_NO = "getVehicleByRegNo";
	private final String CHECK_OUT = "checkOut";
	private final String GET_VEHICLES_BY_COMPANY_ID = "getVehicles";

	private final String NAMESPACE = "http://service.web.vehicle.events.logik.com/";
	private final String URL = "http://88.208.205.225:8080/vehiclecheck/ws/soapservice?wsdl";
	
	private final String CHECK_OUT_SOAP_ACTION = "http://service.web.vehicle.events.logik.com/"+CHECK_OUT;
	private final String VEHICLE_BY_REG_NO_SOAP_ACTION = "http://service.web.vehicle.events.logik.com/"+GET_VEHICLE_BY_REG_NO;
	private final String VEHICLES_BY_COMPANY_ID_SOAP_ACTION = "http://service.web.vehicle.events.logik.com/"+GET_VEHICLES_BY_COMPANY_ID;
	
	private String questionId = "";
	private String answers = "" ;
	private String companyId;
	private String userId;
	private List<String> listOfRegNo;
	private ArrayAdapter<String> adapterArray;
	//private final String SOAP_ACTION = "http://service.web.vehicle.events.logik.com/getVehicleByRegNo";
	//private final String METHOD_NAME = "getVehicleByRegNo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_check_list);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(CheckBoxListActivity.this);
		companyId = preferences.getString(
				"companyId", "null");// -3
		userId = preferences.getString("userId",
				"null");// -1
	    retreiveAutoCompleteTask();

	    vnACTextView = (AutoCompleteTextView) findViewById(R.id.vnEditView);
		
		

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		//vnEditView = (EditText) findViewById(R.id.vnEditView);
		latitude = (TextView) findViewById(R.id.latTextView2);
		latitude.setText("2345.323232");
		langitude = (TextView) findViewById(R.id.langTextView2);
		langitude.setText("434.786778");
		date = (TextView) findViewById(R.id.dateTextView2);
		date.setText(new Date().toString());
		retreiveCheckList();
		findViewById(R.id.home).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						finish();
						Intent homeIntent = new Intent(CheckBoxListActivity.this,
								MainActivity.class);
						CheckBoxListActivity.this.startActivity(homeIntent);
					}
				});
		findViewById(R.id.confirmbutton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String vNumber = vnACTextView.getText().toString();
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

//							String latitude = latitude.concat("asdff");
//							String mEndMileageString = emEditView.getText()
//									.toString();
//							Log.e("Vehicle Number:", vNumber, null);
//							Log.e("Start Mileage:", mStartMileageString, null);
//							Log.e("End Mileage:", mEndMileageString, null);
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
							
							saveCheckList(userId, vehicleId, companyId, latitude.getText().toString(), langitude.getText().toString(), questionId, answers);
							questionId = "";
							answers = "";
						}
					}
				});

	}

	
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
	
	public boolean getRegNoList(){
		listOfRegNo = getVehicleRegNoByCompanyId(companyId);
		adapterArray = new ArrayAdapter<String>(getApplicationContext(), 
		        android.R.layout.simple_list_item_1,listOfRegNo ) {
		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		        View view = super.getView(position, convertView, parent);
		        TextView text = (TextView) view.findViewById(android.R.id.text1);
		        text.setBackgroundResource(R.drawable.edit_text);
		        text.setTextColor(Color.BLACK);
		        return view;
		    }
		};
		return true;
	}

    public void retreiveAutoCompleteTask(){
    	if (autoComplete != null) {
			return;
		} else {
			autoComplete = new AutoComplete();
			autoComplete.execute((Void) null);
		}
    }
	
	public class AutoComplete extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return getRegNoList();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			vnACTextView.setAdapter(adapterArray);
			autoComplete = null;
		}

		@Override
		protected void onCancelled() {
			autoComplete = null;
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
		request = new SoapObject(NAMESPACE, GET_VEHICLE_BY_REG_NO);

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
			androidHttpTransport.call(VEHICLE_BY_REG_NO_SOAP_ACTION, envelope);
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
	
	private List<String> regNumbers;
	private List<String> getVehicleRegNoByCompanyId(String companyId) {
		regNumbers = new ArrayList<String>();
		request = new SoapObject(NAMESPACE, GET_VEHICLES_BY_COMPANY_ID);

		regProp = new PropertyInfo();
		regProp.setName("companyId");
		regProp.setValue(companyId);
		regProp.setType(String.class);
		request.addProperty(regProp);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

		try {
			androidHttpTransport.call(VEHICLES_BY_COMPANY_ID_SOAP_ACTION, envelope);
			SoapObject soapObj = (SoapObject) envelope.getResponse();
			// anyType{id=41; name=abc; regNo=abc; }
			for (int i = 0; i < soapObj.getPropertyCount(); i++) {
				Object property = soapObj.getProperty(i);
				
				if (property instanceof SoapObject) {
					SoapObject v = (SoapObject) property;
					String regNo = v.getProperty("regNo").toString();
					regNumbers.add(regNo);
				}
			}
				

			Log.i("myApp", soapObj.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return regNumbers;
	}
	
	// userId-1 vehicleId-2 companyId-3 startMileage-4 endMileage-5
	// questionId[]-6 answer[]-7
	private PropertyInfo userIdProp;
	private PropertyInfo vehicleIdProp;
	private PropertyInfo companyIdProp;
	private PropertyInfo latProp;
	private PropertyInfo langProp;
	private PropertyInfo questionIdProp;
	private PropertyInfo answersProp;
	private PropertyInfo regProp;
	
	//private final String _NAMESPACE = "http://service.web.vehicle.events.logik.com/";
	//private final String _URL = "http://88.208.205.225:8080/vehiclecheck/ws/soapservice?wsdl";
	//private final String _SOAP_ACTION = "http://service.web.vehicle.events.logik.com/checkOut";
	
	
	private void saveCheckList(String userId, String vehicleId, String companyId, String lat, String lang, String questionId, String answer) {
		request = new SoapObject(NAMESPACE, CHECK_OUT);

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
		
		latProp = new PropertyInfo();
		latProp.setName("latitude");
		latProp.setValue(lat);
		latProp.setType(String.class);
		request.addProperty(latProp);
		
		langProp = new PropertyInfo();
		langProp.setName("langitude");
		langProp.setValue(lang);
		langProp.setType(String.class);
		request.addProperty(langProp);
		
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
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		try {
			androidHttpTransport.call(CHECK_OUT_SOAP_ACTION, envelope);
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

	@Override
	public void onLocationChanged(Location location) {
		latitude.setText(""+location.getLatitude());
		langitude.setText(""+location.getLongitude());
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("Latitude", "disable");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("Latitude", "enable");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Latitude", "status");
	}
	
	private List<String> getAllRegNo(){
		List<String> regNoArray = 
			new ArrayList<String>();
		regNoArray.add("aaa");
		regNoArray.add("aab");
		regNoArray.add("aaac");
		regNoArray.add("aaad");
		regNoArray.add("eaaa");
		regNoArray.add("eaaa");
		regNoArray.add("eaaa");
			
		return regNoArray;
	}
}