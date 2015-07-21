package com.anstar.models.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.anstar.common.NetworkConnectivity;
import com.anstar.common.Utils;
import com.anstar.fieldwork.FieldworkApplication;
import com.anstar.model.helper.ServiceHelper;
import com.anstar.model.helper.ServiceHelper.ServiceHelperDelegate;
import com.anstar.model.helper.ServiceResponse;
import com.anstar.model.mapper.ModelMapHelper;
import com.anstar.models.DilutionInfo;
import com.anstar.models.ModelDelegates.ModelDelegate;

public class DilutionRatesList implements ServiceHelperDelegate {

	private DilutionRatesList() {

	}

	private static volatile DilutionRatesList _instance = null;

	// private static volatile int cat_id = 0;

	public static DilutionRatesList Instance() {
		if (_instance == null) {
			synchronized (DilutionRatesList.class) {
				_instance = new DilutionRatesList();
				// _instance.m_modelList = new ArrayList<CategoryItemInfo>();
			}
		}
		return _instance;
	}

	protected ArrayList<DilutionInfo> m_modelList = null;
	private ModelDelegate<DilutionInfo> m_delegate = null;

	public void load(ModelDelegate<DilutionInfo> delegate) throws Exception {
		if (delegate == null) {
			throw new Exception("Delegate can not be null.");
		}
		m_delegate = delegate;
		loadFromDB();
		if (m_modelList == null) {
			if (NetworkConnectivity.isConnected()) {
				ServiceHelper helper = new ServiceHelper(
						ServiceHelper.DILUTION_RATES);
				helper.call(this);
			} else {
				m_delegate.ModelLoadFailedWithError(ServiceHelper.COMMON_ERROR);
			}
		} else {
			m_delegate.ModelLoaded(m_modelList);
		}
	}

	public void ClearDB() {
		try {
			FieldworkApplication.Connection().delete(DilutionInfo.class);
			m_modelList = null;
		} catch (Exception e) {
			Utils.LogException(e);
		}
	}

	public void loadFromDB() {
		try {
			List<DilutionInfo> list = FieldworkApplication.Connection()
					.findAll(DilutionInfo.class);
			if (list != null) {
				if (list.size() > 0) {
					m_modelList = new ArrayList<DilutionInfo>(list);
				}
			}
		} catch (Exception e) {
			Utils.LogException(e);
		}
	}

	@Override
	public void CallFinish(ServiceResponse res) {
		if (!res.isError()) {
			try {
				ClearDB();
				m_modelList = new ArrayList<DilutionInfo>();
				JSONArray subjectList = new JSONArray(res.RawResponse);
				for (int i = 0; i < subjectList.length(); i++) {
					JSONObject data = subjectList.getJSONObject(i);
					if (data != null) {
						ModelMapHelper<DilutionInfo> mapper = new ModelMapHelper<DilutionInfo>();
						DilutionInfo info = mapper.getObject(
								DilutionInfo.class, data);
						if (info != null) {
							info.save();
							m_modelList.add(info);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			m_delegate.ModelLoaded(m_modelList);
		} else {
			m_delegate.ModelLoadFailedWithError(res.getErrorMessage());
		}
	}

	@Override
	public void CallFailure(String ErrorMessage) {
		m_delegate.ModelLoadFailedWithError(ErrorMessage);
	}

	public ArrayList<DilutionInfo> getDilutionList() {
		loadFromDB();
		if (m_modelList == null) {
			m_modelList = new ArrayList<DilutionInfo>();
		}
		return m_modelList;
	}

	public int getDilutionIdByname(String name) {
		int id = 0;
		if (m_modelList == null) {
			loadFromDB();
		}
		if (m_modelList != null) {

			for (DilutionInfo d : m_modelList) {
				if (d.name.equalsIgnoreCase(name)) {
					id = d.id;
					break;
				}
			}
		}
		return id;

	}

	public String getDilutionNameByid(int id) {
		if (m_modelList == null) {
			loadFromDB();
		}
		if (m_modelList != null) {
			for (DilutionInfo d : m_modelList) {
				if (d.id == id) {
					return d.name;
				}
			}
		}
		return "";

	}

}
