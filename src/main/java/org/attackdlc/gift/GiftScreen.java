package org.attackdlc.gift;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Рулетка подарков — модальный экран на титульном экране.
 */
public final class GiftScreen {

    public enum State { IDLE, OPENING, SPINNING, RESULT, CLOSED }

    private static final int[] SEGMENT_COLORS = {
            0xFF4A4A5A,  // серый -- 0 дней
            0xFF3B82F6,  // синий -- 1 день
            0xFF8B5CF6,  // фиолетовый -- 3 дня
            0xFFF59E0B,  // золотой -- 6 дней
    };
    private static final String[] SEGMENT_LABELS = {"0 дн", "1 дн", "3 дн", "6 дн"};

    private State state = State.IDLE;
    private float progress = 0f;
    private float wheelAngle = 0f;
    private long spinStartTime = 0L;
    private int wonDays = 0;
    private boolean showedResult;

    public void update(float dt) {
        switch (state) {
            case OPENING -> {
                progress = Math.min(1f, progress + dt * 2.5f);
                if (progress >= 1f) { state = State.SPINNING; startSpin(); }
            }
            case SPINNING -> {
                float elapsed = (System.currentTimeMillis() - spinStartTime) / 1000f;
                float speed = Math.max(0f, 10f - elapsed * 1.5f);
                wheelAngle += speed * dt;
                if (elapsed > 4.8f) { state = State.RESULT; progress = 0f; showedResult = false; }
            }
            case RESULT -> progress = Math.min(1f, progress + dt * 3f);
            case CLOSED -> {
                progress = Math.max(0f, progress - dt * 3f);
                if (progress <= 0f) state = State.IDLE;
            }
        }
    }

