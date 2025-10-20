# User Tracking Feature - Code Structure Visualization

## Menu Navigation Flow

```
Main Screen (DialogsActivity)
    │
    ├─ Side Menu (DrawerLayoutAdapter)
    │   │
    │   ├─ My Profile
    │   ├─ New Group
    │   ├─ Contacts
    │   ├─ Calls
    │   ├─ Saved Messages
    │   ├─ [NEW] User Tracking ← (ID: 18)
    │   ├─ Settings
    │   └─ ...
    │
    └─ Click Handler (LaunchActivity)
        │
        └─ case 18 → UserTrackingActivity
```

## User Tracking Activity Flow

```
UserTrackingActivity
    │
    ├─ "Add User to Track" Button
    │   └─→ ContactsActivity (user selection)
    │       └─→ UserTrackingController.addTrackedUser()
    │           └─→ NotificationCenter.userTrackingListChanged
    │
    ├─ Tracked Users List (RecyclerView)
    │   │
    │   ├─ UserCell (user 1) ─ tap ──→ UserTrackingHistoryActivity(user1)
    │   ├─ UserCell (user 2) ─ tap ──→ UserTrackingHistoryActivity(user2)
    │   └─ UserCell (user N) ─ long press ──→ Remove confirmation dialog
    │                                          └─→ UserTrackingController.removeTrackedUser()
    │
    └─ Info Text: "You can track profile changes..."
```

## User Tracking History Activity Flow

```
UserTrackingHistoryActivity(userId)
    │
    ├─ Header: User Name + "Tracking History"
    │
    ├─ Current Status Section
    │   └─ Shows: "Online" or "Offline"
    │
    ├─ History List (RecyclerView)
    │   ├─ TextDetailCell: "Went Online" - timestamp
    │   ├─ TextDetailCell: "Changed Photo" - timestamp
    │   ├─ TextDetailCell: "Changed Profile: username" - timestamp
    │   └─ ...
    │
    └─ Clear History Button
        └─→ UserTrackingController.clearTrackingHistory()
```

## Data Flow

```
User Profile Update (from Telegram servers)
    │
    ↓
MessagesController.processUpdate()
    │
    ↓
NotificationCenter.updateInterfaces
    │
    ↓
UserTrackingController.checkUserChanges()
    │
    ├─→ Compare current data with cached data
    │   │
    │   ├─ If online status changed
    │   │   ├─→ addTrackingHistory(CHANGE_TYPE_ONLINE/OFFLINE)
    │   │   ├─→ sendNotification()
    │   │   └─→ NotificationCenter.userTrackingHistoryChanged
    │   │
    │   ├─ If photo changed
    │   │   ├─→ addTrackingHistory(CHANGE_TYPE_PHOTO)
    │   │   ├─→ sendNotification()
    │   │   └─→ NotificationCenter.userTrackingHistoryChanged
    │   │
    │   └─ If profile changed (name/username)
    │       ├─→ addTrackingHistory(CHANGE_TYPE_PROFILE)
    │       ├─→ sendNotification()
    │       └─→ NotificationCenter.userTrackingHistoryChanged
    │
    └─→ Update cached data
```

## Storage Architecture

### Current Implementation (Temporary)
```
SharedPreferences (MessagesController.getMainSettings())
    │
    └─ "tracked_user_ids" → "123,456,789" (comma-separated)
```

### Future Implementation (Persistent)
```
SQLite Database (MessagesStorage)
    │
    ├─ tracked_users table
    │   ├─ user_id (PRIMARY KEY)
    │   ├─ last_first_name
    │   ├─ last_last_name
    │   ├─ last_username
    │   ├─ last_bio
    │   ├─ last_photo_id
    │   ├─ last_online_time
    │   ├─ last_online_status
    │   └─ added_time
    │
    └─ tracking_history table
        ├─ id (PRIMARY KEY AUTOINCREMENT)
        ├─ user_id
        ├─ change_type (0=online, 1=offline, 2=photo, 3=profile)
        ├─ old_value
        ├─ new_value
        └─ timestamp
```

## Class Relationships

