package org.attackdlc.gift;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Минималистичная рулетка подарков.
 *
 * Шансы: 0д 60% / 1д 15% / 3д 14% / 6д 1%
 * Кулдаун: 7 дней на HWID (хранится в Firebase).
 */
public final class GiftScreen {

    public enum State { IDLE, OPENING, SPINNING, RESULT, CLOSED }

    // Сегменты по часовой стрелке (индекс = порядковый номер при 0° = верх)
    private static final int[] PRIZE_DAYS    = {0, 1, 3, 6};
    private static final String[] PRIZE_TEXT = {"0", "1", "3", "6"};
    private static final int[] SEG_COLORS    = {
            0xFF3F3F46, // 0 — тёмно-серый
            0xFF2563EB, // 1 — синий
            0xFF7C3AED, // 3 — фиолет
            0xFFF59E0B, // 6 — золото
    };
    private static final int  NUM_SEG       = 4;
    private static final float DEG_PER_SEG  = 360f / NUM_SEG;
    private static final float SPIN_MS      = 4800f;

    private State  state;
    private float  progress;
    private float  wheelAngle;
    private float  targetAngle;
    private long   spinStartMs;
    private int    wonDays;
    private boolean showedResult;

    public GiftScreen() {}

    // ─── update ─────────────────────────────────────────────────────

    public void update(float dt) {
        switch (state) {
            case OPENING -> {
                progress = Math.min(1f, progress + dt * 3f);
                if (progress >= 1f) { state = State.SPINNING; startSpin(); }
            }
            case SPINNING -> {
                float t = Math.min(1f, (System.currentTimeMillis() - spinStartMs) / SPIN_MS);
                wheelAngle = easeOut(t) * targetAngle;
                if (t >= 1f) { state = State.RESULT; progress = 0f; showedResult = false; }
            }
            case RESULT -> progress = Math.min(1f, progress + dt * 3f);
            case CLOSED -> {
                progress = Math.max(0f, progress - dt * 3f);
                if (progress <= 0f) state = State.IDLE;
            }
        }
    }

    // ─── render ─────────────────────────────────────────────────────

