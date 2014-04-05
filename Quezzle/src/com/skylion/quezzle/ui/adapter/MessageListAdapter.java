package com.skylion.quezzle.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.skylion.quezzle.R;
import com.skylion.quezzle.datastorage.table.MessageTable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 28.03.14 Time: 23:58 To change
 * this template use File | Settings | File Templates.
 */
public class MessageListAdapter extends CursorAdapter {
    public static final String[] PROJECTION = new String[] {MessageTable._ID, MessageTable.UPDATED_AT_COLUMN,
                                                            MessageTable.MESSAGE_COLUMN, MessageTable.AUTHOR_COLUMN };
	private static final String DATE_FORMAT_PATTERN = "[dd.MM.yyyy HH:mm:ss]";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

	private int authorColumnIndex = -1;
	private int messageColumnIndex = -1;
	private int updatedAtColumnIndex = -1;

	public MessageListAdapter(Context context, int flags) {
		super(context, null, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		// create view
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.message_list_item, viewGroup, false);

		// create holder
		ViewHolder holder = new ViewHolder();
		holder.content = (RelativeLayout) view.findViewById(R.id.contentLayout);
		holder.author = (TextView) view.findViewById(R.id.message_author);
		holder.date = (TextView) view.findViewById(R.id.message_date);
		holder.text = (TextView) view.findViewById(R.id.message_text);
		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		if (!isColumnIndexesCalculated()) {
			calculateColumnIndexes(cursor);
		}

		Date date = new Date(cursor.getLong(updatedAtColumnIndex));
		holder.author.setText(cursor.getString(authorColumnIndex));
		holder.date.setText(DATE_FORMAT.format(date));
		holder.text.setText(cursor.getString(messageColumnIndex));

		if (cursor.getPosition() % 2 == 0) {
			holder.content.setBackgroundResource(R.drawable.item);
		} else {
			holder.content.setBackgroundResource(R.drawable.item_my);
		}
	}

	private boolean isColumnIndexesCalculated() {
		return (messageColumnIndex >= 0);
	}

	private void calculateColumnIndexes(Cursor cursor) {
		authorColumnIndex = cursor.getColumnIndex(MessageTable.AUTHOR_COLUMN);
		messageColumnIndex = cursor.getColumnIndex(MessageTable.MESSAGE_COLUMN);
		updatedAtColumnIndex = cursor.getColumnIndex(MessageTable.UPDATED_AT_COLUMN);
	}

	private static class ViewHolder {
		public TextView author;
		public TextView date;
		public TextView text;
		public RelativeLayout content;
	}
}
