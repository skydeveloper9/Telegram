# User Tracking Feature - UI Mockup

## Screen 1: Main Menu (Side Drawer)

```
┌────────────────────────────┐
│  ☰  Telegram              │
├────────────────────────────┤
│                            │
│  👤 John Doe               │
│  ⚡ Premium                │
│                            │
├────────────────────────────┤
│                            │
│  👤 My Profile             │
│  🎭 Change Emoji Status    │
│                            │
│  ════════════════════      │
│                            │
│  👥 New Group              │
│  📞 Contacts               │
│  📞 Calls                  │
│  💾 Saved Messages         │
│  👁️  User Tracking   ← NEW │
│  ⚙️  Settings              │
│                            │
│  ════════════════════      │
│                            │
│  👤 Invite Friends         │
│  ❓ Telegram Features      │
│                            │
└────────────────────────────┘
```

## Screen 2: User Tracking Activity (Empty State)

```
┌────────────────────────────┐
│  ←  User Tracking          │
├────────────────────────────┤
│                            │
│  TRACKED USERS             │
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Add User to Track   ││
│  └────────────────────────┘│
│                            │
│  ────────────────────────  │
│                            │
│  No users are being        │
│  tracked yet. Add users    │
│  from the button above.    │
│                            │
│  You can track profile     │
│  changes, photo changes,   │
│  and online/offline status │
│  of selected users. Tap a  │
│  user to see their         │
│  tracking history.         │
│                            │
└────────────────────────────┘
```

## Screen 3: User Tracking Activity (With Tracked Users)

```
┌────────────────────────────┐
│  ←  User Tracking          │
├────────────────────────────┤
│                            │
│  TRACKED USERS             │
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Add User to Track   ││
│  └────────────────────────┘│
│                            │
│  ────────────────────────  │
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Alice Johnson       ││
│  │    Online              ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Bob Smith           ││
│  │    Last seen recently  ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Carol White         ││
│  │    Last seen 2 hours   ││
│  │    ago                 ││
│  └────────────────────────┘│
│                            │
│  You can track profile     │
│  changes, photo changes... │
│                            │
└────────────────────────────┘
```

## Screen 4: Add User (Contact Selector)

```
┌────────────────────────────┐
│  ←  Select Contact         │
├────────────────────────────┤
│  🔍 Search                 │
├────────────────────────────┤
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Alice Johnson    ✓  ││ ← Already tracked
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 👤 David Brown         ││ ← Can be selected
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Emma Wilson         ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Frank Miller        ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 👤 Grace Lee           ││
│  └────────────────────────┘│
│                            │
└────────────────────────────┘
```

## Screen 5: User Tracking History Activity

```
┌────────────────────────────┐
│  ←  Alice Johnson          │
│     Tracking History       │
├────────────────────────────┤
│                            │
│  CURRENT STATUS            │
│                            │
│  ┌────────────────────────┐│
│  │ Online                 ││
│  └────────────────────────┘│
│                            │
│  ────────────────────────  │
│                            │
│  ┌────────────────────────┐│
│  │ Went Online            ││
│  │ Oct 20, 2025 10:30 AM  ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ Went Offline           ││
│  │ Oct 20, 2025 09:15 AM  ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ Changed Photo          ││
│  │ Oct 19, 2025 08:45 PM  ││
│  │ Profile photo was      ││
│  │ changed                ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ Changed Profile        ││
│  │ Oct 19, 2025 02:30 PM  ││
│  │ Changed: username      ││
│  └────────────────────────┘│
│                            │
│  ┌────────────────────────┐│
│  │ 🗑️ Clear History        ││
│  └────────────────────────┘│
│                            │
│  History of all tracked    │
│  changes for this user.    │
│                            │
└────────────────────────────┘
```

## Screen 6: Remove User Confirmation

```
┌────────────────────────────┐
│                            │
│                            │
│  ╔════════════════════════╗│
│  ║  User Tracking         ║│
│  ╠════════════════════════╣│
│  ║                        ║│
│  ║  Remove Alice Johnson  ║│
│  ║  from tracking?        ║│
│  ║                        ║│
│  ╠════════════════════════╣│
│  ║                        ║│
│  ║  ┌──────────────────┐  ║│
│  ║  │ Cancel           │  ║│
│  ║  └──────────────────┘  ║│
│  ║                        ║│
│  ║  ┌──────────────────┐  ║│
│  ║  │ Remove     (Red) │  ║│
│  ║  └──────────────────┘  ║│
│  ║                        ║│
│  ╚════════════════════════╝│
│                            │
│                            │
└────────────────────────────┘
```

## Screen 7: Clear History Confirmation

