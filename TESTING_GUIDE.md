# AutoFish Testing Guide

## ✅ Fixed: Instant Reel-In Issue

### What Was Fixed
The mod was instantly reeling in the rod when enabled. Now it properly:
1. ⏱️ Waits for bobber to settle in water (1 second)
2. 🎣 Only detects actual fish bites (strong downward pull)
3. ✅ Ignores normal bobber floating motion

### How to Test

#### 1. **Load the Game**
The mod has been built and should be in your mods folder automatically (via copyMod task).

#### 2. **Enable in Config**
- Press **Right Shift** to open Config GUI
- Navigate to **Fishing** category  
- Toggle **"Auto Fish"** to **ON**

#### 3. **Configure Settings (Optional)**
Adjust these settings to your preference:
- **Throw If No Hook**: ON (will auto-throw)
- **Rethrow**: ON (auto-rethrow after catch)
- **Throw Cooldown**: 1-2 seconds recommended
- **Rethrow Timeout**: 30 seconds
- **Show Timer**: ON (to see it working)

#### 4. **Test Basic Functionality**

**Test 1: Manual Throw**
1. Hold a fishing rod
2. Press **F** to toggle AutoFish ON
3. Manually throw your rod (right-click)
4. **EXPECTED**: Rod stays in water, doesn't reel in immediately
5. Wait for fish bite
6. **EXPECTED**: Auto-reels when fish bites

**Test 2: Auto Throw**
1. Enable "Throw If No Hook" in config
2. Hold fishing rod
3. Press **F** to toggle ON
4. **EXPECTED**: Automatically throws rod
5. Waits for fish
6. Auto-catches and re-throws

**Test 3: Detection Accuracy**
1. Throw rod normally with AutoFish ON
2. Watch the bobber float
3. **EXPECTED**: Should NOT reel during normal floating
4. Wait for actual fish bite (bobber dips)
5. **EXPECTED**: Should reel when bite happens

#### 5. **Check HUD Display**
When "Show Timer" is enabled, you should see:
```
AutoFish: 00:15
Fish: 3
Status: Waiting...
```

Position adjustable in config via Timer X/Y sliders.

### Expected Behavior

| Scenario | Old Behavior | New Behavior |
|----------|-------------|--------------|
| Enable module | Instant reel | Nothing (waits) |
| Throw rod | Instant reel | Waits 1s then monitors |
| Bobber floating | Instant reel | Ignores (no reel) |
| Fish bites | Might catch | Catches reliably |
| Auto throw | Breaks immediately | Works correctly |

### Debug Checklist

✅ **If rod still reels instantly:**
- Check that bobberStableTime logic is working
- Verify velocity threshold (-0.35)
- Make sure isTouchingWater() returns true

✅ **If not catching fish:**
- Velocity threshold might be too strict
- Increase from -0.35 to -0.30 if needed
- Check that bobberStableTime reaches > 20

✅ **If catching too early:**
- Increase bobberStableTime threshold from 20 to 30-40
- Increase velocity threshold from -0.35 to -0.40

### Key Thresholds

```java
bobberStableTime > 20      // Must be stable 1 second (20 ticks)
velocityY < -0.35          // Must pull down hard (0.35 blocks/tick)
```

These can be adjusted if needed for your server/playstyle.

### Configuration Quick Reference

**Must Be Enabled:**
- ✅ Auto Fish (main toggle)
- ✅ Hold fishing rod in main hand

**Recommended Settings:**
- ✅ Throw If No Hook: ON
- ✅ Rethrow: ON  
- ✅ Show Timer: ON
- ⚙️ Throw Cooldown: 1-2s
- ⚙️ Rethrow Timeout: 30s

**Optional:**
- Sneak Hold: For stealth
- Auto Shift: Anti-AFK
- Slug Mode: Quiet operation
- Messages: Status updates

### File Locations

**Built Mod:**
```
C:\Users\user\Downloads\AfterTime_Fault_1.21\build\libs\
  ↳ AfterTimeFault-1.0.jar
```

**Auto-copied to:**
```
C:\Users\user\AppData\Roaming\PrismLauncher\instances\1.21.8\minecraft\mods\
```

### Support

If issues persist:
1. Check `logs/latest.log` for errors
2. Verify config settings in game
3. Test with different velocity thresholds
4. Ensure fishing rod is in main hand
5. Check that bobber lands in water (not ground)

## Summary

✅ **Fixed**: Instant reel-in bug  
✅ **Added**: 1-second stabilization period  
✅ **Improved**: Fish bite detection accuracy  
✅ **Tested**: Compiles and builds successfully  

The mod is ready to test! Try it in-game and fish should now be caught properly without false triggers. 🎣

