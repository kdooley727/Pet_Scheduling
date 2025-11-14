# Testing WorkManager Notifications

## Quick Test Guide

### Test 1: Basic Notification (2 minutes)
**Goal**: Verify notifications work for a simple task

**Steps**:
1. Open the app and navigate to a pet's task list
2. Click "Add Task" (FAB button)
3. Fill in:
   - **Task Title**: "Test Notification"
   - **Category**: Any (e.g., "Feeding")
   - **Date**: Today
   - **Time**: Set to **3 minutes from now** (e.g., if it's 2:00 PM, set to 2:03 PM)
   - **Reminder**: "2 minutes before"
   - **Recurrence**: "No Repeat"
4. Click "Save Task"
5. **Wait 1 minute** - You should see a notification appear

**Expected Result**: 
- Notification appears 1 minute after saving (2 minutes before the task time)
- Notification shows: "Reminder: Test Notification"
- Notification text: "Time to take care of [Pet Name]!"
- Tapping notification opens the app

---

### Test 2: Immediate Notification (for quick testing)
**Goal**: Test notification appears immediately

**Steps**:
1. Create a task with:
   - **Time**: 1 minute from now
   - **Reminder**: "5 minutes before" (this will schedule immediately since reminder time is in the past)
2. Save the task
3. **Note**: For this test, the notification might not appear if reminder time is in the past. Try Test 1 instead.

---

### Test 3: Recurring Task Notifications
**Goal**: Verify multiple notifications are scheduled for recurring tasks

**Steps**:
1. Create a task with:
   - **Task Title**: "Daily Test"
   - **Time**: 5 minutes from now
   - **Reminder**: "2 minutes before"
   - **Recurrence**: "Daily"
2. Save the task
3. Check Logcat for messages like:
   ```
   Scheduled notification for task [taskId] at [time]
   Scheduled notification for task [taskId] at [time] (recurring_1)
   Scheduled notification for task [taskId] at [time] (recurring_2)
   ...
   ```
4. Wait for notifications to appear (should get multiple over the next few days)

**Expected Result**: 
- Multiple notifications scheduled (up to 10 for daily tasks)
- Each notification appears at the correct time

---

### Test 4: Task Update Reschedules Notification
**Goal**: Verify updating a task cancels old and schedules new notification

**Steps**:
1. Create a task scheduled for 5 minutes from now with 2-minute reminder
2. Wait 1 minute (notification should be scheduled for 2 minutes from now)
3. Edit the task and change time to 10 minutes from now
4. Save the task
5. Check Logcat for:
   ```
   Cancelled notifications for task [taskId]
   Scheduled notification for task [taskId] at [new time]
   ```
6. Wait - notification should appear at the new time (8 minutes from now)

**Expected Result**: 
- Old notification cancelled
- New notification scheduled for updated time

---

### Test 5: Task Deletion Cancels Notification
**Goal**: Verify deleting a task cancels its notifications

**Steps**:
1. Create a task scheduled for 5 minutes from now
2. Immediately delete the task
3. Check Logcat for: `Cancelled notifications for task [taskId]`
4. Wait - no notification should appear

**Expected Result**: 
- Notification cancelled
- No notification appears

---

### Test 6: Task Deactivation Cancels Notification
**Goal**: Verify deactivating a task cancels notifications

**Steps**:
1. Create an active task with notification scheduled
2. Deactivate the task (if UI supports this)
3. Check Logcat for cancellation message
4. Wait - no notification should appear

**Expected Result**: 
- Notification cancelled when task deactivated

---

## Debugging Tips

### Check Logcat
Filter by these tags to see notification activity:
- `NotificationScheduler` - Shows when notifications are scheduled/cancelled
- `ReminderWorker` - Shows when notifications are displayed
- `ScheduleViewModel` - Shows task save/update operations

### Check WorkManager Status
You can check scheduled work in Android Studio:
1. Open **Device File Explorer**
2. Navigate to `/data/data/com.hfad.pet_scheduling/databases/`
3. Look for WorkManager database files

Or use ADB:
```bash
adb shell dumpsys jobscheduler | grep pet_scheduling
```

### Common Issues

**Issue**: Notification doesn't appear
- **Check**: Is the task active? (`isActive = true`)
- **Check**: Is reminder time in the future?
- **Check**: Are notifications enabled for the app in device settings?
- **Check**: Is the app in background? (WorkManager works even when app is closed)

**Issue**: Multiple notifications for same task
- **Expected**: This is normal for recurring tasks
- **Check**: Each notification should have a different tag (`recurring_1`, `recurring_2`, etc.)

**Issue**: Notification appears at wrong time
- **Check**: Verify task start time is correct
- **Check**: Verify reminder minutes before setting
- **Check**: Device time zone settings

---

## Quick Verification Commands

### View Scheduled Work (via ADB)
```bash
adb shell dumpsys jobscheduler | grep -A 10 "pet_scheduling"
```

### Clear All Scheduled Work (for testing)
```bash
adb shell pm clear com.hfad.pet_scheduling
```
⚠️ **Warning**: This clears all app data!

### View Logcat
```bash
adb logcat | grep -E "NotificationScheduler|ReminderWorker|ScheduleViewModel"
```

---

## Success Criteria

✅ Notification appears at correct time
✅ Notification shows correct pet name and task title
✅ Tapping notification opens app to task
✅ Recurring tasks schedule multiple notifications
✅ Updating task reschedules notification
✅ Deleting task cancels notification
✅ Deactivating task cancels notification

