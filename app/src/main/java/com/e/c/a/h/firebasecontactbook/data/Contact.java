package com.e.c.a.h.firebasecontactbook.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hugoa on 4/9/2017.
 */

public class Contact implements Parcelable {
    private String uid;
    private String name;
    private String birthDate;
    private String imageURL;
    private String deleteDate;

    public Contact() {
    }

    public Contact(String uid, String name, String birthDate, String imageURL, String deleteDate) {
        this.uid = uid;
        this.name = name;
        this.birthDate = birthDate;
        this.imageURL = imageURL;
        this.deleteDate = deleteDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(String deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;

        Contact contact = (Contact) o;

        return uid.equals(contact.uid);

    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.name);
        dest.writeString(this.birthDate);
        dest.writeString(this.imageURL);
        dest.writeString(this.deleteDate);
    }

    protected Contact(Parcel in) {
        this.uid = in.readString();
        this.name = in.readString();
        this.birthDate = in.readString();
        this.imageURL = in.readString();
        this.deleteDate = in.readString();
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
