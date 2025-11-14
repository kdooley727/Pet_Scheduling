# Quick Test Steps - WorkManager Notifications

## 🚀 Ready to Test!

Follow these steps to test notifications right now:

---

## Step 1: Open Logcat in Android Studio

1. **Build and run** your app on a device/emulator
2. In Android Studio, open the **Logcat** panel (bottom of screen)
3. **Filter Logcat** by typing: `NotificationScheduler|ReminderWorker`
   - This will show only notification-related logs

---

## Step 2: Create a Test Task

1. **Open the app** and sign in
2. **Select or create a pet** (if you don't have one)
3. **Navigate to the pet's task list** (tap on a pet)
4. **Tap the "+" button** (FAB) to add a new task
5. **Fill in the form**:
   - **Task Title**: `Test Notification`
   - **Category**: `Feeding` (or any category)
   - **Date**: Today
   - **Time**: Set to **3 minutes from now**
     - Example: If it's 2:00 PM, set to 2:03 PM
   - **Reminder**: `2 minutes before`
   - **Recurrence**: `No Repeat`
6. **Tap "Save Task"**

---

## Step 3: Watch Logcat

**Immediately after saving**, you should see in Logcat:

```
✅ Scheduled notification for task 'Test Notification' (ID: [taskId]) at [time] (1 minutes from now)
```

This confirms the notification was scheduled!

---

## Step 4: Wait for Notification

1. **Wait 1 minute** (2 minutes before the task time)
2. **You should see a notification** appear in the notification bar
3. **The notification should show**:
   - Title: "Reminder: Test Notification"
   - Text: "Time to take care of [Pet Name]!"

**In Logcat**, you should see:
```
🔔 Notification shown for task: 'Test Notification' (Pet: [Pet Name], ID: [taskId])
```

---

## Step 5: Test Notification Tap

1. **Tap the notification**
2. **The app should open** and navigate to the task detail screen

---

## ✅ Success Checklist

- [ ] Logcat shows "✅ Scheduled notification..." when saving task
- [ ] Notification appears after waiting
- [ ] Notification shows correct pet name and task title
- [ ] Tapping notification opens the app
- [ ] Logcat shows "🔔 Notification shown..." when notification appears

---

## 🐛 Troubleshooting

### Notification doesn't appear?

1. **Check Logcat** - Do you see the "✅ Scheduled notification..." message?
   - ✅ Yes → Check if notification time has passed
   - ❌ No → Task might not have been saved properly

2. **Check device settings**:
   - Go to **Settings → Apps → Pet Scheduling → Notifications**
   - Make sure notifications are **enabled**

3. **Check task is active**:
   - Verify the task exists and `isActive = true`
   - Check Logcat for "⚠️ Task is no longer active" messages

4. **Check time**:
   - Make sure reminder time is in the future
   - If task time is too soon, notification might have already fired

### Want to test faster?

Create a task with:
- **Time**: 2 minutes from now
- **Reminder**: 1 minute before
- Wait 1 minute → notification should appear!

---

## 📊 Additional Tests

### Test Recurring Tasks

1. Create a task with:
   - **Time**: 5 minutes from now
   - **Reminder**: 2 minutes before
   - **Recurrence**: Daily
2. Check Logcat - you should see multiple "✅ Scheduled notification..." messages
3. Each one should be for a different day

### Test Task Update

1. Create a task scheduled for 5 minutes from now
2. Wait 1 minute
3. Edit the task and change time to 10 minutes from now
4. Save
5. Check Logcat - should see:
   - `❌ Cancelled all notifications for task ID: [taskId]`
   - `✅ Scheduled notification...` (with new time)

### Test Task Deletion

1. Create a task with notification scheduled
2. Delete the task
3. Check Logcat - should see:
   - `❌ Cancelled all notifications for task ID: [taskId]`
4. Wait - no notification should appear

---

## 🎯 What to Look For

**In Logcat**, you'll see these emoji indicators:
- ✅ = Success (notification scheduled)
- 🔔 = Notification displayed
- ❌ = Cancelled
- ⚠️ = Warning (task inactive, etc.)

**Filter Logcat with**: `NotificationScheduler|ReminderWorker`

---

## 💡 Pro Tip

Keep Logcat open while testing - it will show you exactly what's happening with notifications in real-time!

