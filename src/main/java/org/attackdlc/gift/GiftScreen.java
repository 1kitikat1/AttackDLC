package org.attackdlc.gift;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Рулетка подарков — модальный экран на титульном экране.
 *
 * <p>Шансы (заранее определены, не меняются при анимации):
 *   0 дней — 60%, 1 день — 15%, 3 дня — 14%, 6 дней — 1%.</p>
 *
 * <p>Результат фиксируется до начала вращения.
 * Кулдаун — 7 дней на HWID, проверяется через Firebase.</p>
 */
public final class GiftScreen {

    public enum State { IDLE, OPENING, SPINNING, RESULT, CLOSED }

    private static final int[] PRIZE_DAYS      = {0, 1, 3, 6};
    private static final String[] PRIZE_TEXT   = {"0 дн", "1 дн", "3 дн", "6 дн"};
    private static final int[] SEGMENT_COLORS  = {
            0xFF4A4A5A, // серый   — 0 дней
            0xFF2563EB, // синий   — 1 день
            0xFF7C3AED, // фиолет  — 3 дня
            0xFFF59E0B, // золотой — 6 дней
    };
    private static final int    SEGMENT_BORDER = 0xFF1C1C27;
    private static final int    NUM_SEGMENTS   = 4;
    private static final float  DEGREE_PER_SEG = 360f / NUM_SEGMENTS;

    private State        state         = State.IDLE;
    private float        progress      = 0f;
    private float        wheelAngle    = 0f;
    private float        targetAngle   = 0f;
    private long         spinStartMs   = 0L;
    private static final float SPIN_DURATION_MS = 5000f;

    private int wonDays   = 0;
    private boolean showedResult;

    // ─── update ────────────────────────────────────────────────────

    public void update(float dt) {
        switch (state) {
            case OPENING -> {
                progress = Math.min(1f, progress + dt * 2.8f);
                if (progress >= 1f) { state = State.SPINNING; startSpin(); }
            }
            case SPINNING -> {
                float t = Math.min(1f, (System.currentTimeMillis() - spinStartMs) / SPIN_DURATION_MS);
                wheelAngle = easeOutCubic(t) * targetAngle;
                if (t >= 1f) { state = State.RESULT; progress = 0f; showedResult = false; }
            }
            case RESULT -> progress = Math.min(1f, progress + dt * 3f);
            case CLOSED -> {
                progress = Math.max(0f, progress - dt * 3f);
                if (progress <= 0f) state = State.IDLE;
            }
        }
    }

    // ─── render ────────────────────────────────────────────────────