```
┌────────────────────────────┐
│                            │
│                            │
│  ╔════════════════════════╗│
│  ║  Clear History?        ║│
│  ╠════════════════════════╣│
│  ║                        ║│
│  ║  This will clear all   ║│
│  ║  tracking history for  ║│
│  ║  Alice Johnson.        ║│
│  ║                        ║│
│  ║  This action cannot    ║│
│  ║  be undone.            ║│
│  ║                        ║│
│  ╠════════════════════════╣│
│  ║                        ║│
│  ║  ┌──────────────────┐  ║│
│  ║  │ Cancel           │  ║│
│  ║  └──────────────────┘  ║│
│  ║                        ║│
│  ║  ┌──────────────────┐  ║│
│  ║  │ Clear      (Red) │  ║│
│  ║  └──────────────────┘  ║│
│  ║                        ║│
│  ╚════════════════════════╝│
│                            │
│                            │
└────────────────────────────┘
```

## Interaction Flows

### Flow 1: Add User to Tracking
```
User opens side menu
    ↓
Taps "User Tracking"
    ↓
UserTrackingActivity opens
    ↓
Taps "Add User to Track"
    ↓
ContactsActivity opens (user selector)
    ↓
User selects "David Brown"
    ↓
UserTrackingController.addTrackedUser(davidId)
    ↓
NotificationCenter.userTrackingListChanged fired
    ↓
UserTrackingActivity updates
    ↓
David Brown appears in tracked list
```

### Flow 2: View History
```
User in UserTrackingActivity
    ↓
Taps on "Alice Johnson"
    ↓
UserTrackingHistoryActivity(aliceId) opens
    ↓
Shows current status: "Online"
    ↓
Shows history list:
  - Went Online (10:30 AM)
  - Went Offline (09:15 AM)
  - Changed Photo (Yesterday)
  - Changed Profile (2 days ago)
```

### Flow 3: Remove User
```
User in UserTrackingActivity
    ↓
Long-presses on "Alice Johnson"
    ↓
Confirmation dialog appears
    ↓
User taps "Remove"
    ↓
UserTrackingController.removeTrackedUser(aliceId)
    ↓
NotificationCenter.userTrackingListChanged fired
    ↓
UserTrackingActivity updates
    ↓
Alice Johnson removed from list
```

### Flow 4: Automatic Change Detection
```
Alice Johnson changes profile photo
    ↓
Telegram server sends update
    ↓
MessagesController processes update
    ↓
UserTrackingController.checkUserChanges(aliceId)
    ↓
Detects photo_id changed
    ↓
Creates TrackingHistoryItem(CHANGE_TYPE_PHOTO)
    ↓
Posts NotificationCenter.userTrackingHistoryChanged
    ↓
Logs notification: "Alice Johnson changed profile photo"
    ↓
If UserTrackingHistoryActivity is open:
    → Updates history list
```

## Cell Types Used

### HeaderCell
```
┌────────────────────────────┐
│ TRACKED USERS              │
└────────────────────────────┘
```

### TextCell
```
┌────────────────────────────┐
│ 👤 Add User to Track       │
└────────────────────────────┘
```

### UserCell
```
┌────────────────────────────┐
│ 👤 Alice Johnson           │
│    Online                  │
└────────────────────────────┘
```

### TextDetailCell
```
┌────────────────────────────┐
│ Went Online                │
│ Oct 20, 2025 10:30 AM      │
└────────────────────────────┘
```

### TextInfoPrivacyCell
```
┌────────────────────────────┐
│ You can track profile      │
│ changes, photo changes,    │
│ and online/offline status  │
│ of selected users.         │
└────────────────────────────┘
```

### ShadowSectionCell
```
────────────────────────────
```

## Color Scheme (Follows Telegram Theme)

- **Background**: Theme.key_windowBackgroundGray
- **Cell Background**: Theme.key_windowBackgroundWhite
- **Text**: Theme.key_windowBackgroundWhiteBlackText
- **Accent**: Theme.key_windowBackgroundWhiteBlueButton
- **Red (Remove)**: Theme.key_text_RedRegular
- **Icons**: Theme.key_windowBackgroundWhiteBlueIcon
- **Divider**: Theme.key_windowBackgroundGrayShadow

## Accessibility

- ✅ All buttons have descriptive text
- ✅ All actions have confirmation dialogs
- ✅ Clear visual hierarchy
- ✅ Standard Telegram UI components
- ✅ Supports all Telegram themes
- ✅ Touch targets are standard size
- ✅ Text is readable at all sizes

## Responsive Design

- ✅ Works on all screen sizes
- ✅ RecyclerView handles scrolling
- ✅ Adapts to landscape orientation
- ✅ Supports multi-window mode
- ✅ Handles configuration changes

## Performance

- ✅ Efficient RecyclerView with view recycling
- ✅ Only loads visible items
- ✅ Minimal memory footprint
- ✅ Fast scrolling
- ✅ No UI lag
