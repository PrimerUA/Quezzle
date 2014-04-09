package com.skylion.quezzle.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.skylion.quezzle.R;
import com.skylion.quezzle.datastorage.table.FullMessageTable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 28.03.14 Time: 23:58 To change
 * this template use File | Settings | File Templates.
 */
public class MessageListAdapter extends CursorAdapter {
    public static final String[] PROJECTION = new String[] {FullMessageTable._ID, FullMessageTable.UPDATED_AT_COLUMN,
                                                            FullMessageTable.MESSAGE_COLUMN, FullMessageTable.AUTHOR_ID_COLUMN,
                                                            FullMessageTable.USERNAME_COLUMN, FullMessageTable.USER_AVATAR_COLUMN};
	private static final String DATE_FORMAT_PATTERN = "[dd.MM.yyyy HH:mm:ss]";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

	private int authorIdColumnIndex = -1;
	private int messageColumnIndex = -1;
	private int updatedAtColumnIndex = -1;
    private int authorNameColumnIndex = -1;
    private int authorAvatarColumnIndex = -1;

    private DisplayImageOptions options;

	public MessageListAdapter(Context context, int flags) {
		super(context, null, flags);

        options = new DisplayImageOptions.Builder()
//                .showStubImage(R.drawable.stub_image)
//                .showImageForEmptyUrl(R.drawable.image_for_empty_url)
                .imageScaleType(ImageScaleType.EXACTLY)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE))
                .build();
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
        holder.avatar = (ImageView)view.findViewById(R.id.avatar);
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
		holder.author.setText(cursor.getString(authorNameColumnIndex));
        ImageLoader.getInstance().displayImage(cursor.getString(authorAvatarColumnIndex), holder.avatar, options);
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
		authorIdColumnIndex = cursor.getColumnIndex(FullMessageTable.AUTHOR_ID_COLUMN);
		messageColumnIndex = cursor.getColumnIndex(FullMessageTable.MESSAGE_COLUMN);
		updatedAtColumnIndex = cursor.getColumnIndex(FullMessageTable.UPDATED_AT_COLUMN);
        authorNameColumnIndex = cursor.getColumnIndex(FullMessageTable.USERNAME_COLUMN);
        authorAvatarColumnIndex = cursor.getColumnIndex(FullMessageTable.USER_AVATAR_COLUMN);
	}

	private static class ViewHolder {
		public TextView author;
        public ImageView avatar;
        public TextView date;
		public TextView text;
		public RelativeLayout content;
	}
}