    public void render(GuiGraphicsExtractor ctx, int w, int h, float mx, float my) {
        if (state == State.IDLE) return;
        float alpha = state == State.CLOSED ? progress : 1f;

        float cx  = w / 2f;
        float cy  = h / 2f - 10f;
        float R   = Math.min(w, h) * 0.28f;

        // Фон
        Render2DUtil.rect(0, 0, w, h)
                .color(ColorUtil.rgba(3, 3, 7, (int)(alpha * 190)))
                .draw();

        // Заголовок
        String title = state == State.SPINNING ? "✨ Крутим..."
                : state == State.RESULT  ? "🎉 Результат!" : "🎁 Подарок";
        Render2DUtil.text(cx - 50f, cy - R - 58f, 22f, title)
                .font(UiFonts.sfPro(700))
                .style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha))
                .draw();

        String hint = state == State.SPINNING ? "Шансы: 0 / 1 / 3 / 6 дней подписки"
                    : state == State.RESULT  ? "Спасибо за игру!"
                    : "Раз в неделю  ·  привязка к ПК";
        Render2DUtil.text(cx - 60f, cy - R - 30f, 9f, hint)
                .font(UiFonts.sfPro(500))
                .style(UiFontStyle.REGULAR)
                .color(ColorUtil.multiplyAlpha(0xFF6B7280, alpha))
                .draw();

        // Колесо
        drawWheel(ctx, cx, cy, R, alpha);

        // Указатель
        drawPointer(ctx, cx, cy - R - 6f, alpha);

        // Карточка результата
        if (state == State.RESULT && progress > 0.15f) {
            drawResultCard(cx, cy + R + 44f, alpha);
        }

        // Кнопка закрытия
        if (state != State.SPINNING) {
            float bx = cx + R + 18f;
            float by = cy - 16f;
            boolean hover = mx >= bx && mx <= bx + 32f && my >= by && my <= by + 32f;
            Render2DUtil.rect(bx, by, 32f, 32f)
                    .color(ColorUtil.multiplyAlpha(hover ? 0xFFEF4444 : 0xFF787E88, alpha))
                    .radius(8f)
                    .draw();
            Render2DUtil.text(bx + 7f, by + 3f, 18f, "\u2715")
                    .font(UiFonts.sfPro(700))
                    .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha))
                    .draw();
            if (hover) close();
        }
    }

    // ─── колесо ────────────────────────────────────────────────────

    private void drawWheel(GuiGraphicsExtractor ctx, float cx, float cy, float R, float alpha) {
        MsdfFont labelFont = UiFonts.sfPro(600);

        // Тёмная подложка
        Render2DUtil.rect(cx - R, cy - R, R * 2f, R * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF0E0F14, alpha))
                .radius(R)
                .draw();

        // Сегменты
        for (int i = 0; i < NUM_SEGMENTS; i++) {
            float segAngleDeg = wheelAngle + i * DEGREE_PER_SEG;
            drawWedge(ctx, cx, cy, R, segAngleDeg, DEGREE_PER_SEG * 0.92f,
                    SEGMENT_COLORS[i], alpha);
            drawSegmentLabel(ctx, cx, cy, R * 0.6f, segAngleDeg,
                    PRIZE_TEXT[i], alpha, labelFont);
        }

        // Ободок
        Render2DUtil.rect(cx - R - 3f, cy - R - 3f, (R + 3f) * 2f, (R + 3f) * 2f)
                .color(0)
                .radius(R + 3f)
                .border(3f, ColorUtil.multiplyAlpha(SEGMENT_BORDER, alpha))
                .draw();

        // Центр
        int cr = Math.round(R * 0.18f);
        Render2DUtil.rect(cx - cr, cy - cr, cr * 2f, cr * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF0C0D12, alpha))
                .radius(cr)
                .draw();
        Render2DUtil.text(cx - 9f, cy - 9f, 16f, "\uD83C\uDF81")
                .font(UiFonts.sfPro(400))
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha))
                .draw();
    }

    /** Сектор-клин: прямоугольник от центра к краю, повёрнутый на angleDeg. */
    private void drawWedge(GuiGraphicsExtractor ctx, float cx, float cy, float R,
                            float angleDeg, float sweepDeg, int color, float alpha) {
        // Половина ширины клина на окружности ≈ R·tan(sweepDeg/2)
        float halfW = R * (float) Math.tan(Math.toRadians(sweepDeg / 2f));
        float rectW = halfW * 2f;
        float rectH = R * 0.96f;

        var pose = ctx.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.rotate((float) Math.toRadians(angleDeg));
        // Нижний край прямоугольника = центр колеса
        Render2DUtil.rect(-rectW / 2f, 0f, rectW, rectH)
                .color(ColorUtil.multiplyAlpha(color, alpha * 0.88f))
                .radius(4f)
                .draw();
        pose.popMatrix();
    }

    private void drawSegmentLabel(GuiGraphicsExtractor ctx, float cx, float cy, float r,
                                   float angleDeg, String text, float alpha, MsdfFont font) {
        double rad = Math.toRadians(angleDeg);
        float lx = (float)(cx + Math.cos(rad) * r);
        float ly = (float)(cy - Math.sin(rad) * r);
        Render2DUtil.text(lx - 16f, ly - 7f, 11f, text)
                .font(font)
                .style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, alpha * 0.92f))
                .draw();
    }

    private void drawPointer(GuiGraphicsExtractor ctx, float x, float y, float alpha) {
        float s = 14f;
        Render2DUtil.rect(x - s / 2f, y, s, s)
                .color(ColorUtil.multiplyAlpha(0xFFFFD166, alpha))
                .radius(3f)
                .draw();
        Render2DUtil.rect(x - 4f, y + s, 8f, 6f)
                .color(ColorUtil.multiplyAlpha(0xFFFFD166, alpha))
                .radius(2f)
                .draw();
    }

    private void drawResultCard(float cx, float y, float alpha) {
        if (showedResult) return;
        showedResult = true;
        int days = wonDays;
        String text = days == 0 ? "Не повезло в этот раз…"
                    : "Выпало " + days + " " + dayWord(days) + "!";
        int color = days == 0 ? 0xFF787E88
                  : days == 1 ? 0xFF60A5FA
                  : days == 3 ? 0xFFA78BFA
                  : 0xFFFBBF24;
        float t = Math.max(0f, (alpha - 0.15f) / 0.85f);

        float pw = 200f, ph = 52f;
        Render2DUtil.rect(cx - pw / 2f, y, pw, ph)
                .color(ColorUtil.multiplyAlpha(0xFF13141F, t))
                .radius(12f)
                .border(1.5f, ColorUtil.multiplyAlpha(color, t * 0.45f))
                .draw();
        Render2DUtil.text(cx - 50f, y + 14f, 15f, text)
                .font(UiFonts.sfPro(700))
                .style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(color, t))
                .draw();
    }

    // ─── клик / открытие / закрытие ───────────────────────────────

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

    // ─── логика спина ──────────────────────────────────────────────

    /** Определяет результат ДО анимации и рассчитывает целевой угол. */
    private void initSpin() {
        String hwid = org.attackdlc.license.Hwid.get();
        String uid  = "";
        try {
            uid = (String) Class.forName("ru.xd.Session")
                    .getMethod("getUid").invoke(null);
        } catch (Throwable ignored) {}

        boolean can = GiftSpinManager.canSpin(hwid);
        if (!can) { state = State.CLOSED; progress = 1f; return; }

        GiftSpinManager.SpinResult result = GiftSpinManager.spin(hwid, uid);
        wonDays = result.ok() ? result.days() : 0;

        // Индекс сегмента, который соответствует wonDays
        int prizeIdx = 0;
        for (int i = 0; i < PRIZE_DAYS.length; i++) {
            if (PRIZE_DAYS[i] == wonDays) { prizeIdx = i; break; }
        }
        // Целевой угол: 5 полных оборотов + позиция prizeIdx + случайный сдвиг внутри сегмента
        float jitter = (float) (Math.random() - 0.5) * 18f;
        targetAngle = prizeIdx * DEGREE_PER_SEG + 360f * 5f + jitter;
    }

    private void startSpin() {
        spinStartMs = System.currentTimeMillis();
    }

    private static float easeOutCubic(float t) {
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    private static String dayWord(int days) {
        int abs = Math.abs(days) % 10;
        int ten = Math.abs(days) % 100;
        if (ten >= 11 && ten <= 19) return "дней";
        if (abs == 1) return "день";
        if (abs >= 2 && abs <= 4) return "дня";
        return "дней";
    }
}
