# Setting Up Your New App Icon

## Step 1: Generate Icon Files Using Android Asset Studio

1. **Go to**: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

2. **Upload your PNG**:
   - Click "Image" tab
   - Upload your `PETPLANNER` icon PNG file
   - The tool will automatically detect the icon

3. **Configure settings**:
   - **Shape**: Choose "Circle" or "Square" (your icon looks good as square)
   - **Padding**: Adjust if needed (usually 0% or small amount)
   - **Background color**: Your gradient should work, but you can set a solid color if needed
   - **Name**: Keep as `ic_launcher` (or change if you prefer)

4. **For Adaptive Icon**:
   - The tool will automatically create foreground/background layers
   - If your icon has a gradient background, you might want to:
     - **Foreground**: The calendar + paw print + text (white elements)
     - **Background**: The gradient background

5. **Download**:
   - Click "Download" button
   - Extract the ZIP file

## Step 2: Replace Icon Files

After downloading, you'll need to:

1. **Extract the ZIP** and find the `res/` folder inside

2. **Copy the mipmap folders** to your project:
   - Copy `mipmap-mdpi/`, `mipmap-hdpi/`, `mipmap-xhdpi/`, `mipmap-xxhdpi/`, `mipmap-xxxhdpi/`
   - Copy `mipmap-anydpi/` (for adaptive icon)
   - Paste into `app/src/main/res/`

3. **Copy drawable files** (for adaptive icon):
   - Copy `drawable/ic_launcher_background.xml` (or PNG)
   - Copy `drawable/ic_launcher_foreground.xml` (or PNG)
   - Paste into `app/src/main/res/drawable/`

4. **Rebuild** your project

## Alternative: Manual Setup

If you prefer to set it up manually or the Asset Studio doesn't work well with your gradient background, let me know and I can help you create the adaptive icon XML files manually.

## After Setup

1. Uninstall the app from your device
2. Rebuild and reinstall
3. Your new icon should appear!

