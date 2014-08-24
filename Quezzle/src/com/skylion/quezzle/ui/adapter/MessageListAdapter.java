package com.skylion.quezzle.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
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
	public static final String[] PROJECTION = new String[] { FullMessageTable._ID, FullMessageTable.UPDATED_AT_COLUMN,
			FullMessageTable.MESSAGE_COLUMN, FullMessageTable.AUTHOR_ID_COLUMN, FullMessageTable.USERNAME_COLUMN,
			FullMessageTable.USER_AVATAR_COLUMN, FullMessageTable.GPLUS_LINK_COLUMN, FullMessageTable.IS_ADMIN_COLUMN };
	private static final String DATE_FORMAT_PATTERN = "[dd.MM.yyyy HH:mm:ss]";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

	private int authorIdColumnIndex = -1;
	private int messageColumnIndex = -1;
	private int updatedAtColumnIndex = -1;
	private int authorNameColumnIndex = -1;
	private int authorAvatarColumnIndex = -1;
	private int gplusLinkColumnIndex = -1;
	private int isAdminColumnIndex = -1;

    private Context context;
	private DisplayImageOptions options;
    private View.OnClickListener onAvatarClickListener;

	public MessageListAdapter(Context context) {
		super(context, null, 0);

        this.context = context;

		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_avatar).showImageForEmptyUri(R.drawable.default_avatar)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true)
				.displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE)).build();
        onAvatarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View avatar) {
                String gplusLink = (String)avatar.getTag();
                if (!TextUtils.isEmpty(gplusLink)) {
                    showLink(gplusLink);
                } else {
                    Toast.makeText(MessageListAdapter.this.context, R.string.no_gplus_link, Toast.LENGTH_SHORT).show();
                }
            }
        };
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		// create view
		LayoutInflater inflater = LayoutInflater.from(context);
		View view;
//		if (cursor.getPosition() % 2 == 0) {
			view = inflater.inflate(R.layout.message_list_item_left, viewGroup, false);
			Animation animation = AnimationUtils.loadAnimation(context, R.anim.message_push_in_left);
			animation.setDuration(500);
			view.startAnimation(animation);
//		} else {
//			view = inflater.inflate(R.layout.message_list_item_right, viewGroup, false);
//			Animation animation = AnimationUtils.loadAnimation(context, R.anim.message_push_in_right);
//			animation.setDuration(500);
//			view.startAnimation(animation);
//		}

		// create holder
		ViewHolder holder = new ViewHolder();
		holder.content = (LinearLayout) view.findViewById(R.id.contentLayout);
		holder.author = (TextView) view.findViewById(R.id.message_author);
		holder.avatar = (ImageView) view.findViewById(R.id.avatar);
        holder.avatar.setOnClickListener(onAvatarClickListener);
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
        holder.avatar.setTag(cursor.getString(gplusLinkColumnIndex));
        ImageLoader.getInstance().displayImage(cursor.getString(authorAvatarColumnIndex), holder.avatar, options);
		holder.date.setText(DATE_FORMAT.format(date));
		holder.text.setText(cursor.getString(messageColumnIndex));
		if(cursor.getInt(isAdminColumnIndex) == 1)
			holder.content.setBackgroundColor(context.getResources().getColor(R.color.message_admin_background));
		else
			holder.content.setBackgroundColor(context.getResources().getColor(R.color.message_user_background));
		// int color =
		// context.getResources().getColor(cursor.getInt(isAdminColumnIndex) ==
		// 1 ?
		// R.color.admin_message_text_color :
		// R.color.user_message_text_color);
        //holder.text.setTextColor(color);
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
        gplusLinkColumnIndex = cursor.getColumnIndex(FullMessageTable.GPLUS_LINK_COLUMN);
        isAdminColumnIndex = cursor.getColumnIndex(FullMessageTable.IS_ADMIN_COLUMN);
	}

    private void showLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static String extractMessageAuthor(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();

        return holder.author.getText().toString();
    }

    public static String extractMessageText(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();

        return holder.text.getText().toString();
    }

	private static class ViewHolder {
		public TextView author;
		public ImageView avatar;
		public TextView date;
		public TextView text;
		public LinearLayout content;
	}
}
