package com.appzdigital.autocallrecoder.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import com.appzdigital.autocallrecoder.adapters.CallRecyclerViewAdapter;
import com.appzdigital.autocallrecoder.adapters.TabLayoutFragmentPagerAdapter;
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
 * The type All call tab fragment.
 */
public class AllCallTabFragment extends Fragment implements TabLayoutFragmentPagerAdapter.ITabLayoutIconFragmentPagerAdapter {
	private static final String TAG = AllCallTabFragment.class.getSimpleName ();
	/**
	 * The M recycler view.
	 */
	public RecyclerView mRecyclerView = null;
	private Realm mRealm = null;
	private RealmResults<CallObject> mCallObjectRealmResults = null;
	private ScrollView mScrollView = null;
	private LinearLayout mMainLinearLayout = null;

	private Context getContextNonNull () {
		return Objects.requireNonNull (getContext ());
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
				mCallObjectRealmResults = mRealm.where (CallObject.class)
						.greaterThan ("mEndTimestamp", 0L)
						.sort ("mBeginTimestamp", Sort.DESCENDING)
						.findAll ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (mCallObjectRealmResults != null) {
				mCallObjectRealmResults.addChangeListener (incomingCallObjectRealmResults -> {
					if (mRecyclerView != null) {
						List<CallObject> incomingCallObjectList = null;
						if (mRealm != null) {
							incomingCallObjectList = mRealm.copyFromRealm (incomingCallObjectRealmResults);
						}
						if (incomingCallObjectList == null) {
							incomingCallObjectList = new ArrayList<> (incomingCallObjectRealmResults);
						}
						setAdapter (populateAdapter (mRecyclerView.getContext (), incomingCallObjectList));
					}
					updateLayouts ();
				});
			}
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
		if (mCallObjectRealmResults != null) {
			mCallObjectRealmResults.removeAllChangeListeners ();
			mCallObjectRealmResults = null;
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
		super.onDestroy ();
		LOGD (TAG, "Fragment destroy");
	}

	@Override
	public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate (R.layout.fragment_all_call_tab, container, false);
		mScrollView = view.findViewById (R.id.fragment_allcall_tab_scroll_view);
		mMainLinearLayout = view.findViewById (R.id.fragment_allcall_tab_main_linear_layout);
		mRecyclerView = view.findViewById (R.id.fragment_allcall_tab_recycler_view);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager (mRecyclerView.getContext ());
		linearLayoutManager.setOrientation (RecyclerView.VERTICAL);
		mRecyclerView.setHasFixedSize (true);
		mRecyclerView.setLayoutManager (linearLayoutManager);
		mRecyclerView.setItemAnimator (new DefaultItemAnimator ());
		List<CallObject> incomingCallObjectList = null;
		if (mRealm != null) {
			incomingCallObjectList = mRealm.copyFromRealm (mCallObjectRealmResults);
		}
		if (incomingCallObjectList == null) {
			incomingCallObjectList = new ArrayList<> (mCallObjectRealmResults);
		}
		setAdapter (populateAdapter (mRecyclerView.getContext (), incomingCallObjectList));
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
		return "All Call";
	}

	@Override
	public int getIcon () {
		return R.drawable.ic_all_call;
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

	private CallRecyclerViewAdapter populateAdapter (@NonNull Context context, @NonNull List<CallObject> callObjectList) {
		Calendar calendar = Calendar.getInstance ();
		int todayDayOfYear = calendar.get (Calendar.DAY_OF_YEAR), yesterdayDayOfYear = todayDayOfYear - 1;
		boolean hasToday = false, hasYesterday = false;
		List<CallObject> list = new ArrayList<> ();
		if (!callObjectList.isEmpty ()) {
			calendar.setTime (new Date (callObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
				hasToday = true;
			}
			if (hasToday) {
				list.add (new CallObject (true, context.getString (R.string.today)));
				for (Iterator<CallObject> iterator = callObjectList.iterator () ; iterator.hasNext () ; ) {
					CallObject incomingCallObject = iterator.next ();
					calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == todayDayOfYear) {
						iterator.remove ();
						list.add (incomingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!callObjectList.isEmpty ()) {
			calendar.setTime (new Date (callObjectList.get (0).getBeginTimestamp ()));
			if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
				hasYesterday = true;
			}
			if (hasYesterday) {
				list.add (new CallObject (true, context.getString (R.string.yesterday)));
				for (Iterator<CallObject> iterator = callObjectList.iterator () ; iterator.hasNext () ; ) {
					CallObject incomingCallObject = iterator.next ();
					calendar.setTime (new Date (incomingCallObject.getBeginTimestamp ()));
					if (calendar.get (Calendar.DAY_OF_YEAR) == yesterdayDayOfYear) {
						iterator.remove ();
						list.add (incomingCallObject);
					} else {
						break;
					}
				}
				list.get (list.size () - 1).setIsLastInCategory (true);
			}
		}
		if (!callObjectList.isEmpty ()) {
			list.add (new CallObject (true, context.getString (R.string.older)));
			list.addAll (callObjectList);
		}
		try {
			if (ActivityCompat.checkSelfPermission (getContextNonNull (), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				return new CallRecyclerViewAdapter (context, list, true);
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		return new CallRecyclerViewAdapter (context, list);
	}

	private void setAdapter (@NonNull CallRecyclerViewAdapter incomingCallRecyclerViewAdapter) {
		if (mRecyclerView != null) {
			mRecyclerView.setAdapter (incomingCallRecyclerViewAdapter);
			mRecyclerView.setItemViewCacheSize (incomingCallRecyclerViewAdapter.getItemCount ());
		}
	}
}
