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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserTrackingController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class UserTrackingActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;

    private int headerRow;
    private int addUserRow;
    private int headerSectionRow;
    private int trackedUsersStartRow;
    private int trackedUsersEndRow;
    private int emptyRow;
    private int infoRow;
    private int rowCount;

    private ArrayList<Long> trackedUserIds = new ArrayList<>();

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        
        UserTrackingController.getInstance(currentAccount).loadTrackedUsers();
        updateRows();
        
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.userTrackingListChanged);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.userTrackingListChanged);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("UserTracking", R.string.UserTracking));
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
            if (position == addUserRow) {
                // Open contact selector to add new tracked user
                presentFragment(new ContactsActivity(null) {
                    @Override
                    public void didSelectContact(TLRPC.User user, String param, ContactsActivity activity) {
                        if (user != null) {
                            UserTrackingController.getInstance(currentAccount).addTrackedUser(user.id);
                        }
                    }
                });
            } else if (position >= trackedUsersStartRow && position < trackedUsersEndRow) {
                // Show history for this user
                int index = position - trackedUsersStartRow;
                if (index >= 0 && index < trackedUserIds.size()) {
                    long userId = trackedUserIds.get(index);
                    presentFragment(new UserTrackingHistoryActivity(userId));
                }
            }
        });
        
        listView.setOnItemLongClickListener((view, position) -> {
            if (position >= trackedUsersStartRow && position < trackedUsersEndRow) {
                int index = position - trackedUsersStartRow;
                if (index >= 0 && index < trackedUserIds.size()) {
                    long userId = trackedUserIds.get(index);
                    TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(userId);
                    if (user != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("UserTracking", R.string.UserTracking));
                        builder.setMessage(LocaleController.formatString("RemoveUserFromTracking", R.string.RemoveUserFromTracking, ContactsController.formatName(user.first_name, user.last_name)));
                        builder.setPositiveButton(LocaleController.getString("Remove", R.string.Remove), (dialogInterface, i) -> {
                            UserTrackingController.getInstance(currentAccount).removeTrackedUser(userId);
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                        return true;
                    }
                }
            }
            return false;
        });

        return fragmentView;
    }

    private void updateRows() {
        rowCount = 0;
        
        headerRow = rowCount++;
        addUserRow = rowCount++;
        headerSectionRow = rowCount++;
        
        trackedUserIds = UserTrackingController.getInstance(currentAccount).getTrackedUserIds();
        if (!trackedUserIds.isEmpty()) {
            trackedUsersStartRow = rowCount;
            rowCount += trackedUserIds.size();
            trackedUsersEndRow = rowCount;
            emptyRow = -1;
        } else {
            trackedUsersStartRow = -1;
            trackedUsersEndRow = -1;
            emptyRow = rowCount++;
        }
        
        infoRow = rowCount++;
        
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.userTrackingListChanged) {
            updateRows();
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

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

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
                        headerCell.setText(LocaleController.getString("TrackedUsers", R.string.TrackedUsers));
                    }
                    break;
                case 1:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == addUserRow) {
                        textCell.setTextAndIcon(LocaleController.getString("AddUserToTrack", R.string.AddUserToTrack), R.drawable.msg_addcontact, false);
                        textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    }
                    break;
                case 2:
                    UserCell userCell = (UserCell) holder.itemView;
                    int index = position - trackedUsersStartRow;
                    if (index >= 0 && index < trackedUserIds.size()) {
                        long userId = trackedUserIds.get(index);
                        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(userId);
                        if (user != null) {
                            userCell.setData(user, null, null, 0);
                        }
                    }
                    break;
                case 3:
                    TextInfoPrivacyCell infoCell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == infoRow) {
                        infoCell.setText(LocaleController.getString("UserTrackingInfo", R.string.UserTrackingInfo));
                        infoCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else if (position == emptyRow) {
                        infoCell.setText(LocaleController.getString("NoTrackedUsers", R.string.NoTrackedUsers));
                        infoCell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 1 || type == 2;
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
                    view = new UserCell(mContext, 6, 0, false);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 4:
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) {
                return 0;
            } else if (position == addUserRow) {
                return 1;
            } else if (position >= trackedUsersStartRow && position < trackedUsersEndRow) {
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
