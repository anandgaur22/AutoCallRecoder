package com.appzdigital.autocallrecoder.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * The type Tab layout fragment state pager adapter.
 */
public class TabLayoutFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
	private List<ITabLayoutFragmentStatePagerAdapter> mTabLayoutFragmentStatePagerAdapterList;
	private List<ITabLayoutIconFragmentStatePagerAdapter> mTabLayoutIconFragmentStatePagerAdapterList;

	/**
	 * Instantiates a new Tab layout fragment state pager adapter.
	 *
	 * @param fragmentManager                            the fragment manager
	 * @param tabLayoutFragmentStatePagerAdapterList     the tab layout fragment state pager adapter list
	 * @param tabLayoutIconFragmentStatePagerAdapterList the tab layout icon fragment state pager adapter list
	 */
	public TabLayoutFragmentStatePagerAdapter (@NonNull FragmentManager fragmentManager,
	                                           @Nullable List<ITabLayoutFragmentStatePagerAdapter> tabLayoutFragmentStatePagerAdapterList,
	                                           @Nullable List<ITabLayoutIconFragmentStatePagerAdapter> tabLayoutIconFragmentStatePagerAdapterList) {
		super (fragmentManager);
		if (tabLayoutFragmentStatePagerAdapterList != null) {
			mTabLayoutFragmentStatePagerAdapterList = tabLayoutFragmentStatePagerAdapterList;
			return;
		}
		if (tabLayoutIconFragmentStatePagerAdapterList != null) {
			mTabLayoutIconFragmentStatePagerAdapterList = tabLayoutIconFragmentStatePagerAdapterList;
		}
	}

	@Override
	public Fragment getItem (int i) {
		if (mTabLayoutFragmentStatePagerAdapterList != null) {
			return mTabLayoutFragmentStatePagerAdapterList.get (i).getItem ();
		}
		if (mTabLayoutIconFragmentStatePagerAdapterList != null) {
			return mTabLayoutIconFragmentStatePagerAdapterList.get (i).getItem ();
		}
		return null;
	}

	@Override
	public int getCount () {
		if (mTabLayoutFragmentStatePagerAdapterList != null) {
			return mTabLayoutFragmentStatePagerAdapterList.size ();
		}
		if (mTabLayoutIconFragmentStatePagerAdapterList != null) {
			return mTabLayoutIconFragmentStatePagerAdapterList.size ();
		}
		return 0;
	}

	@Override
	public CharSequence getPageTitle (int i) {
		if (mTabLayoutFragmentStatePagerAdapterList != null) {
			return mTabLayoutFragmentStatePagerAdapterList.get (i).getPageTitle ();
		}
		if (mTabLayoutIconFragmentStatePagerAdapterList != null) {
			return mTabLayoutIconFragmentStatePagerAdapterList.get (i).getPageTitle ();
		}
		return null;
	}

	/**
	 * The interface Tab layout fragment state pager adapter.
	 */
	public interface ITabLayoutFragmentStatePagerAdapter {
		/**
		 * Gets item.
		 *
		 * @return the item
		 */
		Fragment getItem ();

		/**
		 * Gets page title.
		 *
		 * @return the page title
		 */
		CharSequence getPageTitle ();
	}

	/**
	 * The interface Tab layout icon fragment state pager adapter.
	 */
	public interface ITabLayoutIconFragmentStatePagerAdapter {
		/**
		 * Gets item.
		 *
		 * @return the item
		 */
		Fragment getItem ();

		/**
		 * Gets page title.
		 *
		 * @return the page title
		 */
		CharSequence getPageTitle ();

		/**
		 * Gets icon.
		 *
		 * @return the icon
		 */
		int getIcon ();
	}
}
