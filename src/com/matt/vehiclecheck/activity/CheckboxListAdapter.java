package com.matt.vehiclecheck.activity;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.matt.vehiclecheck.R;

public class CheckboxListAdapter extends BaseAdapter implements OnClickListener {

	private LayoutInflater inflator;

	private List<CheckListData> dataList;
	private final String NAMESPACE = "http://service.web.vehicle.events.logik.com/";
	private final String URL = "http://88.208.205.225:8080/vehiclecheck/ws/soapservice?wsdl";
	private final String SOAP_ACTION = "http://service.web.vehicle.events.logik.com/getChecklist";
	private final String METHOD_NAME = "getChecklist";
	private SoapObject request;
	private CheckListData checkListData;
	private PropertyInfo companyId;
	private SharedPreferences preferences;

	public CheckboxListAdapter(LayoutInflater inflator, Context context) {
		super();
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String cmpnyId = preferences.getString("companyId", "null");
		request = new SoapObject(NAMESPACE, METHOD_NAME);
		if (!cmpnyId.equals("null")) {
			companyId = new PropertyInfo();
			companyId.setName("companyId");
			companyId.setValue(cmpnyId);
			companyId.setType(String.class);
			request.addProperty(companyId);
		}

		this.inflator = inflator;
		dataList = new ArrayList<CheckListData>();
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		try {
			androidHttpTransport.call(SOAP_ACTION, envelope);
			SoapObject checkList = (SoapObject) envelope.getResponse();

			for (int i = 0; i < checkList.getPropertyCount(); i++) {
				Object property = checkList.getProperty(i);
				if (property instanceof SoapObject) {
					SoapObject item = (SoapObject) property;
					String questionId = item.getProperty("id").toString();
					String question = item.getProperty("question").toString();
					dataList.add(new CheckListData(question, false,Integer.parseInt(questionId)));
				}
			}
			Log.i("myApp", checkList.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {

		if (view == null) {
			view = inflator.inflate(R.layout.check_list, null);
			view.findViewById(R.id.checkBox1).setOnClickListener(this);
		}

		CheckListData data = (CheckListData) getItem(position);
		CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
		cb.setChecked(data.isSelected());
		cb.setTag(data);
		TextView tv = (TextView) view.findViewById(R.id.textView1);
		tv.setText(data.getName());

		return view;
	}

	@Override
	public void onClick(View view) {
		checkListData = (CheckListData) view.getTag();
		checkListData.setSelected(((CheckBox) view).isChecked());
	}

}