    public void render(GuiGraphicsExtractor ctx, int w, int h, float mx, float my) {
        if (state == State.IDLE) return;
        float a = state == State.CLOSED ? progress : 1f;
        float cx = w / 2f, cy = h / 2f - 8f;
        float R  = Math.min(w, h) * 0.27f;

        // Затемнение фона
        Render2DUtil.rect(0, 0, w, h)
                .color(ColorUtil.rgba(2, 2, 5, (int)(a * 200))).draw();

        // Заголовок
        String title = switch (state) {
            case SPINNING -> "Крутим";
            case RESULT   -> "Готово";
            default       -> "Подарок";
        };
        Render2DUtil.text(cx - 28f, cy - R - 48f, 18f, title)
                .font(UiFonts.sfPro(700)).style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, a)).draw();

        // Подзаголовок
        if (state == State.SPINNING) {
            Render2DUtil.text(cx - 52f, cy - R - 26f, 9f,
                    "0 · 1 · 3 · 6 дней подписки")
                    .font(UiFonts.sfPro(500)).style(UiFontStyle.REGULAR)
                    .color(ColorUtil.multiplyAlpha(0xFF52526B, a)).draw();
        } else if (state == State.RESULT) {
            Render2DUtil.text(cx - 40f, cy - R - 26f, 9f,
                    wonDays == 0 ? "Попробуй через неделю"
                               : "Всё зачислится автоматически")
                    .font(UiFonts.sfPro(500)).style(UiFontStyle.REGULAR)
                    .color(ColorUtil.multiplyAlpha(0xFF52526B, a)).draw();
        } else {
            Render2DUtil.text(cx - 52f, cy - R - 26f, 9f,
                    "раз в неделю  ·  привязка к ПК")
                    .font(UiFonts.sfPro(500)).style(UiFontStyle.REGULAR)
                    .color(ColorUtil.multiplyAlpha(0xFF52526B, a)).draw();
        }

        // Колесо
        drawWheel(ctx, cx, cy, R, a);

        // Стрелка-указатель сверху
        drawArrow(ctx, cx, cy - R - 2f, a);

        // Карточка результата
        if (state == State.RESULT && progress > 0.2f)
            drawResult(cx, cy + R + 36f, a);

        // Кнопка закрытия (только не во время спина)
        if (state != State.SPINNING) {
            float bx = cx + R + 16f, by = cy - 14f;
            boolean hover = mx >= bx && mx <= bx + 28f && my >= by && my <= by + 28f;
            Render2DUtil.rect(bx, by, 28f, 28f)
                    .color(ColorUtil.multiplyAlpha(hover ? 0xFFEF4444 : 0xFF6B7280, a))
                    .radius(6f).draw();
            Render2DUtil.text(bx + 6f, by + 2f, 16f, "\u2715")
                    .font(UiFonts.sfPro(700))
                    .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, a)).draw();
            if (hover) close();
        }
    }

    // ─── колесо ────────────────────────────────────────────────────

    private void drawWheel(GuiGraphicsExtractor ctx, float cx, float cy, float R, float a) {
        MsdfFont font = UiFonts.sfPro(600);

        // Тёмный круг-подложка
        Render2DUtil.rect(cx - R, cy - R, R * 2f, R * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF0A0B10, a)).radius(R).draw();

        // 4 сегмента-клина
        for (int i = 0; i < NUM_SEG; i++) {
            float angleDeg = wheelAngle + i * DEG_PER_SEG;
            drawWedge(ctx, cx, cy, R, angleDeg, DEG_PER_SEG * 0.9f,
                    SEG_COLORS[i], a);
            // Метка (цифра) внутри сегмента
            float labelR = R * 0.62f;
            double rad = Math.toRadians(angleDeg);
            float lx = (float)(cx + Math.cos(rad) * labelR);
            float ly = (float)(cy - Math.sin(rad) * labelR);
            Render2DUtil.text(lx - 10f, ly - 6f, 12f, PRIZE_TEXT[i])
                    .font(font).style(UiFontStyle.MEDIUM)
                    .color(ColorUtil.multiplyAlpha(0xFFE5E7EB, a * 0.85f)).draw();
        }

        // Ободок
        Render2DUtil.rect(cx - R - 2f, cy - R - 2f, (R + 2f) * 2f, (R + 2f) * 2f)
                .color(0).radius(R + 2f)
                .border(2f, ColorUtil.multiplyAlpha(0xFF27272E, a)).draw();

        // Центр
        int cr = Math.round(R * 0.17f);
        Render2DUtil.rect(cx - cr, cy - cr, cr * 2f, cr * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF0A0B10, a)).radius(cr).draw();
        Render2DUtil.rect(cx - cr + 2f, cy - cr + 2f, (cr - 2f) * 2f, (cr - 2f) * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF13141C, a)).radius(cr - 2f).draw();
        Render2DUtil.text(cx - 7f, cy - 7f, 14f, "\uD83C\uDF81")
                .font(UiFonts.sfPro(400))
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, a)).draw();
    }

    /** Сектор: прямоугольник от центра к краю, повёрнутый на angleDeg. */
    private void drawWedge(GuiGraphicsExtractor ctx, float cx, float cy, float R,
                            float angleDeg, float sweepDeg, int color, float a) {
        float halfW = R * (float) Math.tan(Math.toRadians(sweepDeg / 2f));
        float w = halfW * 2f, h = R * 0.95f;
        var pose = ctx.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.rotate((float) Math.toRadians(angleDeg));
        Render2DUtil.rect(-w / 2f, 0f, w, h)
                .color(ColorUtil.multiplyAlpha(color, a * 0.82f))
                .radius(3f).draw();
        pose.popMatrix();
    }

    // ─── указатель (треугольник вниз) ──────────────────────────────

    private void drawArrow(GuiGraphicsExtractor ctx, float x, float y, float a) {
        float s = 10f;
        Render2DUtil.rect(x - s / 2f, y, s, s)
                .color(ColorUtil.multiplyAlpha(0xFFEAB308, a)).radius(2f).draw();
        Render2DUtil.rect(x - 3f, y + s, 6f, 5f)
                .color(ColorUtil.multiplyAlpha(0xFFEAB308, a)).radius(1f).draw();
    }

    // ─── карточка результата ───────────────────────────────────────

    private void drawResult(float cx, float y, float a) {
        if (showedResult) return;
        showedResult = true;
        int days = wonDays;
        String text = days == 0
                ? "Не повезло — попробуй через неделю"
                : days + " " + dayWord(days) + " подписки";
        int color = days == 0 ? 0xFF52526B
                  : days == 1 ? 0xFF60A5FA
                  : days == 3 ? 0xFFA78BFA
                  : 0xFFFBBF24;
        float t = Math.max(0f, (a - 0.2f) / 0.8f);
        float pw = 220f, ph = 44f;
        Render2DUtil.rect(cx - pw / 2f, y, pw, ph)
                .color(ColorUtil.multiplyAlpha(0xFF111219, t))
                .radius(10f)
                .border(1.5f, ColorUtil.multiplyAlpha(color, t * 0.4f))
                .draw();
        Render2DUtil.text(cx - 80f, y + 11f, 14f, text)
                .font(UiFonts.sfPro(600)).style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(color, t)).draw();
    }

    // ─── клик / открытие / закрытие ───────────────────────────────

    public boolean handleClick(float mx, float my, float bx, float by, float bw, float bh) {
        if (state != State.IDLE && state != State.CLOSED) return true;
        if (mx >= bx && mx <= bx + bw && my >= by && my <= by + bh) { open(); return true; }
        return false;
    }

    public void open() {
        if (state == State.IDLE || state == State.CLOSED) {
            state = State.OPENING; progress = 0f; showedResult = false; initSpin();
        }
    }

    public void close() {
        if (state == State.RESULT) { state = State.CLOSED; progress = 1f; }
    }

    // ─── логика спина ──────────────────────────────────────────────

    private void initSpin() {
        String hwid = org.attackdlc.license.Hwid.get();
        String uid  = "";
        try { uid = (String) Class.forName("ru.xd.Session").getMethod("getUid").invoke(null); }
        catch (Throwable ignored) {}

        boolean can = GiftSpinManager.canSpin(hwid);
        if (!can) { state = State.CLOSED; progress = 1f; return; }

        GiftSpinManager.SpinResult r = GiftSpinManager.spin(hwid, uid);
        wonDays = r.ok() ? r.days() : 0;

        // Целевой угол: 5 полных оборотов + позиция выигрышного сегмента + джиттер
        int prizeIdx = 0;
        for (int i = 0; i < PRIZE_DAYS.length; i++)
            if (PRIZE_DAYS[i] == wonDays) { prizeIdx = i; break; }
        float jitter = (float)(Math.random() - 0.5) * 16f;
        targetAngle = prizeIdx * DEG_PER_SEG + 360f * 5f + jitter;
    }

    private void startSpin() { spinStartMs = System.currentTimeMillis(); }

    private static float easeOut(float t) {
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    private static String dayWord(int d) {
        int abs = Math.abs(d) % 10, ten = Math.abs(d) % 100;
        if (ten >= 11 && ten <= 19) return "дней";
        if (abs == 1) return "день";
        if (abs >= 2 && abs <= 4) return "дня";
        return "дней";
    }
}
