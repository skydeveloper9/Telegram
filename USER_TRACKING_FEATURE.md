# User Tracking Feature for Telegram

## Overview
This feature allows users to track profile changes, photo changes, and online/offline status of selected contacts.

## Implementation Details

### Architecture
The implementation follows Telegram's existing code patterns:
- Controllers extend `BaseController`
- UI components extend `BaseFragment`
- Data storage uses `MessagesStorage` with SQLite
- UI notifications use `NotificationCenter` pattern
- Views are created programmatically (no XML layouts)

### Components

#### 1. UserTrackingController.java
Location: `/org/telegram/messenger/UserTrackingController.java`

**Purpose**: Manages all tracking logic and data
**Key Methods**:
- `loadTrackedUsers()` - Loads list of tracked users from storage
- `addTrackedUser(userId)` - Adds a user to tracking list
- `removeTrackedUser(userId)` - Removes a user from tracking
- `checkUserChanges(userId)` - Checks for changes in user data
- `getTrackingHistory(userId)` - Returns history of changes

**Tracked Changes**:
- Online status (CHANGE_TYPE_ONLINE, CHANGE_TYPE_OFFLINE)
- Profile photo (CHANGE_TYPE_PHOTO)
- Profile information (CHANGE_TYPE_PROFILE) - name, username, bio

#### 2. UserTrackingActivity.java
Location: `/org/telegram/ui/UserTrackingActivity.java`

**Purpose**: Main UI for viewing and managing tracked users
**Features**:
- Lists all tracked users
- Button to add new users to track
- Long-press to remove users
- Tap to view tracking history
- Empty state when no users tracked

#### 3. UserTrackingHistoryActivity.java
Location: `/org/telegram/ui/UserTrackingHistoryActivity.java`

**Purpose**: Displays history of changes for a specific user
**Features**:
- Shows current online/offline status
- Lists all tracked changes with timestamps
- Option to clear history
- Empty state when no history available

### Integration Points

#### Menu Integration
- **File**: `/org/telegram/ui/Adapters/DrawerLayoutAdapter.java`
- **Menu ID**: 18
- **Location**: Between "Saved Messages" and "Settings" in drawer menu
- **Icon**: `R.drawable.msg_permissions`

#### Navigation
- **File**: `/org/telegram/ui/LaunchActivity.java`
- **Handler**: Case 18 in drawer item click handler
- **Action**: Opens `UserTrackingActivity`

#### Notifications
- **File**: `/org/telegram/messenger/NotificationCenter.java`
- **Constants Added**:
  - `userTrackingListChanged` - Fired when tracking list changes
  - `userTrackingHistoryChanged` - Fired when history is updated

### String Resources
Location: `/res/values/strings.xml`

Added strings:
- `UserTracking` - "User Tracking"
- `TrackedUsers` - "Tracked Users"
- `AddUserToTrack` - "Add User to Track"
- `RemoveUserFromTracking` - "Remove %s from tracking?"
- `TrackingHistory` - "Tracking History"
- `WentOnline` - "Went Online"
- `WentOffline` - "Went Offline"
- `ChangedPhoto` - "Changed Photo"
- `ChangedProfile` - "Changed Profile"
- And more...

## Current Implementation Status

### ✅ Completed
- Core controller logic
- UI activities and fragments
- Menu integration
- String resources
- Notification center events

### ⚠️ Pending
1. **Database Persistence**
   - Tables need to be added to `MessagesStorage` for:
     - `tracked_users` table
     - `tracking_history` table
   - Currently using SharedPreferences as temporary storage

2. **Background Monitoring**
   - Need to hook into existing user update events
   - Subscribe to `NotificationCenter.updateInterfaces`
   - Call `UserTrackingController.checkUserChanges()` periodically

3. **Push Notifications**
   - Integration with `NotificationsController`
   - Custom notification channel for tracking alerts
   - Notification preferences

4. **Testing**
   - Build and compilation
   - Runtime testing
   - Edge cases

## Usage

### For Users
1. Open Telegram
2. Open the side menu (swipe from left or tap menu button)
3. Tap "User Tracking"
4. Tap "Add User to Track"
5. Select a contact
6. User is now being tracked
7. Tap on tracked user to see their history
8. Long-press to remove from tracking

### For Developers

#### Adding to Tracking List
```java
UserTrackingController controller = UserTrackingController.getInstance(currentAccount);
controller.addTrackedUser(userId);
```

#### Checking for Changes
```java
UserTrackingController controller = UserTrackingController.getInstance(currentAccount);
controller.checkUserChanges(userId);
```

#### Getting History
```java
UserTrackingController controller = UserTrackingController.getInstance(currentAccount);
ArrayList<TrackingHistoryItem> history = controller.getTrackingHistory(userId);
```

## Database Schema (To Be Implemented)

### tracked_users table
```sql
CREATE TABLE tracked_users (
    user_id INTEGER PRIMARY KEY,
    last_first_name TEXT,
    last_last_name TEXT,
    last_username TEXT,
    last_bio TEXT,
    last_photo_id INTEGER,
    last_online_time INTEGER,
    last_online_status INTEGER,
    added_time INTEGER
);
```

### tracking_history table
```sql
CREATE TABLE tracking_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    change_type INTEGER,
    old_value TEXT,
    new_value TEXT,
    timestamp INTEGER
);
```

## Future Enhancements
1. Export tracking history
2. Tracking statistics (most active times, change frequency)
3. Notifications with customizable settings per user
4. Track additional fields (phone number, status message)
5. Track group/channel changes
6. Visual timeline view of changes
7. Comparison view (before/after)

## Notes
- The implementation follows Telegram's existing patterns to ensure consistency
- All UI is created programmatically without XML layouts (Telegram's standard)
- The code structure matches existing activities like `NotificationsSettingsActivity`
- Minimal changes to existing files to reduce merge conflicts