    public void render(GuiGraphicsExtractor ctx, int w, int h, float mx, float my) {
        if (state == State.IDLE) return;
        float alpha = state == State.CLOSED ? progress : 1f;

        float cx = w / 2f;
        float cy = h / 2f - 20f;
        float radius = Math.min(w, h) * 0.26f;

        // Фон
        Render2DUtil.rect(0, 0, w, h)
                .color(ColorUtil.rgba(4, 4, 8, (int)(alpha * 170)))
                .draw();

        // Заголовок
        String title = state == State.SPINNING ? "Крутим..."
                : state == State.RESULT ? "Результат!" : "Подарок";
        Render2DUtil.text(cx - 40f, cy - radius - 52f, 20f, title)
                .font(UiFonts.sfPro(700))
                .style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha))
                .draw();

        Render2DUtil.text(cx - 55f, cy - radius - 26f, 9f,
                state == State.SPINNING ? "Шансы: 0 / 1 / 3 / 6 дней подписки"
                        : state == State.RESULT ? "Удачи в следующий раз!" : "Раз в неделю -- привязка к ПК")
                .font(UiFonts.sfPro(500))
                .color(ColorUtil.multiplyAlpha(0xFF787E88, alpha))
                .draw();

        // Колесо
        drawWheel(ctx, cx, cy, radius, alpha);

        // Указатель
        drawPointer(ctx, cx, cy - radius - 8f, alpha);

        // Результат
        if (state == State.RESULT && progress > 0.2f) {
            drawResult(cx, cy + radius + 42f, alpha);
        }

        // Кнопка закрытия
        if (state != State.SPINNING) {
            float bx = cx + radius + 14f;
            float by = cy - 16f;
            boolean hover = mx >= bx && mx <= bx + 32f && my >= by && my <= by + 32f;
            Render2DUtil.rect(bx, by, 32f, 32f)
                    .color(ColorUtil.multiplyAlpha(hover ? 0xFFEF4444 : 0xFF787E88, alpha))
                    .radius(8f)
                    .draw();
            Render2DUtil.text(bx + 8f, by + 4f, 16f, "\u2715")
                    .font(UiFonts.sfPro(700))
                    .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha))
                    .draw();
            if (hover) close();
        }
    }

    public boolean handleClick(float mx, float my, float bx, float by, float bw, float bh) {
        if (state != State.IDLE && state != State.CLOSED) return true;
        if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) {
            open();
            return true;
        }
        return false;
    }

    public void open() {
        if (state == State.IDLE || state == State.CLOSED) {
            state = State.OPENING;
            progress = 0f;
            showedResult = false;
            initSpin();
        }
    }

    public void close() {
        if (state == State.RESULT) {
            state = State.CLOSED;
            progress = 1f;
        }
    }

    // --- wheel ---

    private void drawWheel(GuiGraphicsExtractor ctx, float cx, float cy, float radius, float alpha) {
        int segs = SEGMENT_COLORS.length;
        float degPerSeg = 360f / segs;
        MsdfFont font = UiFonts.sfPro(600);

        for (int i = 0; i < segs; i++) {
            float angle = wheelAngle + i * degPerSeg;
            drawSector(ctx, cx, cy, radius, angle, degPerSeg, SEGMENT_COLORS[i], alpha);

            float midRad = (float) Math.toRadians(angle + degPerSeg / 2f);
            float labelR = radius * 0.6f;
            float lx = cx + (float) Math.cos(midRad) * labelR;
            float ly = cy + (float) Math.sin(midRad) * labelR;
            int lc = i == 0 ? 0xFF9CA3AF : 0xFFFFFFFF;
            Render2DUtil.text(lx - 14f, ly - 6f, 10f, SEGMENT_LABELS[i])
                    .font(font)
                    .style(UiFontStyle.MEDIUM)
                    .color(ColorUtil.multiplyAlpha(lc, alpha * 0.88f))
                    .draw();
        }

        // Обводка
        Render2DUtil.rect(cx - radius, cy - radius, radius * 2f, radius * 2f)
                .color(0)
                .radius(radius)
                .border(2f, ColorUtil.multiplyAlpha(0xFF3A3F4A, alpha))
                .draw();

        // Центр
        int cr = Math.round(radius * 0.16f);
        Render2DUtil.rect(cx - cr, cy - cr, cr * 2f, cr * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF0C0D10, alpha))
                .radius(cr)
                .draw();
        Render2DUtil.text(cx - 8f, cy - 8f, 14f, "\ud83c\udf81")
                .font(UiFonts.sfPro(400))
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha))
                .draw();
    }

    private void drawSector(GuiGraphicsExtractor ctx, float cx, float cy, float r,
                             float angleDeg, float sweepDeg, int color, float alpha) {
        var pose = ctx.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.rotate((float) Math.toRadians(angleDeg + sweepDeg / 2f));
        try {
            float halfSweep = sweepDeg / 2f;
            float chord = 2f * r * (float) Math.sin(Math.toRadians(halfSweep));
            float w = chord + 2f;
            float h = r * 0.95f;
            Render2DUtil.rect(-w / 2f, 0f, w, h)
                    .color(ColorUtil.multiplyAlpha(color, alpha * 0.85f))
                    .radius(h * 0.08f)
                    .draw();
        } finally {
            pose.popMatrix();
        }
    }

    private void drawPointer(GuiGraphicsExtractor ctx, float x, float y, float alpha) {
        var pose = ctx.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        try {
            float s = 12f;
            Render2DUtil.rect(-s / 2f, -s / 2f, s, s)
                    .color(ColorUtil.multiplyAlpha(0xFFFFD700, alpha))
                    .draw();
            Render2DUtil.rect(-s / 2f, s / 2f - 2f, s, 2f)
                    .color(ColorUtil.multiplyAlpha(0xFFFFD700, alpha))
                    .draw();
        } finally {
            pose.popMatrix();
        }
    }

    private void drawResult(float x, float y, float alpha) {
        if (showedResult) return;
        showedResult = true;
        int days = wonDays;
        String text = days == 0 ? "Не повезло..." : "Выпало " + days + " " + dayWord(days) + "!";
        int color = days == 0 ? 0xFF787E88 : days == 1 ? 0xFF3B82F6
                : days == 3 ? 0xFF8B5CF6 : 0xFFF59E0B;
        float t = Math.max(0f, (alpha - 0.2f) / 0.8f);
        Render2DUtil.text(x - 60f, y, 16f, text)
                .font(UiFonts.sfPro(700))
                .style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(color, t))
                .draw();
    }

    private static String dayWord(int days) {
        int abs = Math.abs(days) % 10;
        int ten = Math.abs(days) % 100;
        if (ten >= 11 && ten <= 19) return "дней";
        if (abs == 1) return "день";
        if (abs >= 2 && abs <= 4) return "дня";
        return "дней";
    }

    private void initSpin() {
        String hwid = org.attackdlc.license.Hwid.get();
        String uid = "";
        try {
            uid = (String) Class.forName("ru.xd.Session").getMethod("getUid").invoke(null);
        } catch (Throwable ignored) {}
        boolean can = GiftSpinManager.canSpin(hwid);
        if (!can) { state = State.CLOSED; progress = 1f; return; }
        GiftSpinManager.SpinResult result = GiftSpinManager.spin(hwid, uid);
        wonDays = result.ok() ? result.days() : 0;
    }

    private void startSpin() { spinStartTime = System.currentTimeMillis(); }
}
