# User Tracking Feature - Integration Guide for Telegram Codebase

## Overview
This document provides specific guidance on where and how to integrate the user tracking feature into the Telegram Android codebase.

## File-by-File Integration Guide

### 1. Core Controller

#### File: `UserTrackingController.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/messenger/UserTrackingController.java`
**Status**: ✅ Created
**Purpose**: Main tracking logic controller

**Key Integration Points:**
- Extends `BaseController` (standard Telegram pattern)
- Uses `getInstance(account)` singleton pattern
- Stores data temporarily in SharedPreferences
- Will use MessagesStorage database when fully implemented

**Methods to call from other parts of code:**
```java
// Initialize and load tracked users
UserTrackingController.getInstance(currentAccount).loadTrackedUsers();

// Check user for changes (call when user data updates)
UserTrackingController.getInstance(currentAccount).checkUserChanges(userId);

// Add/remove tracked users
UserTrackingController.getInstance(currentAccount).addTrackedUser(userId);
UserTrackingController.getInstance(currentAccount).removeTrackedUser(userId);
```

**Where to hook into Telegram's update system:**
In `MessagesController.java`, find where user updates are processed and add:
```java
// When a user object is updated
if (UserTrackingController.getInstance(currentAccount).isUserTracked(userId)) {
    UserTrackingController.getInstance(currentAccount).checkUserChanges(userId);
}
```

---

### 2. UI Activities

#### File: `UserTrackingActivity.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/ui/UserTrackingActivity.java`
**Status**: ✅ Created
**Purpose**: Main tracking list screen

