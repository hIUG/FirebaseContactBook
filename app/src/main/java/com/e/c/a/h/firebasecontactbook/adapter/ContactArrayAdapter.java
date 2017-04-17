package com.e.c.a.h.firebasecontactbook.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.e.c.a.h.firebasecontactbook.R;
import com.e.c.a.h.firebasecontactbook.data.Contact;

import java.util.List;

/**
 * Created by hugoa on 4/9/2017.
 */

public class ContactArrayAdapter extends ArrayAdapter<Contact> {

    public ContactArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Contact c = getItem(position);
        ImageView listItemViewImage;
        TextView listItemViewName;
        TextView listItemViewBirthDate;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_list_view_item, null);
        }

        if(c != null) {
            listItemViewImage = (ImageView) convertView.findViewById(R.id.listItemViewImage);
            listItemViewName = (TextView) convertView.findViewById(R.id.listItemViewName);
            listItemViewBirthDate = (TextView) convertView.findViewById(R.id.listItemViewBirthDate);

            listItemViewName.setText(c.getName());
            listItemViewBirthDate.setText(c.getBirthDate());

            if(c.getImageURL() != null) {
                Glide.with(listItemViewImage.getContext()).load(c.getImageURL()).into(listItemViewImage);
            }
        }

        return convertView;
    }
}
