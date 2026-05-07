package com.honeywell;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;

/* JADX INFO: loaded from: classes3.dex */
public class Message implements Parcelable {
    public static Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() { // from class: com.honeywell.Message.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Message createFromParcel(Parcel parcel) {
            Message message = new Message();
            message.action = (String) parcel.readValue(Message.class.getClassLoader());
            message.extras = (Map) parcel.readValue(Message.class.getClassLoader());
            return message;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Message[] newArray(int i) {
            return new Message[i];
        }
    };
    public String action;
    public Map<String, Object> extras;

    public Message() {
        this.action = null;
        this.extras = null;
    }

    @Override // android.os.Parcelable
    @SuppressLint("WrongConstant") // decompiled AIDL stub — value is correct at runtime
    public int describeContents() {
        return 1;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(this.action);
        parcel.writeValue(this.extras);
    }

    public Message(String str) {
        this.extras = null;
        this.action = str;
    }

    public Message(String str, Map<String, Object> map) {
        this.action = str;
        this.extras = map;
    }
}
