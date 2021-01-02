package com.appzdigital.autocallrecoder.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * The type Tab layout fragment pager adapter.
 */
public class TabLayoutFragmentPagerAdapter extends FragmentPagerAdapter {
	private List<ITabLayoutFragmentPagerAdapter> mTabLayoutFragmentPagerAdapterList;
	private List<ITabLayoutIconFragmentPagerAdapter> mTabLayoutIconFragmentPagerAdapterList;

	/**
	 * Instantiates a new Tab layout fragment pager adapter.
	 *
	 * @param fragmentManager                       the fragment manager
	 * @param tabLayoutFragmentPagerAdapterList     the tab layout fragment pager adapter list
	 * @param tabLayoutIconFragmentPagerAdapterList the tab layout icon fragment pager adapter list
	 */
	public TabLayoutFragmentPagerAdapter (@NonNull FragmentManager fragmentManager,
	                                      @Nullable List<ITabLayoutFragmentPagerAdapter> tabLayoutFragmentPagerAdapterList,
	                                      @Nullable List<ITabLayoutIconFragmentPagerAdapter> tabLayoutIconFragmentPagerAdapterList) {
		super (fragmentManager);
		if (tabLayoutFragmentPagerAdapterList != null) {
			mTabLayoutFragmentPagerAdapterList = tabLayoutFragmentPagerAdapterList;
			return;
		}
		if (tabLayoutIconFragmentPagerAdapterList != null) {
			mTabLayoutIconFragmentPagerAdapterList = tabLayoutIconFragmentPagerAdapterList;
		}
	}

	@Override
	public Fragment getItem (int i) {
		if (mTabLayoutFragmentPagerAdapterList != null) {
			return mTabLayoutFragmentPagerAdapterList.get (i).getItem ();
		}
		if (mTabLayoutIconFragmentPagerAdapterList != null) {
			return mTabLayoutIconFragmentPagerAdapterList.get (i).getItem ();
		}
		return null;
	}

	@Override
	public int getCount () {
		if (mTabLayoutFragmentPagerAdapterList != null) {
			return mTabLayoutFragmentPagerAdapterList.size ();
		}
		if (mTabLayoutIconFragmentPagerAdapterList != null) {
			return mTabLayoutIconFragmentPagerAdapterList.size ();
		}
		return 0;
	}

	@Override
	public CharSequence getPageTitle (int i) {
		if (mTabLayoutFragmentPagerAdapterList != null) {
			return mTabLayoutFragmentPagerAdapterList.get (i).getPageTitle ();
		}
		if (mTabLayoutIconFragmentPagerAdapterList != null) {
			return mTabLayoutIconFragmentPagerAdapterList.get (i).getPageTitle ();
		}
		return null;
	}

	/**
	 * The interface Tab layout fragment pager adapter.
	 */
	public interface ITabLayoutFragmentPagerAdapter {
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
	 * The interface Tab layout icon fragment pager adapter.
	 */
	public interface ITabLayoutIconFragmentPagerAdapter {
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
