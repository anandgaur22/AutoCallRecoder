package com.appzdigital.autocallrecoder.adapters;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.activities.CallActivity;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.models.CallObject;
import com.appzdigital.autocallrecoder.utils.ResourceUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;

/**
 * The type Outgoing call recycler view adapter.
 */
public class OutgoingCallRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
	private static final int VIEW_HEADER = 0, VIEW_ITEM = 1;
	private LayoutInflater mLayoutInflater = null;
	private List<CallObject> mOutgoingCallObjectList, mOutgoingCallObjectFilteredList;
	private boolean mReadContacts = false;

	/**
	 * Instantiates a new Outgoing call recycler view adapter.
	 *
	 * @param context                the context
	 * @param outgoingCallObjectList the outgoing call object list
	 */
	public OutgoingCallRecyclerViewAdapter (@NonNull Context context, @NonNull List<CallObject> outgoingCallObjectList) {
		try {
			mLayoutInflater = LayoutInflater.from (context);
		} catch (Exception e) {
			e.printStackTrace ();
		}
		if (mLayoutInflater == null) {
			try {
				mLayoutInflater = (LayoutInflater) context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
			} catch (Exception e) {
				e.printStackTrace ();
			}
		}
		mOutgoingCallObjectList = mOutgoingCallObjectFilteredList = outgoingCallObjectList;
	}

	/**
	 * Instantiates a new Outgoing call recycler view adapter.
	 *
	 * @param context                the context
	 * @param outgoingCallObjectList the outgoing call object list
	 * @param readContacts           the read contacts
	 */
	public OutgoingCallRecyclerViewAdapter (@NonNull Context context, @NonNull List<CallObject> outgoingCallObjectList, boolean readContacts) {
		this (context, outgoingCallObjectList);
		mReadContacts = readContacts;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder viewHolder;
		if (viewType == VIEW_HEADER) {
			View view = mLayoutInflater.inflate (R.layout.adapter_view_header, parent, false);
			viewHolder = new HeaderViewHolder (view);
		} else {
			View view = mLayoutInflater.inflate (R.layout.adapter_item, parent, false);
			viewHolder = new ItemViewHolder (view);
		}
		return viewHolder;
	}

	@Override
	public void onBindViewHolder (@NonNull RecyclerView.ViewHolder holder, int position) {
		CallObject outgoingCallObject = mOutgoingCallObjectFilteredList.get (position);
		if (outgoingCallObject != null) {
			boolean isFirstItem = position == 0, isLastItem = position == mOutgoingCallObjectFilteredList.size () - 1;
			if (holder instanceof HeaderViewHolder) {
				HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
				headerViewHolder.titleTextView.setText (outgoingCallObject.getHeaderTitle ());
			} else {
				ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
				String correspondent = outgoingCallObject.getPhoneNumber ();
				LOGE (TAG, "" + outgoingCallObject.getPhoneNumber ());
				if (correspondent != null && !correspondent.trim ().isEmpty ()) {
					if (mReadContacts) {
						try {
							Uri uri = Uri.withAppendedPath (ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode (correspondent));
							Cursor cursor = holder.itemView.getContext ().getContentResolver ().query (uri, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
							if (cursor != null) {
								if (cursor.moveToFirst ()) {
									String tempDisplayName = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup.DISPLAY_NAME));
									if (tempDisplayName != null && !tempDisplayName.trim ().isEmpty ()) {
										outgoingCallObject.setCorrespondentName (correspondent = tempDisplayName);
									}
									String id = cursor.getString (cursor.getColumnIndex (ContactsContract.PhoneLookup._ID));
									if (id != null && !id.trim ().isEmpty ()) {
										InputStream inputStream = null;
										try {
											inputStream = ContactsContract.Contacts.openContactPhotoInputStream (holder.itemView.getContext ().getContentResolver (), ContentUris.withAppendedId (ContactsContract.Contacts.CONTENT_URI, Long.valueOf (id)));
										} catch (Exception e) {
											e.printStackTrace ();
										}
										if (inputStream != null) {
											Bitmap bitmap = null;
											try {
												bitmap = BitmapFactory.decodeStream (inputStream);
											} catch (Exception e) {
												e.printStackTrace ();
											}
											if (bitmap != null) {
											}
										}
									}
								}
								cursor.close ();
							}
						} catch (Exception e) {
							e.printStackTrace ();
						}
					}
					if (correspondent.equals (outgoingCallObject.getPhoneNumber ())) {
						itemViewHolder.numberTextView.setText (holder.itemView.getContext ().getString (R.string.unknown_number));
					} else {
						itemViewHolder.numberTextView.setText (correspondent);
					}
				} else {
					itemViewHolder.numberTextView.setText (correspondent = holder.itemView.getContext ().getString (R.string.unknown_number));
				}
				String beginDateTime = null;
				if (!DateFormat.is24HourFormat (holder.itemView.getContext ())) {
					try {
						beginDateTime = new SimpleDateFormat ("hh:mm a", Locale.getDefault ()).format (new Date (outgoingCallObject.getBeginTimestamp ()));
					} catch (Exception e) {
						e.printStackTrace ();
					}
				} else {
					try {
						beginDateTime = new SimpleDateFormat ("HH:mm", Locale.getDefault ()).format (new Date (outgoingCallObject.getBeginTimestamp ()));
					} catch (Exception e) {
						e.printStackTrace ();
					}
				}
				String durationString = null;
				Date beginDate = new Date (outgoingCallObject.getBeginTimestamp ());
				Date endDate = new Date (outgoingCallObject.getEndTimestamp ());
				long durationMs = endDate.getTime () - beginDate.getTime ();
				try {
					durationString = String.format (Locale.getDefault (), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours (durationMs),
							TimeUnit.MILLISECONDS.toMinutes (durationMs) - TimeUnit.HOURS.toMinutes (TimeUnit.MILLISECONDS.toHours (durationMs)),
							TimeUnit.MILLISECONDS.toSeconds (durationMs) - TimeUnit.MINUTES.toSeconds (TimeUnit.MILLISECONDS.toMinutes (durationMs)));
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
				itemViewHolder.beginDateTimeTextView.setText (beginDateTime != null ? beginDateTime + "\n(" + durationString + ")" : "N/A");
				String finalCorrespondent = correspondent;
				itemViewHolder.menuImageButton.setOnClickListener (view -> showItemMenuDialog (holder.itemView.getContext (), outgoingCallObject, finalCorrespondent));
				itemViewHolder.imageView.setImageResource (R.drawable.ic_outgoing);
				itemViewHolder.imageView.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.cp_5), android.graphics.PorterDuff.Mode.SRC_IN);
				itemViewHolder.tvPhoneNumer.setText (String.valueOf (outgoingCallObject.getPhoneNumber ()));
				if (outgoingCallObject.isFavourit ()) {
					itemViewHolder.ivFavourit.setImageResource (R.drawable.ic_favorit);
				} else {
					itemViewHolder.ivFavourit.setImageResource (R.drawable.ic_favourit_stroke);
				}
				itemViewHolder.ivFavourit.setColorFilter (ContextCompat.getColor (holder.itemView.getContext (), R.color.cp_4), android.graphics.PorterDuff.Mode.SRC_IN);
			}
		}
	}

	@Override
	public int getItemCount () {
		return mOutgoingCallObjectFilteredList != null ? mOutgoingCallObjectFilteredList.size () : 0;
	}

	@Override
	public long getItemId (int position) {
		return position;
	}

	@Override
	public int getItemViewType (int position) {
		return mOutgoingCallObjectFilteredList.get (position).getIsHeader () ? VIEW_HEADER : VIEW_ITEM;
	}

	@Override
	public Filter getFilter () {
		return new Filter () {
			@Override
			protected FilterResults performFiltering (CharSequence charSequence) {
				String query = charSequence != null ? charSequence.toString () : null;
				if (query != null && !query.trim ().isEmpty ()) {
					List<CallObject> newOutgoingCallObjectFilteredList = new ArrayList<> ();
					for (CallObject outgoingCallObject : mOutgoingCallObjectList) {
						if (!outgoingCallObject.getIsHeader ()) {
							String phoneNumber = outgoingCallObject.getPhoneNumber ();
							if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()) {
								if (phoneNumber.toLowerCase (Locale.getDefault ()).contains (query.toLowerCase (Locale.getDefault ()))) {
									newOutgoingCallObjectFilteredList.add (outgoingCallObject);
								}
							}
							if (mReadContacts) {
								String correspondentName = outgoingCallObject.getCorrespondentName ();
								if (correspondentName != null && !correspondentName.trim ().isEmpty ()) {
									if (correspondentName.toLowerCase (Locale.getDefault ()).contains (query.toLowerCase (Locale.getDefault ()))) {
										newOutgoingCallObjectFilteredList.add (outgoingCallObject);
									}
								}
							}
						}
					}
					mOutgoingCallObjectFilteredList = newOutgoingCallObjectFilteredList;
				} else {
					mOutgoingCallObjectFilteredList = mOutgoingCallObjectList;
				}
				FilterResults filterResults = new FilterResults ();
				filterResults.values = mOutgoingCallObjectFilteredList;
				filterResults.count = mOutgoingCallObjectFilteredList.size ();
				return filterResults;
			}

			@Override
			protected void publishResults (CharSequence charSequence, FilterResults filterResults) {
				mOutgoingCallObjectFilteredList = (ArrayList<CallObject>) filterResults.values;
				notifyDataSetChanged ();
			}
		};
	}

	private void openOutgoingCall (@NonNull Context context, @NonNull CallObject outgoingCallObject) {
		Intent intent = new Intent (context, CallActivity.class);
		intent.putExtra (AppEnvr.INTENT_ACTION_OUTGOING_CALL, true);
		intent.putExtra ("mBeginTimestamp", outgoingCallObject.getBeginTimestamp ());
		intent.putExtra ("mEndTimestamp", outgoingCallObject.getEndTimestamp ());
		intent.putExtra ("mType", outgoingCallObject.getType ());
		if (outgoingCallObject.getCorrespondentName () != null && !outgoingCallObject.getCorrespondentName ().trim ().isEmpty ()) {
			intent.putExtra ("mCorrespondentName", outgoingCallObject.getCorrespondentName ());
		}
		try {
			context.startActivity (intent);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	private boolean showItemMenuDialog (@NonNull Context context, @NonNull CallObject outgoingCallObject, @NonNull String correspondent) {
		CharSequence[] menuItems = {outgoingCallObject.isFavourit () ? "UnFavourite" : "Favourite", "Open recording", "Make phone call", "Delete"};
		Drawable drawable = ResourceUtil.getDrawable (context, R.drawable.ic_outgoing);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			drawable.setTint (ResourceUtil.getColor (context, R.color.colorPrimary));
		} else {
			DrawableCompat.setTint (drawable, ResourceUtil.getColor (context, R.color.colorPrimary));
		}
		Dialog dialog = new AlertDialog.Builder (context)
				.setIcon (drawable)
				.setTitle (context.getString (R.string.outgoing_call) + " - " + correspondent)
				.setItems (menuItems, (dialogInterface, which) -> {
					dialogInterface.dismiss ();
					switch (which) {
						case 0:
							Realm realmf = null;
							try {
								realmf = Realm.getDefaultInstance ();
							} catch (Exception e) {
								e.printStackTrace ();
							}
							if (realmf != null && !realmf.isClosed ()) {
								try {
									realmf.beginTransaction ();
									List<CallObject> incomingCallObjects = realmf.where (CallObject.class)
											.equalTo ("mPhoneNumber", outgoingCallObject.getPhoneNumber ())
											.findAll ();
									if (incomingCallObjects != null) {
										for (CallObject callObject : incomingCallObjects) {
											callObject.setFavourit (!outgoingCallObject.isFavourit ());
										}
										realmf.commitTransaction ();
									} else {
										realmf.cancelTransaction ();
										Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
									}
									realmf.close ();
								} catch (Exception e) {
									e.printStackTrace ();
								}
							} else {
								Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
							}
							break;
						case 1:
							openOutgoingCall (context, outgoingCallObject);
							break;
						case 2:
							String phoneNumber = outgoingCallObject.getPhoneNumber ();
							if (phoneNumber != null && !phoneNumber.trim ().isEmpty ()
									&& ActivityCompat.checkSelfPermission (context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
								context.startActivity (new Intent (Intent.ACTION_DIAL, Uri.fromParts ("tel", phoneNumber, null)));
							} else {
								new AlertDialog.Builder (context)
										.setTitle ("Cannot make phone call")
										.setMessage ("Making phone call to this correspondent is not possible.")
										.setNeutralButton (android.R.string.ok, (dialogInterface1, i) -> {
											dialogInterface1.dismiss ();
										})
										.create ().show ();
							}
							break;
						case 3:
							new AlertDialog.Builder (context)
									.setTitle ("Delete call recording")
									.setMessage ("Are you sure you want to delete this call recording (and its audio file)? Data cannot be recovered.")
									.setPositiveButton (R.string.yes, (dialogInterface1, i) -> {
										dialogInterface1.dismiss ();
										Realm realm = null;
										try {
											realm = Realm.getDefaultInstance ();
										} catch (Exception e) {
											e.printStackTrace ();
										}
										if (realm != null && !realm.isClosed ()) {
											try {
												realm.beginTransaction ();
												CallObject outgoingCallObject1 = realm.where (CallObject.class)
														.equalTo ("mBeginTimestamp", outgoingCallObject.getBeginTimestamp ())
														.equalTo ("mEndTimestamp", outgoingCallObject.getEndTimestamp ())
														.beginGroup ()
														.equalTo ("type", "outgoing")
														.endGroup ()
														.findFirst ();
												if (outgoingCallObject1 != null) {
													File outputFile = null;
													try {
														outputFile = new File (outgoingCallObject1.getOutputFile ());
													} catch (Exception e) {
														e.printStackTrace ();
													}
													if (outputFile != null) {
														if (outputFile.exists () && outputFile.isFile ()) {
															try {
																outputFile.delete ();
															} catch (Exception e) {
																e.printStackTrace ();
															}
														}
													}
													outgoingCallObject1.deleteFromRealm ();
													realm.commitTransaction ();
													Toast.makeText (context, "Call recording is deleted", Toast.LENGTH_SHORT).show ();
												} else {
													realm.cancelTransaction ();
													Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
												}
												realm.close ();
											} catch (Exception e) {
												e.printStackTrace ();
											}
										} else {
											Toast.makeText (context, "Call recording is not deleted", Toast.LENGTH_SHORT).show ();
										}
									})
									.setNegativeButton (R.string.no, (dialogInterface1, i) -> dialogInterface1.dismiss ())
									.create ().show ();
							break;
					}
				}).create ();
		dialog.show ();
		return dialog.isShowing ();
	}

	/**
	 * The type Header view holder.
	 */
	class HeaderViewHolder extends RecyclerView.ViewHolder {
		/**
		 * The Title text view.
		 */
		TextView titleTextView;

		/**
		 * Instantiates a new Header view holder.
		 *
		 * @param itemView the item view
		 */
		public HeaderViewHolder (@NonNull View itemView) {
			super (itemView);
			titleTextView = itemView.findViewById (R.id.adapter_view_header_title);
		}
	}

	/**
	 * The type Item view holder.
	 */
	class ItemViewHolder extends RecyclerView.ViewHolder {
		/**
		 * The Image view.
		 */
		ImageView imageView;
		/**
		 * The Number text view.
		 */
		TextView numberTextView, /**
		 * The Begin date time text view.
		 */
		beginDateTimeTextView;
		/**
		 * The Menu image button.
		 */
		ImageButton menuImageButton;
		/**
		 * The Tv phone numer.
		 */
		TextView tvPhoneNumer;
		/**
		 * The Iv favourit.
		 */
		ImageView ivFavourit;

		/**
		 * Instantiates a new Item view holder.
		 *
		 * @param itemView the item view
		 */
		public ItemViewHolder (@NonNull View itemView) {
			super (itemView);
			imageView = itemView.findViewById (R.id.iv_phonenumber);
			numberTextView = itemView.findViewById (R.id.adapter_item_number);
			beginDateTimeTextView = itemView.findViewById (R.id.adapter_item_begin_date_time);
			menuImageButton = itemView.findViewById (R.id.adapter_item_menu);
			tvPhoneNumer = itemView.findViewById (R.id.tv_phonenumber);
			ivFavourit = itemView.findViewById (R.id.tv_favourit);
		}
	}
}
