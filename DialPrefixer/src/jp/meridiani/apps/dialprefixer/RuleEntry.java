package jp.meridiani.apps.dialprefixer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

public class RuleEntry implements Parcelable {

	private static final String RULEENTRY_START = "<ruleEntry>";
	private static final String RULEENTRY_END   = "</ruleEntry>";

	public static enum RuleColomuns {
		UUID,
		ORDER,
		ENABLE,
		USERRULE,
		NAME,
		ACTION,
		CONTINUE,
		PATTERN,
		NEGATE;
	}

	public static enum RuleAction {
		REWRITE;

		private final static RuleAction[] sActions;
		static {
			RuleAction[] values = values();
			sActions = new RuleAction[values.length];
			for (int i = 0; i < values.length; i++) {
				RuleAction key = values[i];
				sActions[i] = key;
			}
		};

		public static RuleAction[] getActions() {
			return sActions;
		}
	}

	private UUID mUuid;
	private int mOrder;
	private String mName;

	private boolean mEnable;
	private boolean mUserRule;
	
	private RuleAction mAction;
	private boolean mContinue;

	private String mPattern;
	private boolean mNegate;
	
	RuleEntry() {
		this((UUID)null);
	}

	RuleEntry(UUID uuid) {
		if (uuid == null) {
			uuid = UUID.randomUUID();
		}
		
		mUuid = uuid;
		mOrder = 0;
		mName = "";

		mAction = RuleAction.REWRITE;
		mContinue = true;

		mPattern = "";
		mNegate = false;
	}

	String getValue(RuleColomuns col) {
		switch (col) {
		case UUID:
			return getUuid().toString();
		case ORDER:
			return Integer.toString(getOrder());
		case ENABLE:
			return Boolean.toString(isEnable());
		case USERRULE:
			return Boolean.toString(isUserRule());
		case NAME:
			return getName();
		case ACTION:
			return getAction().toString();
		case CONTINUE:
			return Boolean.toString(isContinue());
		case PATTERN:
			return getPattern();
		case NEGATE:
			return Boolean.toString(isNegate());
		}
		return null;
	}

	void setValue(String col, String value) {
		RuleColomuns k;
		try {
			k = RuleColomuns.valueOf(col);
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			return;
		}
		catch (NullPointerException e) {
			e.printStackTrace();
			return;
		}
		setValue(k, value);
	}

	void setValue(RuleColomuns key, String value) {
		switch (key) {
		case UUID:
			setUuid(UUID.fromString(value)) ;
			break;
		case ORDER:
			setOrder(Integer.parseInt(value));
			break;
		case ENABLE:
			setEnable(Boolean.parseBoolean(value));
			break;
		case USERRULE:
			setUserRule(Boolean.parseBoolean(value));
			break;
		case NAME:
			setName(value);
			break;
		case ACTION:
			setAction(RuleAction.valueOf(value));
			break;
		case CONTINUE:
			setContinue(Boolean.parseBoolean(value));
			break;
		case PATTERN:
			setPattern(value);
			break;
		case NEGATE:
			setNegate(Boolean.parseBoolean(value));
			break;
		}
	}

	public UUID getUuid() {
		return mUuid;
	}

	public void setUuid(UUID uuid) {
		mUuid = uuid;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public int getOrder() {
		return mOrder;
	}

	public void setOrder(int order) {
		mOrder = order;
	}

	public boolean isEnable() {
		return mEnable;
	}

	public void setEnable(boolean enable) {
		mEnable = enable;
	}

	public boolean isUserRule() {
		return mUserRule;
	}

	public void setUserRule(boolean userRule) {
		mUserRule = userRule;
	}

	public RuleAction getAction() {
		return mAction;
	}

	public void setAction(RuleAction action) {
		mAction = action;
	}

	public boolean isContinue() {
		return mContinue;
	}

	public void setContinue(boolean cont) {
		mContinue = cont;
	}
	
	public String getPattern() {
		return mPattern;
	}

	public void setPattern(String pattern) {
		mPattern = pattern;
	}

	public boolean isNegate() {
		return mNegate;
	}

	public void setNegate(boolean negate) {
		mNegate = negate;
	}
	
	
	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public int describeContents() {
		return 0;
	}

    @Override
	public void writeToParcel(Parcel out, int flags) {
    	out.writeString(mUuid.toString());
    	out.writeInt(mOrder);
    	out.writeInt(mEnable ? 1 : 0);
    	out.writeInt(mUserRule ? 1 : 0);
    	out.writeString(mName);
    	out.writeString(mAction.toString());
    	out.writeInt(mContinue ? 1 : 0);
    	out.writeString(mPattern);
    	out.writeInt(mNegate ? 1 : 0);
	}

	public RuleEntry(Parcel in) {
		mUuid                = UUID.fromString(in.readString());
    	mOrder               = in.readInt();
    	mEnable              = in.readInt() != 0;
    	mUserRule            = in.readInt() != 0;
    	mName                = in.readString();
    	mAction              = RuleAction.valueOf(in.readString());
    	mContinue            = in.readInt() != 0;
    	mPattern             = in.readString();
    	mNegate              = in.readInt() != 0;
	}

    public static final Parcelable.Creator<RuleEntry> CREATOR = new Parcelable.Creator<RuleEntry>() {
		public RuleEntry createFromParcel(Parcel in) {
		    return new RuleEntry(in);
		}
		
		public RuleEntry[] newArray(int size) {
		    return new RuleEntry[size];
		}
    };

    // for backup
    public void writeToText(BufferedWriter out) throws IOException {
    	out.write(RULEENTRY_START);
    	out.newLine();
    	for (RuleColomuns col : RuleColomuns.values()) {
    		String value = getValue(col);
    		if (value != null) {
    			out.write(col.toString() + '=' + value );
    			out.newLine();
    		}
    	}
    	out.write(RULEENTRY_END);
    	out.newLine();
    }

    public static RuleEntry createFromText(BufferedReader rdr) throws IOException {
    	RuleEntry profile = null;
    	boolean started = false;
    	String line;
		while ((line = rdr.readLine()) != null) {
			if (started) {
				if (RULEENTRY_END.equals(line)) {
					break;
				}
				String[] tmp = line.split("=", 2);
				if (tmp.length < 2) {
					continue;
				}
				profile.setValue(tmp[0], tmp[1]);
			}
			else {
				if (RULEENTRY_START.equals(line)) {
					started = true;
					profile = new RuleEntry();
				}
			}
		}
    	return profile;
    }
}
