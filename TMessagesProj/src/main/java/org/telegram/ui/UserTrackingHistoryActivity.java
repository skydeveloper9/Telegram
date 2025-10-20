/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.UserTrackingController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserTrackingHistoryActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;

    private long userId;
    private TLRPC.User user;
    
    private int headerRow;
    private int currentStatusRow;
    private int headerSectionRow;
    private int historyStartRow;
    private int historyEndRow;
    private int emptyRow;
    private int clearHistoryRow;
    private int infoRow;
    private int rowCount;

    private ArrayList<UserTrackingController.TrackingHistoryItem> historyItems = new ArrayList<>();

    public UserTrackingHistoryActivity(long userId) {
        super();
        this.userId = userId;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        
        user = MessagesController.getInstance(currentAccount).getUser(userId);
        updateRows();
        
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.userTrackingHistoryChanged);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.userTrackingHistoryChanged);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        
        if (user != null) {
            actionBar.setTitle(UserObject.getUserName(user));
            actionBar.setSubtitle(LocaleController.getString("TrackingHistory", R.string.TrackingHistory));
        } else {
            actionBar.setTitle(LocaleController.getString("TrackingHistory", R.string.TrackingHistory));
        }
        
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);
        
        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == clearHistoryRow) {
                // Clear history for this user
                UserTrackingController.getInstance(currentAccount).clearTrackingHistory(userId);
            }
        });

        return fragmentView;
    }

    private void updateRows() {
        rowCount = 0;
        
        headerRow = rowCount++;
        currentStatusRow = rowCount++;
        headerSectionRow = rowCount++;
        
        historyItems = UserTrackingController.getInstance(currentAccount).getTrackingHistory(userId);
        if (!historyItems.isEmpty()) {
            historyStartRow = rowCount;
            rowCount += historyItems.size();
            historyEndRow = rowCount;
            emptyRow = -1;
        } else {
            historyStartRow = -1;
            historyEndRow = -1;
            emptyRow = rowCount++;
        }
        
        clearHistoryRow = rowCount++;
        infoRow = rowCount++;
        
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.userTrackingHistoryChanged) {
            if (args.length > 0 && args[0] instanceof Long) {
                long changedUserId = (Long) args[0];
                if (changedUserId == userId) {
                    updateRows();
                }
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private String getChangeTypeString(int changeType) {
        switch (changeType) {
            case UserTrackingController.CHANGE_TYPE_ONLINE:
                return LocaleController.getString("WentOnline", R.string.WentOnline);
            case UserTrackingController.CHANGE_TYPE_OFFLINE:
                return LocaleController.getString("WentOffline", R.string.WentOffline);
            case UserTrackingController.CHANGE_TYPE_PHOTO:
                return LocaleController.getString("ChangedPhoto", R.string.ChangedPhoto);
            case UserTrackingController.CHANGE_TYPE_PROFILE:
                return LocaleController.getString("ChangedProfile", R.string.ChangedProfile);
            default:
                return LocaleController.getString("ChangedStatus", R.string.ChangedStatus);
        }
    }

    private String getCurrentStatusString() {
        if (user == null) {
            return LocaleController.getString("Loading", R.string.Loading);
        }
        
        if (user.status != null && user.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime()) {
            return LocaleController.getString("Online", R.string.Online);
        } else {
            return LocaleController.getString("Offline", R.string.Offline);
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerRow) {
                        headerCell.setText(LocaleController.getString("CurrentStatus", R.string.CurrentStatus));
                    }
                    break;
                case 1:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == currentStatusRow) {
                        textCell.setText(getCurrentStatusString(), false);
                    } else if (position == clearHistoryRow) {
                        textCell.setTextAndIcon(LocaleController.getString("ClearHistory", R.string.ClearHistory), R.drawable.msg_delete, false);
                        textCell.setColors(Theme.key_text_RedRegular, Theme.key_text_RedRegular);
                    }
                    break;
                case 2:
                    TextDetailCell detailCell = (TextDetailCell) holder.itemView;
                    int index = position - historyStartRow;
                    if (index >= 0 && index < historyItems.size()) {
                        UserTrackingController.TrackingHistoryItem item = historyItems.get(index);
                        String changeType = getChangeTypeString(item.changeType);
                        String timestamp = dateFormat.format(new Date(item.timestamp));
                        
                        String details = "";
                        if (item.changeType == UserTrackingController.CHANGE_TYPE_PROFILE) {
                            details = LocaleController.getString("Changed", R.string.Changed) + ": " + item.newValue;
                        } else if (item.changeType == UserTrackingController.CHANGE_TYPE_PHOTO) {
                            details = LocaleController.getString("ProfilePhotoChanged", R.string.ProfilePhotoChanged);
                        }
                        
                        detailCell.setTextAndValue(changeType, timestamp + (details.isEmpty() ? "" : "\n" + details), false);
                    }
                    break;
                case 3:
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        infoCell.setText(LocaleController.getString("TrackingHistoryInfo", R.string.TrackingHistoryInfo));
                        infoCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else if (position == emptyRow) {
                        infoCell.setText(LocaleController.getString("NoTrackingHistory", R.string.NoTrackingHistory));
                        infoCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 1 && holder.getAdapterPosition() == clearHistoryRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new TextDetailCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 4:
                default:
                    view = new View(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
                    view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(12)));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) {
                return 0;
            } else if (position == currentStatusRow || position == clearHistoryRow) {
                return 1;
            } else if (position >= historyStartRow && position < historyEndRow) {
                return 2;
            } else if (position == infoRow || position == emptyRow) {
                return 3;
            } else if (position == headerSectionRow) {
                return 4;
            }
            return 0;
        }
    }
}
