/*
 * Copyright 2021 DeathsGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.deathsgun.modmanager.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.util.DrawingUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import xyz.deathsgun.modmanager.ModManager;
import xyz.deathsgun.modmanager.api.mod.DetailedMod;
import xyz.deathsgun.modmanager.api.mod.SummarizedMod;
import xyz.deathsgun.modmanager.api.provider.IModProvider;

import java.util.Objects;
import java.util.Optional;

import static xyz.deathsgun.modmanager.gui.widget.ModListEntry.LOADING_ICON;
import static xyz.deathsgun.modmanager.gui.widget.ModListEntry.UNKNOWN_ICON;

public class ModDetailScreen extends Screen {

    private final Screen parentScreen;
    private final SummarizedMod summarizedMod;
    private DetailedMod detailedMod;

    public ModDetailScreen(Screen parentScreen, SummarizedMod mod) {
        super(new LiteralText(mod.name()));
        this.parentScreen = parentScreen;
        this.summarizedMod = mod;
    }

    @Override
    protected void init() {
        super.init();
        Optional<IModProvider> opt = ModManager.getModProvider();
        if (opt.isEmpty()) {
            return;
        }
        try {
            detailedMod = opt.get().getMod(summarizedMod.id());
        } catch (Exception e) {
            e.printStackTrace();
            Objects.requireNonNull(this.client).openScreen(new ModManagerErrorScreen(this, e));
        }
        //TODO: Add install/update button
        //TODO: If only remove available show only that button
        //TODO: Add description widget
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        int iconSize = 64;
        this.bindIconTexture();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindIconTexture();
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrices, 10, 10, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.disableBlend();

        final TextRenderer font = Objects.requireNonNull(client).textRenderer;

        MutableText trimmedTitle = new LiteralText(font.trimToWidth(detailedMod.name(), this.width - 200));
        trimmedTitle = trimmedTitle.setStyle(Style.EMPTY.withBold(true));

        int detailsY = iconSize / 2 - 10;
        int textX = 10 + iconSize + 5;

        font.draw(matrices, trimmedTitle, textX, detailsY, 0xFFFFFF);

        font.draw(matrices, new TranslatableText("modmanager.details.author", summarizedMod.author()), textX, detailsY += font.fontHeight, 0xFFFFFF);

        DrawingUtil.drawBadge(matrices, textX, detailsY + 12, font.getWidth(detailedMod.license()) + 6, Text.of(detailedMod.license()).asOrderedText(), 0xff6f6c6a, 0xff31302f, 0xCACACA);

        //TODO: Render description
        //TODO: Render categories
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void bindIconTexture() {
        if (ModManager.getIconDownloader().isErrored(summarizedMod.id())) {
            RenderSystem.setShaderTexture(0, UNKNOWN_ICON);
            return;
        }
        Identifier icon = ModManager.getIconDownloader().getIcon(summarizedMod.id());
        if (icon == null) {
            if (ModManager.getIconDownloader().isLoading(summarizedMod.id())) {
                icon = LOADING_ICON;
            } else {
                ModManager.getIconDownloader().addMod(summarizedMod);
                return;
            }
        }
        RenderSystem.setShaderTexture(0, icon);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.client).openScreen(parentScreen);
    }
}
