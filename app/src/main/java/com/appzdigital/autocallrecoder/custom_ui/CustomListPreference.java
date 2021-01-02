package com.appzdigital.autocallrecoder.custom_ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import com.appzdigital.autocallrecoder.R;

/**
 * The type Custom list preference.
 */
public class CustomListPreference extends ListPreference {
	private Context context;

	/**
	 * Instantiates a new Custom list preference.
	 *
	 * @param context      the context
	 * @param attrs        the attrs
	 * @param defStyleAttr the def style attr
	 * @param defStyleRes  the def style res
	 */
	public CustomListPreference (Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super (context, attrs, defStyleAttr, defStyleRes);
		this.context = context;
	}

	/**
	 * Instantiates a new Custom list preference.
	 *
	 * @param context      the context
	 * @param attrs        the attrs
	 * @param defStyleAttr the def style attr
	 */
	public CustomListPreference (Context context, AttributeSet attrs, int defStyleAttr) {
		super (context, attrs, defStyleAttr);
		this.context = context;
	}

	/**
	 * Instantiates a new Custom list preference.
	 *
	 * @param context the context
	 * @param attrs   the attrs
	 */
	public CustomListPreference (Context context, AttributeSet attrs) {
		super (context, attrs);
		this.context = context;
	}

	/**
	 * Instantiates a new Custom list preference.
	 *
	 * @param context the context
	 */
	public CustomListPreference (Context context) {
		super (context);
		this.context = context;
	}

	@Override
	public void onBindViewHolder (PreferenceViewHolder holder) {
		super.onBindViewHolder (holder);
		holder.itemView.setMinimumHeight (context.getResources ().getDimensionPixelSize (R.dimen._72sdp));
		TextView title = (TextView) holder.findViewById (android.R.id.title);
		title.setTextColor (Color.BLACK);
		Typeface typefacet = ResourcesCompat.getFont (context, R.font.lato_bold);
		title.setTypeface (typefacet);
		TextView summary = (TextView) holder.findViewById (android.R.id.summary);
		summary.setTextColor (Color.BLACK);
		summary.setAlpha (0.5f);
		Typeface typefaces = ResourcesCompat.getFont (context, R.font.lato_regular);
		summary.setTypeface (typefaces);
		summary.setPadding (context.getResources ().getDimensionPixelSize (R.dimen._16sdp), 0, 0, 0);
	}
}