```
UserTrackingController (BaseController)
    │
    ├─ Singleton per account
    ├─ Manages tracked user list
    ├─ Monitors user changes
    ├─ Generates history entries
    └─ Posts notifications
        │
        └─→ NotificationCenter
            ├─ userTrackingListChanged
            └─ userTrackingHistoryChanged

UserTrackingActivity (BaseFragment)
    │
    ├─ ListAdapter (RecyclerListView.SelectionAdapter)
    │   ├─ HeaderCell
    │   ├─ TextCell (Add User button)
    │   ├─ UserCell (for each tracked user)
    │   └─ TextInfoPrivacyCell (info text)
    │
    └─ Observes NotificationCenter events
        ├─ userTrackingListChanged → updateRows()
        └─ updateInterfaces → notifyDataSetChanged()

UserTrackingHistoryActivity (BaseFragment)
    │
    ├─ ListAdapter (RecyclerListView.SelectionAdapter)
    │   ├─ HeaderCell
    │   ├─ TextCell (current status, clear button)
    │   ├─ TextDetailCell (for each history item)
    │   └─ TextInfoPrivacyCell (info text)
    │
    └─ Observes NotificationCenter events
        └─ userTrackingHistoryChanged → updateRows()
```

## Change Detection Logic

```
checkUserChanges(userId):
    │
    1. Get current user data from MessagesController
    2. Get cached data from trackingDataMap
    3. Compare values:
       │
       ├─ Online Status
       │   ├─ Current: user.status.expires > currentTime
       │   └─ Cached: data.lastOnlineStatus
       │
       ├─ Photo ID
       │   ├─ Current: user.photo.photo_id
       │   └─ Cached: data.lastPhotoId
       │
       └─ Profile Data
           ├─ Current: user.first_name, user.last_name, user.username
           └─ Cached: data.lastFirstName, data.lastLastName, data.lastUsername
    │
    4. For each difference:
       ├─ Create history entry
       ├─ Send notification
       └─ Update cached data
```

## Integration Points

### Existing Telegram Components Used:
- `BaseController` - Base class for controllers
- `BaseFragment` - Base class for UI activities
- `MessagesController` - User data management
- `NotificationCenter` - Event bus
- `RecyclerListView` - List views
- `ContactsActivity` - User selection
- `UserCell` - User display cell
- `Theme` - UI theming

### New Components Added:
- `UserTrackingController` - Tracking logic
- `UserTrackingActivity` - Main UI
- `UserTrackingHistoryActivity` - History UI
- Notification Center events (2)
- String resources (20+)
- Menu item (1)

## Event Sequence Example

```
User Scenario: User goes online
────────────────────────────────

1. Telegram receives status update from server
   └─→ MessagesController.processUpdate()

2. MessagesController updates user object
   └─→ TLRPC.User.status.expires = newValue

3. NotificationCenter posts update
   └─→ NotificationCenter.updateInterfaces

4. UserTrackingController receives notification
   └─→ checkUserChanges(userId)

5. Detects online status change
   ├─→ old: false (offline)
   └─→ new: true (online)

6. Creates history entry
   └─→ TrackingHistoryItem(CHANGE_TYPE_ONLINE, "offline", "online")

7. Posts notification
   └─→ NotificationCenter.userTrackingHistoryChanged(userId, ...)

8. Sends user notification
   └─→ FileLog.d("UserTracking: [User] went online")

9. Updates cached data
   └─→ trackingDataMap.get(userId).lastOnlineStatus = true

10. UI updates (if visible)
    └─→ UserTrackingHistoryActivity receives notification
        └─→ updateRows()
            └─→ listAdapter.notifyDataSetChanged()
```

## File Organization

```
/Telegram
  │
  ├─ /TMessagesProj/src/main/java/org/telegram
  │   │
  │   ├─ /messenger
  │   │   ├─ UserTrackingController.java ← NEW
  │   │   ├─ NotificationCenter.java ← MODIFIED (2 constants added)
  │   │   ├─ MessagesController.java (uses for user data)
  │   │   └─ MessagesStorage.java (future: will add tables)
  │   │
  │   └─ /ui
  │       ├─ UserTrackingActivity.java ← NEW
  │       ├─ UserTrackingHistoryActivity.java ← NEW
  │       ├─ LaunchActivity.java ← MODIFIED (1 case added)
  │       │
  │       └─ /Adapters
  │           └─ DrawerLayoutAdapter.java ← MODIFIED (1 menu item)
  │
  ├─ /TMessagesProj/src/main/res/values
  │   └─ strings.xml ← MODIFIED (20+ strings added)
  │
  └─ USER_TRACKING_FEATURE.md ← NEW (documentation)
```
