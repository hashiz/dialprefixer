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

	public static enum Key {
		UUID,
		ORDER,
		ENABLE,
		USERRULE,
		NAME,
		ACTION,
		CONTINUE,
		PATTERN,
		NEGATE;

		private final static Key[] sKeys;
		private final static Key[] sDataKeys;
		private final static int sSkip = 2;
		static {
			Key[] values = values();
			sKeys = new Key[values.length];
			sDataKeys = new Key[values.length-sSkip];
			for (int i = 0; i < values.length; i++) {
				Key key = values[i];
				sKeys[i] = key;
				if (i >= sSkip) {
					sDataKeys[i-sSkip] = key;
				}
			}
		};

		public static Key[] getKeys() {
			return sKeys;
		}

		public static Key[] getDataKeys() {
			return sDataKeys;
		}
	}

	public static enum RuleAction {
		REWRITE;

		private final static RuleAction[] sKeys;
		private final static RuleAction[] sDataKeys;
		private final static int sSkip = 2;
		static {
			RuleAction[] values = values();
			sKeys = new RuleAction[values.length];
			sDataKeys = new RuleAction[values.length-sSkip];
			for (int i = 0; i < values.length; i++) {
				RuleAction key = values[i];
				sKeys[i] = key;
				if (i >= sSkip) {
					sDataKeys[i-sSkip] = key;
				}
			}
		};

		public static RuleAction[] getKeys() {
			return sKeys;
		}

		public static RuleAction[] getDataKeys() {
			return sDataKeys;
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

	String getValue(Key key) {
		switch (key) {
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
			return getAction().name();
		case CONTINUE:
			return Boolean.toString(isContinue());
		case PATTERN:
			return getPattern();
		case NEGATE:
			return Boolean.toString(isNegate());
		}
		return null;
	}

	void setValue(String key, String value) {
		Key k;
		try {
			k = Key.valueOf(key);
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

	void setValue(Key key, String value) {
		switch (key) {
		case UUID:
			setUuid(UUID.fromString(value)) ;
			break;
		case ORDER:
			setOrder(Integer.parseInt(value));
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
		case ENABLE:
			break;
		case USERRULE:
			break;
		}
	}

	static Key[] listDataKeys() {
		return Key.getDataKeys();
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
    	out.writeString(mName);
    	out.writeInt(mOrder);
    	out.writeString(mAction.name());
    	out.writeInt(mContinue ? 1 : 0);
    	out.writeString(mPattern);
    	out.writeInt(mNegate ? 1 : 0);
	}

	public RuleEntry(Parcel in) {
		mUuid                = UUID.fromString(in.readString());
    	mName                = in.readString();
    	mOrder               = in.readInt();
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
    	for (Key key : Key.getKeys()) {
    		String value = getValue(key);
    		if (value != null) {
    			out.write(key.name() + '=' + value );
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