**Integration Pattern:**
- Extends `BaseFragment` (Telegram's fragment base class)
- Uses `RecyclerListView` with custom adapter
- Uses standard Telegram cells: `HeaderCell`, `TextCell`, `UserCell`, `TextInfoPrivacyCell`
- Observes `NotificationCenter` events

**Launched from:**
- Side menu (DrawerLayoutAdapter) via LaunchActivity
- Can also be launched directly: `presentFragment(new UserTrackingActivity())`

#### File: `UserTrackingHistoryActivity.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/ui/UserTrackingHistoryActivity.java`
**Status**: ✅ Created
**Purpose**: Individual user history screen

**Integration Pattern:**
- Same pattern as UserTrackingActivity
- Requires userId in constructor
- Shows detailed history timeline

**Launched from:**
- UserTrackingActivity when user taps on tracked user
- Usage: `presentFragment(new UserTrackingHistoryActivity(userId))`

---

### 3. Menu Integration

#### File: `DrawerLayoutAdapter.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/ui/Adapters/DrawerLayoutAdapter.java`
**Status**: ✅ Modified
**Changes**: Added menu item with ID 18

**Integration Point:**
```java
// In resetItems() method, around line 342
items.add(new Item(11, LocaleController.getString(R.string.SavedMessages), savedIcon));
items.add(new Item(18, LocaleController.getString(R.string.UserTracking), R.drawable.msg_permissions)); // ← NEW
items.add(new Item(8, LocaleController.getString(R.string.Settings), settingsIcon));
```

**Menu Position:**
- After "Saved Messages"
- Before "Settings"
- Icon: `R.drawable.msg_permissions` (existing Telegram icon)

---

### 4. Navigation Handler

#### File: `LaunchActivity.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/ui/LaunchActivity.java`
**Status**: ✅ Modified
**Changes**: Added case 18 handler

**Integration Point:**
```java
// In drawer item click handler, around line 705
} else if (id == 17) {
    // Stories code...
} else if (id == 18) {  // ← NEW
    presentFragment(new UserTrackingActivity());
    drawerLayoutContainer.closeDrawer(false);
}
```

**Pattern Explanation:**
- Each drawer menu item has an ID
- Click handler switches on ID
- Presents appropriate fragment
- Closes drawer after navigation

---

### 5. Notification System

#### File: `NotificationCenter.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/messenger/NotificationCenter.java`
**Status**: ✅ Modified
**Changes**: Added 2 new notification constants

**Integration Point:**
```java
// Around line 369, after botForumDraftDelete
public static final int botForumDraftDelete = totalEvents++;
public static final int userTrackingListChanged = totalEvents++;      // ← NEW
public static final int userTrackingHistoryChanged = totalEvents++;   // ← NEW
```

**Usage Pattern:**
```java
// To notify that tracking list changed
NotificationCenter.getInstance(currentAccount)
    .postNotificationName(NotificationCenter.userTrackingListChanged);

// To notify that history changed for a user
NotificationCenter.getInstance(currentAccount)
    .postNotificationName(NotificationCenter.userTrackingHistoryChanged, userId);
```

**Observers:**
- `UserTrackingActivity` listens for `userTrackingListChanged`
- `UserTrackingHistoryActivity` listens for `userTrackingHistoryChanged`

---

### 6. String Resources

#### File: `strings.xml`
**Location**: `/TMessagesProj/src/main/res/values/strings.xml`
**Status**: ✅ Modified
**Changes**: Added 20+ new strings

**Integration Pattern:**
All strings follow Telegram's naming convention:
- PascalCase for keys
- Descriptive names
- Proper formatting with %s for parameters

**Example Usage:**
```java
String title = LocaleController.getString("UserTracking", R.string.UserTracking);
String message = LocaleController.formatString("RemoveUserFromTracking", 
    R.string.RemoveUserFromTracking, userName);
```

**Added Strings:**
- `UserTracking` - Menu title
- `TrackedUsers` - Section header
- `AddUserToTrack` - Button text
- `TrackingHistory` - History title
- `WentOnline`, `WentOffline` - Status changes
- `ChangedPhoto`, `ChangedProfile` - Change types
- Info messages and help text

---

## Database Integration (Future Enhancement)

### Where to Add Tables

#### File: `MessagesStorage.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/messenger/MessagesStorage.java`
**Status**: ⚠️ Not yet modified (using SharedPreferences temporarily)

**Where to Add:**
In the `createDatabase()` method, around line 530-600, add:

```java
database.executeFast("CREATE TABLE IF NOT EXISTS tracked_users(" +
    "user_id INTEGER PRIMARY KEY, " +
    "last_first_name TEXT, " +
    "last_last_name TEXT, " +
    "last_username TEXT, " +
    "last_bio TEXT, " +
    "last_photo_id INTEGER, " +
    "last_online_time INTEGER, " +
    "last_online_status INTEGER, " +
    "added_time INTEGER)").stepThis().dispose();

database.executeFast("CREATE TABLE IF NOT EXISTS tracking_history(" +
    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
    "user_id INTEGER, " +
    "change_type INTEGER, " +
    "old_value TEXT, " +
    "new_value TEXT, " +
    "timestamp INTEGER)").stepThis().dispose();

database.executeFast("CREATE INDEX IF NOT EXISTS tracking_history_user_idx " +
    "ON tracking_history(user_id, timestamp DESC)").stepThis().dispose();
```

#### File: `DatabaseMigrationHelper.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/messenger/DatabaseMigrationHelper.java`
**Status**: ⚠️ Not yet modified

**Where to Add:**
In `migrate()` method, add version check and migration:

```java
if (version < YOUR_NEW_VERSION) {
    database.executeFast("CREATE TABLE IF NOT EXISTS tracked_users...").stepThis().dispose();
    database.executeFast("CREATE TABLE IF NOT EXISTS tracking_history...").stepThis().dispose();
    // Migrate data from SharedPreferences if exists
    version = YOUR_NEW_VERSION;
}
```

---

## Background Monitoring Integration

### Where to Hook User Updates

#### File: `MessagesController.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/messenger/MessagesController.java`
**Where**: In methods that process user updates

**Example Integration Points:**

1. **When user data is updated from server:**
```java
// In processLoadedUsers() or similar method
public void processLoadedUsers(ArrayList<TLRPC.User> users, ...) {
    for (TLRPC.User user : users) {
        // ... existing code ...
        
        // Add tracking check
        if (UserTrackingController.getInstance(currentAccount).isUserTracked(user.id)) {
            UserTrackingController.getInstance(currentAccount).checkUserChanges(user.id);
        }
    }
}
```

2. **When online status changes:**
```java
// In updateUserStatus() or onUpdateUserStatus()
private void updateUserStatus(int userId, TLRPC.UserStatus status) {
    // ... existing code ...
    
    // Add tracking check
    if (UserTrackingController.getInstance(currentAccount).isUserTracked(userId)) {
        UserTrackingController.getInstance(currentAccount).checkUserChanges(userId);
    }
}
```

3. **When user profile photo changes:**
```java
// In processUpdateUserPhoto() or similar
private void processUpdateUserPhoto(TLRPC.User user) {
    // ... existing code ...
    
    // Add tracking check
    if (UserTrackingController.getInstance(currentAccount).isUserTracked(user.id)) {
        UserTrackingController.getInstance(currentAccount).checkUserChanges(user.id);
    }
}
```

---

## Notification Integration (Future Enhancement)

### System Notifications

#### File: `NotificationsController.java`
**Location**: `/TMessagesProj/src/main/java/org/telegram/messenger/NotificationsController.java`
**Status**: ⚠️ Not yet integrated (currently using FileLog)

**Where to Add:**
Create a new method in NotificationsController:

```java
public void showUserTrackingNotification(long userId, String userName, String changeText) {
    // Create notification channel if not exists
    // Build notification
    // Show notification
    // Follow pattern from showNotification() or similar methods
}
```

**Call from:**
```java
// In UserTrackingController.sendNotification()
NotificationsController.getInstance(currentAccount)
    .showUserTrackingNotification(userId, userName, notificationText);
```

---

## Initialization

### Application Startup

#### Where to Initialize
In `LaunchActivity.onCreate()` or `MessagesController.onUserConfigLoaded()`:

```java
// Load tracked users when account is ready
UserTrackingController.getInstance(currentAccount).loadTrackedUsers();
```

### Per-Account
The controller is per-account (singleton per account index), so it automatically handles multiple accounts.

---

## Testing Checklist

### Manual Testing Steps:

1. **Menu Integration**
   - [ ] Open side drawer
   - [ ] Verify "User Tracking" appears between "Saved Messages" and "Settings"
   - [ ] Tap "User Tracking" opens the tracking activity

2. **Add User to Tracking**
   - [ ] Tap "Add User to Track"
   - [ ] Select a contact
   - [ ] Verify user appears in tracked list
   - [ ] Verify user is saved (persist after app restart)

3. **View History**
   - [ ] Tap on tracked user
   - [ ] Verify history page opens
   - [ ] Verify current status shows correctly
   - [ ] Verify history list (if any changes tracked)

4. **Remove User**
   - [ ] Long-press on tracked user
   - [ ] Confirm removal dialog
   - [ ] Verify user removed from list

5. **Change Detection**
   - [ ] Track a user
   - [ ] Have them go online/offline
   - [ ] Verify change is logged
   - [ ] Verify notification appears
   - [ ] Check history page shows the change

6. **Profile Changes**
   - [ ] Track a user
   - [ ] Have them change profile photo
   - [ ] Verify photo change is logged
   - [ ] Have them change name
   - [ ] Verify name change is logged

---

## Code Review Checklist

- [ ] All new files follow Telegram's code style
- [ ] No hardcoded strings (all in strings.xml)
- [ ] All activities extend BaseFragment
- [ ] All controllers extend BaseController
- [ ] Proper use of NotificationCenter for events
- [ ] Memory leaks prevented (proper observer cleanup)
- [ ] Thread safety (UI updates on UI thread)
- [ ] Null checks for user objects
- [ ] Proper activity lifecycle handling
- [ ] Minimal changes to existing files

---

## Summary

### What's Working Now:
✅ Menu integration - appears in side drawer
✅ Basic UI - list and history screens
✅ Controller logic - tracking and change detection
✅ Temporary storage - SharedPreferences
✅ Event system - NotificationCenter integration
✅ String resources - all text localized

### What Needs Integration for Production:
⚠️ Database tables - persistent storage
⚠️ Background monitoring - hook into user updates
⚠️ System notifications - full notification support
⚠️ Testing - comprehensive testing needed

### Minimal Changes Made:
- DrawerLayoutAdapter: +1 line (menu item)
- LaunchActivity: +3 lines (click handler)
- NotificationCenter: +2 lines (event constants)
- strings.xml: +22 lines (strings)
- 3 new files (controller + 2 activities)
- 2 documentation files

This keeps the feature isolated and easy to maintain!
