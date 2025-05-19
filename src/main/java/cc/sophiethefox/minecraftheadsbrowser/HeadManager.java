package cc.sophiethefox.minecraftheadsbrowser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeadManager {
    private static final String[] categories = {
            "alphabet", "animals", "blocks", "decoration", "food-drinks",
            "humans", "humanoid", "miscellaneous", "monsters", "plants"
    };
    public Map<String, List<HeadData>> categorizedHeads = new HashMap<>();

    public List<String> activeCategories ;
    public String searchTerm ;
    public Integer page;
    public Boolean nbtEdit;

    public void saveState(List<String> activeCategories, String searchTerm, int page, boolean nbtEdit) {
        this.activeCategories = activeCategories;
        this.searchTerm = searchTerm;
        this.page = page;
        this.nbtEdit = nbtEdit;
    }

    public ItemStack createSkullFromHeadData(HeadData headData) {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);

        GameProfile profile = new GameProfile(
                headData.uuid != null ? UUID.fromString(headData.uuid) : UUID.randomUUID(),
                headData.name != null ? headData.name : "Head"
        );

        profile.getProperties().put("textures", new Property("textures", headData.value));

        ProfileComponent profileComponent = new ProfileComponent(profile);
        skull.set(DataComponentTypes.PROFILE, profileComponent);

        if (headData.name != null) {
            skull.set(DataComponentTypes.CUSTOM_NAME, Text.literal(headData.name));
        }

        return skull;
    }

    public void fetchAllHeads() {
        Gson gson = new Gson();
        for (String cat : categories) {
            try {
                URL url = new URL("https://minecraft-heads.com/scripts/api.php?cat=" + cat);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestMethod("GET");
                conn.connect();
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                    List<HeadData> data = gson.fromJson(reader, new TypeToken<List<HeadData>>() {
                    }.getType());
                    categorizedHeads.put(cat, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
