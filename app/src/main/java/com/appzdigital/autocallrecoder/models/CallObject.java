package com.appzdigital.autocallrecoder.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * The type Call object.
 */
public class CallObject extends RealmObject {
	private String mPhoneNumber;
	private long mBeginTimestamp, mEndTimestamp;
	private boolean mIsInProgress;
	private String mSimOperator, mSimOperatorName, mSimCountryIso;
	private String mSimSerialNumber;
	private String mNetworkOperator, mNetworkOperatorName, mNetworkCountryIso;
	private int mAudioSource, mOutputFormat, mAudioEncoder;
	private String mOutputFile;
	private boolean mIsSaved;
	private String type;
	private boolean favourit;
	@Ignore
	private boolean mIsHeader = false;
	@Ignore
	private String mHeaderTitle = null;
	@Ignore
	private boolean mIsLastInCategory = false;
	@Ignore
	private String mCorrespondentName = null;

	/**
	 * Instantiates a new Call object.
	 */
	public CallObject () {
	}

	/**
	 * Instantiates a new Call object.
	 *
	 * @param phoneNumber the phone number
	 */
	public CallObject (String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

	/**
	 * Instantiates a new Call object.
	 *
	 * @param phoneNumber         the phone number
	 * @param beginTimestamp      the begin timestamp
	 * @param endTimestamp        the end timestamp
	 * @param isInProgress        the is in progress
	 * @param simOperator         the sim operator
	 * @param simOperatorName     the sim operator name
	 * @param simCountryIso       the sim country iso
	 * @param simSerialNumber     the sim serial number
	 * @param networkOperator     the network operator
	 * @param networkOperatorName the network operator name
	 * @param networkCountryIso   the network country iso
	 * @param audioSource         the audio source
	 * @param outputFormat        the output format
	 * @param audioEncoder        the audio encoder
	 * @param outputFile          the output file
	 * @param type                the type
	 * @param favourit            the favourit
	 * @param isSaved             the is saved
	 */
	public CallObject (String phoneNumber,
	                   long beginTimestamp, long endTimestamp,
	                   boolean isInProgress,
	                   String simOperator, String simOperatorName, String simCountryIso,
	                   String simSerialNumber,
	                   String networkOperator, String networkOperatorName, String networkCountryIso,
	                   int audioSource, int outputFormat, int audioEncoder,
	                   String outputFile,
	                   String type,
	                   boolean favourit,
	                   boolean isSaved) {
		mPhoneNumber = phoneNumber;
		mBeginTimestamp = beginTimestamp;
		mEndTimestamp = endTimestamp;
		mIsInProgress = isInProgress;
		mSimOperator = simOperator;
		mSimOperatorName = simOperatorName;
		mSimCountryIso = simCountryIso;
		mSimSerialNumber = simSerialNumber;
		mNetworkOperator = networkOperator;
		mNetworkOperatorName = networkOperatorName;
		mNetworkCountryIso = networkCountryIso;
		mAudioSource = audioSource;
		mOutputFormat = outputFormat;
		mAudioEncoder = audioEncoder;
		mOutputFile = outputFile;
		this.type = type;
		this.favourit = favourit;
		mIsSaved = isSaved;
	}

	/**
	 * Instantiates a new Call object.
	 *
	 * @param isHeader    the is header
	 * @param headerTitle the header title
	 */
	public CallObject (boolean isHeader, String headerTitle) {
		mIsHeader = isHeader;
		mHeaderTitle = headerTitle;
	}

	/**
	 * Gets phone number.
	 *
	 * @return the phone number
	 */
	public String getPhoneNumber () {
		return mPhoneNumber;
	}

	/**
	 * Sets phone number.
	 *
	 * @param phoneNumber the phone number
	 */
	public void setPhoneNumber (String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

	/**
	 * Gets begin timestamp.
	 *
	 * @return the begin timestamp
	 */
	public long getBeginTimestamp () {
		return mBeginTimestamp;
	}

	/**
	 * Sets begin timestamp.
	 *
	 * @param beginTimestamp the begin timestamp
	 */
	public void setBeginTimestamp (long beginTimestamp) {
		mBeginTimestamp = beginTimestamp;
	}

	/**
	 * Gets end timestamp.
	 *
	 * @return the end timestamp
	 */
	public long getEndTimestamp () {
		return mEndTimestamp;
	}

	/**
	 * Sets end timestamp.
	 *
	 * @param endTimestamp the end timestamp
	 */
	public void setEndTimestamp (long endTimestamp) {
		mEndTimestamp = endTimestamp;
	}

	/**
	 * Is is in progress boolean.
	 *
	 * @return the boolean
	 */
	public boolean isIsInProgress () {
		return mIsInProgress;
	}

	/**
	 * Sets is in progress.
	 *
	 * @param isInProgress the is in progress
	 */
	public void setIsInProgress (boolean isInProgress) {
		mIsInProgress = isInProgress;
	}

	/**
	 * Gets sim operator.
	 *
	 * @return the sim operator
	 */
	public String getSimOperator () {
		return mSimOperator;
	}

	/**
	 * Sets sim operator.
	 *
	 * @param simOperator the sim operator
	 */
	public void setSimOperator (String simOperator) {
		mSimOperator = simOperator;
	}

	/**
	 * Gets sim operator name.
	 *
	 * @return the sim operator name
	 */
	public String getSimOperatorName () {
		return mSimOperatorName;
	}

	/**
	 * Sets sim operator name.
	 *
	 * @param simOperatorName the sim operator name
	 */
	public void setSimOperatorName (String simOperatorName) {
		mSimOperatorName = simOperatorName;
	}

	/**
	 * Gets sim country iso.
	 *
	 * @return the sim country iso
	 */
	public String getSimCountryIso () {
		return mSimCountryIso;
	}

	/**
	 * Sets sim country iso.
	 *
	 * @param simCountryIso the sim country iso
	 */
	public void setSimCountryIso (String simCountryIso) {
		mSimCountryIso = simCountryIso;
	}

	/**
	 * Gets sim serial number.
	 *
	 * @return the sim serial number
	 */
	public String getSimSerialNumber () {
		return mSimSerialNumber;
	}

	/**
	 * Sets sim serial number.
	 *
	 * @param simSerialNumber the sim serial number
	 */
	public void setSimSerialNumber (String simSerialNumber) {
		mSimSerialNumber = simSerialNumber;
	}

	/**
	 * Gets network operator.
	 *
	 * @return the network operator
	 */
	public String getNetworkOperator () {
		return mNetworkOperator;
	}

	/**
	 * Sets network operator.
	 *
	 * @param networkOperator the network operator
	 */
	public void setNetworkOperator (String networkOperator) {
		mNetworkOperator = networkOperator;
	}

	/**
	 * Gets network operator name.
	 *
	 * @return the network operator name
	 */
	public String getNetworkOperatorName () {
		return mNetworkOperatorName;
	}

	/**
	 * Sets network operator name.
	 *
	 * @param networkOperatorName the network operator name
	 */
	public void setNetworkOperatorName (String networkOperatorName) {
		mNetworkOperatorName = networkOperatorName;
	}

	/**
	 * Gets network country iso.
	 *
	 * @return the network country iso
	 */
	public String getNetworkCountryIso () {
		return mNetworkCountryIso;
	}

	/**
	 * Sets network country iso.
	 *
	 * @param networkCountryIso the network country iso
	 */
	public void setNetworkCountryIso (String networkCountryIso) {
		mNetworkCountryIso = networkCountryIso;
	}

	/**
	 * Gets audio source.
	 *
	 * @return the audio source
	 */
	public int getAudioSource () {
		return mAudioSource;
	}

	/**
	 * Sets audio source.
	 *
	 * @param audioSource the audio source
	 */
	public void setAudioSource (int audioSource) {
		mAudioSource = audioSource;
	}

	/**
	 * Gets output format.
	 *
	 * @return the output format
	 */
	public int getOutputFormat () {
		return mOutputFormat;
	}

	/**
	 * Sets output format.
	 *
	 * @param outputFormat the output format
	 */
	public void setOutputFormat (int outputFormat) {
		mOutputFormat = outputFormat;
	}

	/**
	 * Gets audio encoder.
	 *
	 * @return the audio encoder
	 */
	public int getAudioEncoder () {
		return mAudioEncoder;
	}

	/**
	 * Sets audio encoder.
	 *
	 * @param audioEncoder the audio encoder
	 */
	public void setAudioEncoder (int audioEncoder) {
		mAudioEncoder = audioEncoder;
	}

	/**
	 * Gets output file.
	 *
	 * @return the output file
	 */
	public String getOutputFile () {
		return mOutputFile;
	}

	/**
	 * Sets output file.
	 *
	 * @param outputFile the output file
	 */
	public void setOutputFile (String outputFile) {
		mOutputFile = outputFile;
	}

	/**
	 * Gets is saved.
	 *
	 * @return the is saved
	 */
	public boolean getIsSaved () {
		return mIsSaved;
	}

	/**
	 * Sets is saved.
	 *
	 * @param isSaved the is saved
	 */
	public void setIsSaved (boolean isSaved) {
		mIsSaved = isSaved;
	}

	/**
	 * Gets is header.
	 *
	 * @return the is header
	 */
	public boolean getIsHeader () {
		return mIsHeader;
	}

	/**
	 * Sets is header.
	 *
	 * @param isHeader the is header
	 */
	public void setIsHeader (boolean isHeader) {
		mIsHeader = isHeader;
	}

	/**
	 * Gets header title.
	 *
	 * @return the header title
	 */
	public String getHeaderTitle () {
		return mHeaderTitle.substring (0, 1).toUpperCase () + mHeaderTitle.substring (1).toLowerCase ();
	}

	/**
	 * Sets header title.
	 *
	 * @param headerTitle the header title
	 */
	public void setHeaderTitle (String headerTitle) {
		mHeaderTitle = headerTitle;
	}

	/**
	 * Gets is last in category.
	 *
	 * @return the is last in category
	 */
	public boolean getIsLastInCategory () {
		return mIsLastInCategory;
	}

	/**
	 * Sets is last in category.
	 *
	 * @param isLastInCategory the is last in category
	 */
	public void setIsLastInCategory (boolean isLastInCategory) {
		mIsLastInCategory = isLastInCategory;
	}

	/**
	 * Gets correspondent name.
	 *
	 * @return the correspondent name
	 */
	public String getCorrespondentName () {
		return mCorrespondentName;
	}

	/**
	 * Sets correspondent name.
	 *
	 * @param correspondentName the correspondent name
	 */
	public void setCorrespondentName (String correspondentName) {
		mCorrespondentName = correspondentName;
	}

	/**
	 * Gets type.
	 *
	 * @return the type
	 */
	public String getType () {
		return type;
	}

	/**
	 * Sets type.
	 *
	 * @param type the type
	 */
	public void setType (String type) {
		this.type = type;
	}

	/**
	 * Is favourit boolean.
	 *
	 * @return the boolean
	 */
	public boolean isFavourit () {
		return favourit;
	}

	/**
	 * Sets favourit.
	 *
	 * @param favourit the favourit
	 */
	public void setFavourit (boolean favourit) {
		this.favourit = favourit;
	}
}
