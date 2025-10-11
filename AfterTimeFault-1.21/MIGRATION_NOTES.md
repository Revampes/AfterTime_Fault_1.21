# Forge 1.8.9 to Fabric 1.21+ Migration Notes

## DrawTooltip.java Migration

I've successfully migrated your `drawTooltip.java` file from Forge 1.8.9 to Fabric 1.21+. Here are the key changes:

### Major Changes Made:

1. **Imports Updated:**
   - Added `net.minecraft.client.MinecraftClient` (Fabric's equivalent to Forge's Minecraft)
   - Added `net.minecraft.client.gui.DrawContext` (required for all rendering in 1.21+)
   - Changed to `net.minecraft.client.font.TextRenderer` (replaces FontRenderer)

2. **Constructor Changes:**
   - Now accepts `TextRenderer textRenderer, int screenWidth, int screenHeight`
   - Removed dependency on GUI class for these values

3. **Method Signature Changes:**
   - `render()` now takes `DrawContext context` as first parameter
   - All rendering must go through DrawContext in modern Minecraft

4. **Rendering API Updates:**
   - `fontRenderer.getStringWidth()` → `textRenderer.getWidth()`
   - `gui.drawRect()` → `context.fill()`
   - `fontRenderer.drawStringWithShadow()` → `context.drawTextWithShadow(textRenderer, ...)`

5. **Static Helper Method:**
   - Added `renderTooltip()` static method for easy use without creating an instance
   - Automatically gets MinecraftClient instance and screen dimensions

### Usage Example:

```java
// In your Screen/GUI render method:
@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    // ... your rendering code ...
    
    // Render a tooltip using the static helper:
    DrawTooltip.renderTooltip(context, "This is a tooltip!", mouseX, mouseY);
    
    // OR create an instance for repeated use:
    MinecraftClient client = MinecraftClient.getInstance();
    DrawTooltip tooltipRenderer = new DrawTooltip(
        client.textRenderer,
        client.getWindow().getScaledWidth(),
        client.getWindow().getScaledHeight()
    );
    tooltipRenderer.render(context, "Another tooltip", mouseX, mouseY);
}
```

## Additional Migration Needed

Your `ModSettingsGui.java` file still uses Forge 1.8.9 APIs and needs migration:

### Key Classes to Update:
- `GuiScreen` → `net.minecraft.client.gui.screen.Screen`
- `GuiButton` → `net.minecraft.client.gui.widget.ButtonWidget`
- LWJGL 2.x (`org.lwjgl.input.*`) → GLFW (LWJGL 3.x)
- `FontRenderer` → `TextRenderer`
- Direct GL calls → Use DrawContext methods

### Forge 1.8.9 vs Fabric 1.21+ Screen Comparison:

**Forge 1.8.9:**
```java
public class MyGui extends GuiScreen {
    @Override
    public void initGui() { }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        fontRendererObj.drawString("Text", x, y, color);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) { }
}
```

**Fabric 1.21+:**
```java
public class MyGui extends Screen {
    public MyGui() {
        super(Text.literal("My GUI"));
    }
    
    @Override
    protected void init() { }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawText(textRenderer, "Text", x, y, color, false);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
```

## File Naming Note

The file `drawTooltip.java` should be renamed to `DrawTooltip.java` to match the class name (Java convention).

## Next Steps

1. Rename `drawTooltip.java` → `DrawTooltip.java`
2. Migrate `ModSettingsGui.java` to extend `Screen` instead of `GuiScreen`
3. Update all rendering calls to use `DrawContext`
4. Replace LWJGL 2.x input handling with GLFW
5. Test the tooltip rendering in your mod

