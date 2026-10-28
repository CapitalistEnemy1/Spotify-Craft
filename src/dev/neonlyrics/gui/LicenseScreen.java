/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package dev.neonlyrics.gui;

import dev.neonlyrics.License;
import dev.neonlyrics.NeonLyrics;
import dev.neonlyrics.config.Config;
import dev.neonlyrics.config.Storage;
import dev.neonlyrics.hud.Gfx;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.time.LocalDateTime;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class LicenseScreen
extends Screen {
    private static final int ACCENT = 10321407;
    private EditBox box;
    private String status = "";
    private int statusCol = 0xFFFFFF;
    private long okTime = -1L;
    private int bx;
    private int by;
    private int bw;
    private int bh;
    private int px;
    private int py;
    private int pw;
    private int ph;

    public LicenseScreen() {
        super((Component)Component.literal((String)"Lyrics Cord"));
    }

    private static String tr(String key) {
        return Component.translatable((String)("neonlyrics.lic." + key)).getString();
    }

    protected void init() {
        int w = Math.min(320, this.width - 40);
        this.box = new EditBox(this.font, this.width / 2 - w / 2 + 12, this.height / 2 + 4, w - 24, 16, (Component)Component.literal((String)""));
        this.box.setMaxLength(200);
        this.box.setHint((Component)Component.literal((String)"LCORD-XXXX-XXXX-XXXX"));
        String old = NeonLyrics.cfg().licenseCode;
        this.box.setValue(old.startsWith("R1:") ? "" : old);
        this.addRenderableWidget((GuiEventListener)this.box);
        this.setInitialFocus((GuiEventListener)this.box);
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int w = Math.min(320, this.width - 40);
        int h = 150;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 70;
        Gfx.aaRound(g, x, y, w, h, 12.0f, 1380895, 0.96f);
        Gfx.aaRound(g, x, y, w, 2.0f, 1.0f, 10321407, 0.9f);
        Gfx.center(g, LicenseScreen.tr("title"), this.width / 2, y + 14, 0xFFFFFF, 1.0f, true);
        Gfx.center(g, LicenseScreen.tr("account") + " " + License.name(), this.width / 2, y + 32, 13219327, 1.0f, false);
        Gfx.center(g, LicenseScreen.tr("hint"), this.width / 2, y + 48, 0xAAAAB5, 1.0f, false);
        Gfx.aaRound(g, x + 10, this.height / 2, w - 20, 24.0f, 6.0f, 0, 0.5f);
        super.extractRenderState(g, mouseX, mouseY, delta);
        this.ph = 18;
        this.pw = (w - 30) / 2;
        this.px = x + 10;
        this.py = y + h - 34;
        this.bx = this.px + this.pw + 10;
        this.by = this.py;
        this.bw = this.pw;
        this.bh = this.ph;
        boolean ovP = mouseX >= this.px && mouseY >= this.py && mouseX < this.px + this.pw && mouseY < this.py + this.ph;
        boolean ovB = mouseX >= this.bx && mouseY >= this.by && mouseX < this.bx + this.bw && mouseY < this.by + this.bh;
        Gfx.aaRound(g, this.px, this.py, this.pw, this.ph, 9.0f, 0xFFFFFF, ovP ? 0.18f : 0.09f);
        Gfx.center(g, LicenseScreen.tr("paste"), this.px + this.pw / 2, this.py + 5, 0xFFFFFF, 1.0f, false);
        Gfx.aaRound(g, this.bx, this.by, this.bw, this.bh, 9.0f, 10321407, ovB ? 1.0f : 0.8f);
        Gfx.center(g, LicenseScreen.tr("unlock"), this.bx + this.bw / 2, this.by + 5, 0xFFFFFF, 1.0f, true);
        if (!this.status.isEmpty()) {
            Gfx.center(g, this.status, this.width / 2, y + h + 8, this.statusCol, 1.0f, true);
        }
        if (this.okTime > 0L && System.currentTimeMillis() - this.okTime > 900L) {
            this.onClose();
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x();
            double my = event.y();
            if (mx >= (double)this.px && my >= (double)this.py && mx < (double)(this.px + this.pw) && my < (double)(this.py + this.ph)) {
                this.box.setValue(this.minecraft.keyboardHandler.getClipboard().trim());
                return true;
            }
            if (mx >= (double)this.bx && my >= (double)this.by && mx < (double)(this.bx + this.bw) && my < (double)(this.by + this.bh)) {
                this.tryUnlock();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void unlocked() {
        NeonLyrics.saveConfig();
        this.status = LicenseScreen.tr("ok");
        this.statusCol = 8257434;
        this.okTime = System.currentTimeMillis();
    }

    private void tryUnlock() {
        String code = this.box.getValue().trim();
        String u = License.uuid();
        Config cf = NeonLyrics.cfg();
        if (License.isRedeemCode(code)) {
            String n = License.normalize(code);
            String owner = cf.usedCodes.get(n);
            if (owner != null && !owner.equals(u)) {
                this.status = LicenseScreen.tr("used");
                this.statusCol = 0xFF6A6A;
                return;
            }
            cf.usedCodes.put(n, u);
            cf.licenseCode = "R1:" + n + ":" + u;
            this.unlocked();
        } else if (License.check(u, code)) {
            cf.licenseCode = code;
            this.unlocked();
        } else {
            String n = License.normalize(code);
            this.status = LicenseScreen.tr("bad") + (String)(n.isEmpty() ? "" : "  (" + (String)(n.length() > 22 ? n.substring(0, 22) + "..." : n) + ")");
            this.statusCol = 0xFF6A6A;
        }
        try {
            Files.writeString(Storage.dir().resolve("license-status.txt"), (CharSequence)(String.valueOf(LocalDateTime.now()) + "\nAccount: " + License.name() + " " + u + "\nEingabe-Laenge: " + code.length() + "\nGelesen: " + License.normalize(code) + "\nErgebnis: " + this.status + "\n"), new OpenOption[0]);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

