/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UserTrackingController extends BaseController {

    private static volatile UserTrackingController[] Instance = new UserTrackingController[UserConfig.MAX_ACCOUNT_COUNT];
    private static final Object[] lockObjects = new Object[UserConfig.MAX_ACCOUNT_COUNT];

    static {
        for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
            lockObjects[i] = new Object();
        }
    }

    public static final int CHANGE_TYPE_ONLINE = 0;
    public static final int CHANGE_TYPE_OFFLINE = 1;
    public static final int CHANGE_TYPE_PHOTO = 2;
    public static final int CHANGE_TYPE_PROFILE = 3;

    private HashSet<Long> trackedUserIds = new HashSet<>();
    private HashMap<Long, UserTrackingData> trackingDataMap = new HashMap<>();
    private boolean loadedFromDatabase = false;

    public static class UserTrackingData {
        public long userId;
        public String lastFirstName;
        public String lastLastName;
        public String lastUsername;
        public String lastBio;
        public long lastPhotoId;
        public long lastOnlineTime;
        public boolean lastOnlineStatus;
    }

    public static class TrackingHistoryItem {
        public long id;
        public long userId;
        public int changeType;
        public String oldValue;
        public String newValue;
        public long timestamp;
    }

    public UserTrackingController(int num) {
        super(num);
    }

    public static UserTrackingController getInstance(int num) {
        UserTrackingController localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (lockObjects[num]) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new UserTrackingController(num);
                }
            }
        }
        return localInstance;
    }

    public void loadTrackedUsers() {
        if (loadedFromDatabase) {
            return;
        }
        getMessagesStorage().getStorageQueue().postRunnable(() -> {
            ArrayList<Long> userIds = new ArrayList<>();
            HashMap<Long, UserTrackingData> dataMap = new HashMap<>();
            
            try {
                // Load tracked users from database
                // This will be implemented when we add the database table
                SharedPreferences prefs = MessagesController.getMainSettings(currentAccount);
                String trackedIdsStr = prefs.getString("tracked_user_ids", "");
                if (!TextUtils.isEmpty(trackedIdsStr)) {
                    String[] ids = trackedIdsStr.split(",");
                    for (String id : ids) {
                        try {
                            long userId = Long.parseLong(id);
                            userIds.add(userId);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            
            AndroidUtilities.runOnUIThread(() -> {
                trackedUserIds.clear();
                trackedUserIds.addAll(userIds);
                trackingDataMap.clear();
                trackingDataMap.putAll(dataMap);
                loadedFromDatabase = true;
                
                // Start monitoring tracked users
                for (Long userId : trackedUserIds) {
                    checkUserChanges(userId);
                }
            });
        });
    }

    public void addTrackedUser(long userId) {
        if (trackedUserIds.contains(userId)) {
            return;
        }
        
        trackedUserIds.add(userId);
        
        // Save to preferences temporarily
        saveTrackedUsers();
        
        // Initialize tracking data
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user != null) {
            UserTrackingData data = new UserTrackingData();
            data.userId = userId;
            data.lastFirstName = user.first_name;
            data.lastLastName = user.last_name;
            data.lastUsername = user.username;
            data.lastBio = "";
            data.lastPhotoId = user.photo != null ? user.photo.photo_id : 0;
            data.lastOnlineStatus = isUserOnline(user);
            data.lastOnlineTime = System.currentTimeMillis();
            trackingDataMap.put(userId, data);
            
            // Start monitoring
            checkUserChanges(userId);
        }
        
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.userTrackingListChanged);
    }

    public void removeTrackedUser(long userId) {
        trackedUserIds.remove(userId);
        trackingDataMap.remove(userId);
        saveTrackedUsers();
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.userTrackingListChanged);
    }

    public boolean isUserTracked(long userId) {
        return trackedUserIds.contains(userId);
    }

    public ArrayList<Long> getTrackedUserIds() {
        return new ArrayList<>(trackedUserIds);
    }

    private void saveTrackedUsers() {
        StringBuilder sb = new StringBuilder();
        for (Long userId : trackedUserIds) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(userId);
        }
        SharedPreferences prefs = MessagesController.getMainSettings(currentAccount);
        prefs.edit().putString("tracked_user_ids", sb.toString()).apply();
    }

    public void checkUserChanges(long userId) {
        if (!trackedUserIds.contains(userId)) {
            return;
        }
        
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user == null) {
            return;
        }
        
        UserTrackingData data = trackingDataMap.get(userId);
        if (data == null) {
            data = new UserTrackingData();
            data.userId = userId;
            data.lastFirstName = user.first_name;
            data.lastLastName = user.last_name;
            data.lastUsername = user.username;
            data.lastPhotoId = user.photo != null ? user.photo.photo_id : 0;
            data.lastOnlineStatus = isUserOnline(user);
            data.lastOnlineTime = System.currentTimeMillis();
            trackingDataMap.put(userId, data);
            return;
        }
        
        // Check for online status change
        boolean currentOnlineStatus = isUserOnline(user);
        if (currentOnlineStatus != data.lastOnlineStatus) {
            if (currentOnlineStatus) {
                addTrackingHistory(userId, CHANGE_TYPE_ONLINE, "offline", "online");
                sendNotification(userId, "went online");
            } else {
                addTrackingHistory(userId, CHANGE_TYPE_OFFLINE, "online", "offline");
                sendNotification(userId, "went offline");
            }
            data.lastOnlineStatus = currentOnlineStatus;
            data.lastOnlineTime = System.currentTimeMillis();
        }
        
        // Check for photo change
        long currentPhotoId = user.photo != null ? user.photo.photo_id : 0;
        if (currentPhotoId != data.lastPhotoId) {
            addTrackingHistory(userId, CHANGE_TYPE_PHOTO, 
                String.valueOf(data.lastPhotoId), String.valueOf(currentPhotoId));
            sendNotification(userId, "changed profile photo");
            data.lastPhotoId = currentPhotoId;
        }
        
        // Check for profile changes
        boolean profileChanged = false;
        StringBuilder changes = new StringBuilder();
        
        if (!TextUtils.equals(user.first_name, data.lastFirstName)) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("first name");
            profileChanged = true;
            data.lastFirstName = user.first_name;
        }
        
        if (!TextUtils.equals(user.last_name, data.lastLastName)) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("last name");
            profileChanged = true;
            data.lastLastName = user.last_name;
        }
        
        if (!TextUtils.equals(user.username, data.lastUsername)) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("username");
            profileChanged = true;
            data.lastUsername = user.username;
        }
        
        if (profileChanged) {
            addTrackingHistory(userId, CHANGE_TYPE_PROFILE, "", changes.toString());
            sendNotification(userId, "changed " + changes.toString());
        }
    }

    private boolean isUserOnline(TLRPC.User user) {
        if (user == null || user.status == null) {
            return false;
        }
        return user.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime();
    }

    private void addTrackingHistory(long userId, int changeType, String oldValue, String newValue) {
        // Store in database and notify
        NotificationCenter.getInstance(currentAccount).postNotificationName(
            NotificationCenter.userTrackingHistoryChanged, userId, changeType, oldValue, newValue);
    }

    private void sendNotification(long userId, String changeDescription) {
        TLRPC.User user = getMessagesController().getUser(userId);
        if (user == null) {
            return;
        }
        
        String userName = UserObject.getUserName(user);
        String notificationText = userName + " " + changeDescription;
        
        // For now, just post to notification center
        // Full notification implementation would require more integration
        AndroidUtilities.runOnUIThread(() -> {
            FileLog.d("UserTracking: " + notificationText);
        });
    }

    public ArrayList<TrackingHistoryItem> getTrackingHistory(long userId) {
        ArrayList<TrackingHistoryItem> history = new ArrayList<>();
        // This will load from database when implemented
        return history;
    }

    public void clearTrackingHistory(long userId) {
        // Clear history from database
        NotificationCenter.getInstance(currentAccount).postNotificationName(
            NotificationCenter.userTrackingHistoryChanged, userId);
    }
}
