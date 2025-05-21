package cc.sophiethefox.minecraftheadsbrowser.gui;

import cc.sophiethefox.minecraftheadsbrowser.HeadData;
import cc.sophiethefox.minecraftheadsbrowser.HeadManager;
import cc.sophiethefox.minecraftheadsbrowser.MinecraftHeadsBrowser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class MCHeadsScreen extends BaseOwoScreen<FlowLayout> implements CommandOpenedScreen{
    private final List<HeadData> headDataList = new ArrayList<>();
    private final int skullsPerPage = 1000;
    private List<String> activeCategories = new ArrayList<>();
    private int page = 0;
    private FlowLayout skullContainer;
    private TextBoxComponent searchComponent;
    private TextBoxComponent setPageComponent;
    private String lastSearch = "";

    private boolean useNbt = false;

    public static String uuidToNBTIntArray(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();

        int i1 = (int) (msb >> 32);
        int i2 = (int) msb;
        int i3 = (int) (lsb >> 32);
        int i4 = (int) lsb;

        return String.format("[I;%d,%d,%d,%d]", i1, i2, i3, i4);
    }

    @Override
    public void init() {
        HeadManager manager = MinecraftHeadsBrowser.headManager;
        if (manager.activeCategories == null || !MinecraftHeadsBrowser.CONFIG.saveCategories()) {
            this.activeCategories.addAll(List.of("alphabet", "animals", "blocks", "decoration", "food-drinks", "humans", "humanoid", "miscellaneous", "monsters", "plants"));
        } else {
            this.activeCategories = manager.activeCategories;
        }

        if (manager.page == null || !MinecraftHeadsBrowser.CONFIG.savePage()) {
            this.page = 0;
        } else {
            this.page = manager.page;
        }

        if (manager.searchTerm != null && MinecraftHeadsBrowser.CONFIG.saveSearch()) {
            this.lastSearch = manager.searchTerm;
        }

        if (manager.nbtEdit != null && MinecraftHeadsBrowser.CONFIG.saveNBTEdit()) {
            this.useNbt = manager.nbtEdit;
        }

        super.init();
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        if (this.headDataList.isEmpty()) {
            this.headDataList.addAll(MinecraftHeadsBrowser.headManager.categorizedHeads.values().stream().flatMap(List::stream).toList());
        }

        rootComponent.surface(Surface.VANILLA_TRANSLUCENT).horizontalAlignment(HorizontalAlignment.LEFT).verticalAlignment(VerticalAlignment.CENTER).padding(Insets.left(this.width / 10).withRight(this.width / 10));

        CollapsibleContainer filtersCollapsible = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal("Categories"), false);
        filtersCollapsible.horizontalAlignment(HorizontalAlignment.LEFT);

        FlowLayout filtersLayout = Containers.ltrTextFlow(Sizing.fill(), Sizing.content());
        filtersLayout.horizontalAlignment(HorizontalAlignment.LEFT);

        Map<String, String> categories = new HashMap<>();
        categories.put("Alphabet", "alphabet");
        categories.put("Animals", "animals");
        categories.put("Blocks", "blocks");
        categories.put("Decoration", "decoration");
        categories.put("Food & Drinks", "food-drinks");
        categories.put("Humans", "humans");
        categories.put("Humanoid", "humanoid");
        categories.put("Misc", "miscellaneous");
        categories.put("Monsters", "monsters");
        categories.put("Plants", "plants");

        for (Map.Entry<String, String> category : categories.entrySet()) {
            SmallCheckboxComponent checkboxComponent = Components.smallCheckbox(Text.of(category.getKey())).checked(this.activeCategories.contains(category.getValue()));
            checkboxComponent.onChanged().subscribe((checked -> {
                if (checked) {
                    this.activeCategories.add(category.getValue());
                } else {
                    this.activeCategories.remove(category.getValue());
                }
                this.updatePage("1");
                MinecraftHeadsBrowser.headManager.saveState(this.activeCategories, this.searchComponent.getText(), this.page, this.useNbt);
                updateSkulls(this.activeCategories, this.searchComponent.getText());
            }));
            filtersLayout.child(checkboxComponent);
        }

        filtersCollapsible.child(filtersLayout);

        this.skullContainer = Containers.ltrTextFlow(Sizing.fill(), Sizing.content());
        this.skullContainer.padding(Insets.of(5)).allowOverflow(false).surface(Surface.PANEL);
        updateSkullsContainer();

        ScrollContainer<FlowLayout> skullScrollContainer = Containers.verticalScroll(Sizing.content(), Sizing.fill(70), skullContainer);
        skullScrollContainer.scrollbarThiccness(4).scrollbar(ScrollContainer.Scrollbar.vanillaFlat()).surface(Surface.PANEL);

        ButtonComponent prevPageButton = Components.button(Text.of("Prev"), buttonComponent -> {
            this.prevPage();
        });
        ButtonComponent nextPageButton = Components.button(Text.of("Next"), buttonComponent -> {
            this.nextPage();
        });
        setPageComponent = Components.textBox(Sizing.expand(10), String.valueOf(this.page + 1));

        FlowLayout searchComponents = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        searchComponents.verticalAlignment(VerticalAlignment.CENTER);
        LabelComponent searchComponentLabel = Components.label(Text.of("Search:"));

        this.searchComponent = Components.textBox(Sizing.expand(10), this.lastSearch);
        this.searchComponent.onChanged().subscribe((s -> {
            this.setPageComponent.setText("1");
            this.page = 0;
            MinecraftHeadsBrowser.headManager.saveState(this.activeCategories, this.searchComponent.getText().toLowerCase(), this.page, this.useNbt);
            this.updateSkulls(this.activeCategories, s.toLowerCase());
        }));

        searchComponents.child(searchComponentLabel);
        searchComponents.child(searchComponent);

        SmallCheckboxComponent useNbtCheckbox = Components.smallCheckbox(Text.of("Modify NBT")).checked(this.useNbt);
        useNbtCheckbox.tooltip(Text.of("Hold a player head in hand. \nThis feature will overwrite the NBT to set the skin. \nCreative mode required. \nMay not be allowed on all servers."));
        useNbtCheckbox.onChanged().subscribe((b -> {
            this.useNbt = b;
            MinecraftHeadsBrowser.headManager.saveState(this.activeCategories, this.searchComponent.getText().toLowerCase(), this.page, this.useNbt);
        }));

        FlowLayout pageNavContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        pageNavContainer.child(prevPageButton);
        pageNavContainer.child(setPageComponent);
        pageNavContainer.child(nextPageButton);
        pageNavContainer.child(useNbtCheckbox);

        rootComponent.child(searchComponents);
        rootComponent.child(filtersCollapsible);
        rootComponent.child(skullScrollContainer);
        rootComponent.child(pageNavContainer);

        updateSkulls(this.activeCategories, this.searchComponent.getText());
    }

    private void updateSkulls(List<String> categories, String search) {
        this.headDataList.clear();

        for (String category : MinecraftHeadsBrowser.headManager.categorizedHeads.keySet()) {
            if (!categories.contains(category)) continue;
            this.headDataList.addAll(MinecraftHeadsBrowser.headManager.categorizedHeads.get(category).stream().filter(headData -> headData.name.toLowerCase().contains(search)).toList());
        }

        updateSkullsContainer();
    }

    private void updateSkullsContainer() {
        int startIndex = this.page * skullsPerPage;
        int endIndex = startIndex + skullsPerPage;
        this.skullContainer.clearChildren();

        for (int i = startIndex; i < this.headDataList.size() && i < endIndex; i++) {
            HeadData skullData = this.headDataList.get(i);
            ItemStack itemStack = MinecraftHeadsBrowser.headManager.createSkullFromHeadData(skullData);
            ItemComponent skull = Components.item(itemStack).setTooltipFromStack(true);
            this.skullContainer.child(skull);
            skull.mouseDown().subscribe(((v, v1, i1) -> {
                if (useNbt) {
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);

                    if (player.isInCreativeMode()) {
                        if (heldItem.isOf(Items.PLAYER_HEAD)) {
                            ItemStack newStack = heldItem.copy();

                            GameProfile profile = new GameProfile(skullData.uuid != null ? UUID.fromString(skullData.uuid) : UUID.randomUUID(), "Head");

                            profile.getProperties().put("textures", new Property("textures", skullData.value));

                            ProfileComponent profileComponent = new ProfileComponent(profile);
                            newStack.set(DataComponentTypes.PROFILE, profileComponent);

                            if (skullData.name != null) {
                                newStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(skullData.name));
                            }

                            // Update the held item (creative only syncs this to server)
                            int selectedSlot = player.getInventory()
                                    //#if MC<12105
                                    //$$ .selectedSlot;
                                    //#else
                                    .getSelectedSlot();
                                    //#endif
                            player.getInventory().setStack(selectedSlot, newStack);

                            // sync to server
                            int creativeSlotIndex = selectedSlot + 36; // 0–8 hotbar is 36–44
                            CreativeInventoryActionC2SPacket packet = new CreativeInventoryActionC2SPacket(creativeSlotIndex, newStack);
                            MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
                        } else {
                            MinecraftClient.getInstance().player.sendMessage(Text.of("§4Please ensure you are holding a player head!"), false);
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 10, (float) 0.3);
                            this.close();
                        }
                    } else {
                        MinecraftClient.getInstance().player.sendMessage(Text.of("§4Please ensure you are in creative mode!"), false);
                        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 10, (float) 0.3);
                        this.close();
                    }

                } else {
                    String id = uuidToNBTIntArray(skullData.uuid);
                    String texture = skullData.value;
                    String headName = skullData.name;

                    String giveCommand =
                            //#if MC<12105
                            //$$ "/give @s minecraft:player_head[minecraft:custom_name='{\"text\":\"%s\",\"color\":\"gold\",\"underlined\":true,\"bold\":true,\"italic\":false}',minecraft:lore=['{\"text\":\"www.minecraft-heads.com\",\"color\":\"blue\",\"italic\":false}'],profile={id:%s,properties:[{name:\"textures\",value:\"%s\"}]}] 1".formatted(headName, id, texture);
                            //#else
                            "/give @s minecraft:player_head[minecraft:custom_name={\"text\":\"%s\",\"color\":\"gold\",\"underlined\":true,\"bold\":true,\"italic\":false},minecraft:lore=[{\"text\":\"www.minecraft-heads.com\",\"color\":\"blue\",\"italic\":false}],profile={id:%s,properties:[{name:\"textures\",value:\"%s\"}]}] 1".formatted(headName, id, texture);
                            //#endif
                    MinecraftClient.getInstance().player.networkHandler.sendChatCommand(giveCommand.substring(1));
                }
                return true;
            }));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) && (this.setPageComponent.isFocused())) {
            this.updatePage(this.setPageComponent.getText());
            this.mouseClicked(this.searchComponent.positioning().get().x, this.searchComponent.positioning().get().y, 0);
            return true;
        }

        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) && this.searchComponent.isFocused()) {
            this.mouseClicked(this.searchComponent.positioning().get().x, this.searchComponent.positioning().get().y, 0);
            // jank ass way to fix needing to click the gui somewhere else to be able to re-select the textbox after removing focus from textbox
            return true;
        }

        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            this.nextPage();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            this.prevPage();
            return true;
        }

        return false;
    }

    private void nextPage() {
        int totalPages = (int) Math.ceil((double) this.headDataList.size() / (double) skullsPerPage);

        if (this.page + 1 == totalPages) return;

        this.page++;

        this.setPageComponent.setText(String.valueOf(this.page + 1));

        MinecraftHeadsBrowser.headManager.saveState(this.activeCategories, this.searchComponent.getText(), this.page, this.useNbt);
        this.updateSkullsContainer();
    }

    private void prevPage() {
        if (this.page == 0) return;
        this.page--;

        this.setPageComponent.setText(String.valueOf(this.page + 1));

        MinecraftHeadsBrowser.headManager.saveState(this.activeCategories, this.searchComponent.getText(), this.page, this.useNbt);
        this.updateSkullsContainer();
    }

    private void updatePage(String page) {
        try {
            int p = Integer.parseInt(page) - 1;
            int totalPages = (int) Math.ceil((double) this.headDataList.size() / (double) skullsPerPage);

            if (p < 0) {
                this.setPageComponent.setText("1");
                p = 0;
            }

            if (p > totalPages) {
                this.setPageComponent.setText(String.valueOf(totalPages));
                p = totalPages - 1;
            }

            this.page = p;

            MinecraftHeadsBrowser.headManager.saveState(this.activeCategories, this.searchComponent.getText(), this.page, this.useNbt);
            this.updateSkullsContainer();

        } catch (Exception e) {
            this.setPageComponent.setText("1");
        }
    }
}

// TODO: improve search
// TODO: fix the stupid checkbox at the bottom
// TODO: fix it going off screen >W<
// TODO: Local copy of heads data in case server is down
// TODO: 1.21.0 - 1.21.4 support
// TODO: implement proxy