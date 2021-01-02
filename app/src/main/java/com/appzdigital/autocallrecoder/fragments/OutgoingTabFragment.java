package com.appzdigital.autocallrecoder.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.adapters.OutgoingCallRecyclerViewAdapter;
import com.appzdigital.autocallrecoder.adapters.TabLayoutFragmentPagerAdapter;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.models.CallObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;

/**
 * The type Outgoing tab fragment.
 */
public class OutgoingTabFragment extends Fragment implements TabLayoutFragmentPagerAdapter.ITabLayoutIconFragmentPagerAdapter {
	private static final String TAG = OutgoingTabFragment.class.getSimpleName ();
	/**
	 * The M recycler view.
	 */
	public RecyclerView mRecyclerView = null;
	private Realm mRealm = null;
	private RealmResults<CallObject> mOutgoingCallObjectRealmResults = null;
	private SharedPreferences mSharedPreferences = null;
	private boolean mRecordOutgoingCalls = true;
	private ScrollView mScrollView = null;
	private LinearLayout mMainLinearLayout = null;

	/**
	 * Instantiates a new Outgoing tab fragment.
	 */
	public OutgoingTabFragment () {
	}

	private Context getContextNonNull () {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return Objects.requireNonNull (getContext ());
		} else {
			return getContext ();
		}
	}

	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		LOGD (TAG, "Fragment create");
		try {
			mRealm = Realm.getDefaultInstance ();
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mRealm != null && !mRealm.isClosed ()) {
			try {
				mOutgoingCallObjectRealmResults = mRealm.where (CallObject.class)
						.greaterThan ("mEndTimestamp", 0L)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.beginGroup ()
						.equalTo ("type", "outgoing")
						.endGroup ()
						.findAll ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (mOutgoingCallObjectRealmResults != null) {
				mOutgoingCallObjectRealmResults.addChangeListener (outgoingCallObjectRealmResults -> {
					if (mRecyclerView != null) {
						List<CallObject> outgoingCallObjectList = null;
						if (mRealm != null) {
							outgoingCallObjectList = mRealm.copyFromRealm (outgoingCallObjectRealmResults);
						}
						if (outgoingCallObjectList == null) {
							outgoingCallObjectList = new ArrayList<> (outgoingCallObjectRealmResults);
						}
						setAdapter (populateAdapter (mRecyclerView.getContext (), outgoingCallObjectList));
					}
					updateLayouts ();
				});
			}
		}
		try {
			mSharedPreferences = getContextNonNull ().getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences.contains (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS)) {
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		} else {
			SharedPreferences.Editor editor = mSharedPreferences.edit ();
			editor.putBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
			editor.apply ();
		}
	}

	@Override
	public void onResume () {
		super.onResume ();
		LOGD (TAG, "Fragment resume");
		if (mRealm != null && !mRealm.isClosed ()) {
			try {
				mRealm.refresh ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	}

	@Override
	public void onDestroy () {
		super.onDestroy ();
		LOGD (TAG, "Fragment destroy");
		if (mSharedPreferences != null) {
			mSharedPreferences = null;
		}
		if (mOutgoingCallObjectRealmResults != null) {
			mOutgoingCallObjectRealmResults.removeAllChangeListeners ();
			mOutgoingCallObjectRealmResults = null;
		}
		if (mRealm != null) {
			if (!mRealm.isClosed ()) {
				try {
					mRealm.close ();
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			mRealm = null;
		}
	}

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate (R.layout.fragment_outgoing_tab, container, false);
		mScrollView = view.findViewById (R.id.fragment_outgoing_tab_scroll_view);
		mMainLinearLayout = view.findViewById (R.id.fragment_outgoing_tab_main_linear_layout);
		mRecyclerView = view.findViewById (R.id.fragment_outgoing_tab_recycler_view);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager (mRecyclerView.getContext ());
		linearLayoutManager.setOrientation (RecyclerView.VERTICAL);
		mRecyclerView.setHasFixedSize (true);
		mRecyclerView.setLayoutManager (linearLayoutManager);
		mRecyclerView.setItemAnimator (new DefaultItemAnimator ());
		List<CallObject> outgoingCallObjectList = null;
		if (mRealm != null) {
			outgoingCallObjectList = mRealm.copyFromRealm (mOutgoingCallObjectRealmResults);
		}
		if (outgoingCallObjectList == null) {
			outgoingCallObjectList = new ArrayList<> (mOutgoingCallObjectRealmResults);
		}
		setAdapter (populateAdapter (mRecyclerView.getContext (), outgoingCallObjectList));
		return view;
	}

	@Override
	public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated (view, savedInstanceState);
		updateLayouts ();
	}

	@Override
	public Fragment getItem () {
		return this;
	}

	@Override
	public CharSequence getPageTitle () {
		return "Outgoing";
	}

	@Override
	public int getIcon () {
		return R.drawable.ic_outgoing;
	}

	private OutgoingCallRecyclerViewAdapter populateAdapter (@NonNull Context context, @NonNull List<CallObject> outgoingCallObjectList) {
		Calendar calendar = Calendar.getInstance ();
		int todayDayOfYear = calendar.get (Calendar.DAY_OF_YEAR), yesterdayDayOfYear = todayDayOfYear - 1;
		boolean hasToday = false, hasYesterday = false;
		List<CallObject> list = new ArrayList<> ();
		if (!outgoingCallObjectList.isEmpty ()) {
			calendar.setTime (new Date (outgoingCallObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
				hasToday = true;
			}
			if (hasToday) {
				list.add (new CallObject (true, context.getString (R.string.today)));
				for (Iterator<CallObject> iterator = outgoingCallObjectList.iterator () ; iterator.hasNext () ; ) {
					CallObject outgoingCallObject = iterator.next ();
					calendar.setTime (new Date (outgoingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
						iterator.remove ();
						list.add (outgoingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!outgoingCallObjectList.isEmpty ()) {
			calendar.setTime (new Date (outgoingCallObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
				hasYesterday = true;
			}
			if (hasYesterday) {
				list.add (new CallObject (true, context.getString (R.string.yesterday)));
				for (Iterator<CallObject> iterator = outgoingCallObjectList.iterator () ; iterator.hasNext () ; ) {
					CallObject outgoingCallObject = iterator.next ();
					calendar.setTime (new Date (outgoingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
						iterator.remove ();
						list.add (outgoingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!outgoingCallObjectList.isEmpty ()) {
			list.add (new CallObject (true, context.getString (R.string.older)));
			list.addAll (outgoingCallObjectList);
		}
		try {
			if (ActivityCompat.checkSelfPermission (getContextNonNull (), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				return new OutgoingCallRecyclerViewAdapter (context, list, true);
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		return new OutgoingCallRecyclerViewAdapter (context, list);
	}

	private void setAdapter (@NonNull OutgoingCallRecyclerViewAdapter outgoingCallRecyclerViewAdapter) {
		if (mRecyclerView != null) {
			mRecyclerView.setAdapter (outgoingCallRecyclerViewAdapter);
			mRecyclerView.setItemViewCacheSize (outgoingCallRecyclerViewAdapter.getItemCount ());
		}
	}

	private void updateLayouts () {
		if (mRecyclerView != null && mRecyclerView.getAdapter () != null && mRecyclerView.getAdapter ().getItemCount () > 0) {
			if (mScrollView != null && mScrollView.getVisibility () != View.GONE) {
				mScrollView.setVisibility (View.GONE);
			}
			if (mMainLinearLayout != null && mMainLinearLayout.getVisibility () != View.VISIBLE) {
				mMainLinearLayout.setVisibility (View.VISIBLE);
			}
		} else {
			if (mMainLinearLayout != null && mMainLinearLayout.getVisibility () != View.GONE) {
				mMainLinearLayout.setVisibility (View.GONE);
			}
			if (mScrollView != null && mScrollView.getVisibility () != View.VISIBLE) {
				mScrollView.setVisibility (View.VISIBLE);
			}
		}
	}
}
