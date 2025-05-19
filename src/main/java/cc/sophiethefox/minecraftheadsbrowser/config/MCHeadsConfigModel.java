package cc.sophiethefox.minecraftheadsbrowser.config;

import cc.sophiethefox.minecraftheadsbrowser.MinecraftHeadsBrowser;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = MinecraftHeadsBrowser.MOD_ID)
@Config(name="minecraft-heads", wrapperName = "MCHeadsConfig")
public class MCHeadsConfigModel {
    public boolean saveCategories = true;
    public boolean savePage = true;
    public boolean saveSearch = true;
    public boolean saveNBTEdit = true;
}
