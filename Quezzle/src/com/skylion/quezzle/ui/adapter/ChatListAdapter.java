package com.skylion.quezzle.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.skylion.quezzle.R;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 23.03.14
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
public class ChatListAdapter extends CursorAdapter {
    private int nameColumnIndex = -1;
    private int descriptionColumnIndex = -1;

    public ChatListAdapter(Context context, int flags) {
        super(context, null, flags);
    }

    private boolean isColumnIndexesCalculated() {
        return (nameColumnIndex >= 0);
    }

    private void calculateColumnIndexes(Cursor cursor) {
        nameColumnIndex = cursor.getColumnIndex(ChatPlaceTable.NAME_COLUMN);
        descriptionColumnIndex = cursor.getColumnIndex(ChatPlaceTable.DESCRIPTION_COLUMN);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //create view
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.chat_list_item, viewGroup, false);

        //create holder
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView)view.findViewById(R.id.chat_name);
        holder.description = (TextView)view.findViewById(R.id.chat_description);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        if (!isColumnIndexesCalculated()) {
            calculateColumnIndexes(cursor);
        }

        holder.name.setText(cursor.getString(nameColumnIndex));
        holder.description.setText(cursor.getString(descriptionColumnIndex));
    }

    private static class ViewHolder {
        public TextView name;
        public TextView description;
    }
}
