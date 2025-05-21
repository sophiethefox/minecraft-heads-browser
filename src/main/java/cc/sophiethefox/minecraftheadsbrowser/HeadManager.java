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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeadManager {
    public Map<String, List<HeadData>> categorizedHeads = new HashMap<>();

    public List<String> activeCategories;
    public String searchTerm;
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

        GameProfile profile = new GameProfile(headData.uuid != null ? UUID.fromString(headData.uuid) : UUID.randomUUID(), headData.name != null ? headData.name : "Head");

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
        try {
            URL url = URI.create("https://mcheads.sophiethefox.cc/heads.json").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestMethod("GET");
            conn.connect();

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                Type mapType = new TypeToken<Map<String, List<HeadData>>>() {
                }.getType();
                categorizedHeads = gson.fromJson(reader, mapType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Fetching from server failed. Attempting to read local heads.json");
            try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft-heads-browser/heads.json")) {
                if (inputStream == null) {
                    System.out.println("Could not find file!");
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                Type mapType = new TypeToken<Map<String, List<HeadData>>>() {}.getType();

                categorizedHeads = gson.fromJson(reader, mapType);
            } catch (Exception e_) {
                System.out.println("Failed reading local heads.json");
                e_.printStackTrace();
            }
        }
    }
}
