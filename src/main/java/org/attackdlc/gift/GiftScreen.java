package org.attackdlc.gift;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.TextAlign;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Минималистичная рулетка подарков.
 *
 * Шансы: 0д 60% / 1д 15% / 3д 14% / 6д 1%
 * Кулдаун: 7 дней на HWID (хранится в Firebase).
 *
 * Каждый сегмент рисуется как набор узких повёрнутых прямоугольников
 * (клинья через pose.pushMatrix/rotate), без прямых GL-вызовов.
 */
public final class GiftScreen {

    public enum State { IDLE, OPENING, SPINNING, RESULT, CLOSED }

    private static final int[] PRIZE_DAYS    = {0, 1, 3, 6};
    private static final String[] PRIZE_TEXT = {"0", "1", "3", "6"};
    private static final int[] SEG_COLORS    = {
            0xFF3A3F4B, // 0 — тёмно-серый
            0xFF2563EB, // 1 — синий
            0xFF7C3AED, // 3 — фиолет
            0xFFF59E0B, // 6 — золото
    };
    private static final int   NUM_SEG     = 4;
    private static final float DEG_PER_SEG = 360f / NUM_SEG;
    private static final float SPIN_MS     = 4800f;
    private static final int   SLICES      = 14;      // подпрямоугольников на сегмент
    private static final float SEG_GAP     = 1.6f;    // зазор между сегментами, градусы

    private State   state         = State.IDLE;
    private float   progress      = 0f;
    private float   wheelAngle    = 0f;
    private float   targetAngle   = 0f;
    private long    spinStartMs   = 0L;
    private int     wonDays       = 0;
    private boolean showedResult = false;

    // ─── update ─────────────────────────────────────────────────────

    public void update(float dt) {
        if (state == null) state = State.IDLE;
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
            default -> {}
        }
    }

    // ─── render ─────────────────────────────────────────────────────

    public void render(GuiGraphicsExtractor ctx, int w, int h, float mx, float my) {
        if (state == State.IDLE) return;
        float a  = state == State.CLOSED ? progress : 1f;
        float cx = w / 2f, cy = h / 2f - 8f;
        float R  = Math.min(w, h) * 0.27f;

        // Затемнение фона
        Render2DUtil.rect(0, 0, w, h)
                .color(ColorUtil.rgba(2, 2, 5, (int) (a * 200))).draw();

        // Заголовок
        String title = switch (state) {
            case SPINNING -> "Крутим";
            case RESULT   -> "Готово";
            default       -> "Подарок";
        };
        Render2DUtil.text(cx, cy - R - 54f, 20f, title)
                .align(TextAlign.CENTER)
                .font(UiFonts.sfPro(700)).style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(0xFFFFFFFF, a)).draw();

        // Подзаголовок
        String hint = switch (state) {
            case SPINNING -> "0 · 1 · 3 · 6 дней подписки";
            case RESULT   -> wonDays == 0 ? "Попробуй через неделю" : "Всё зачислится автоматически";
            default       -> "раз в неделю  ·  привязка к ПК";
        };
        Render2DUtil.text(cx, cy - R - 30f, 9f, hint)
                .align(TextAlign.CENTER)
                .font(UiFonts.sfPro(500)).style(UiFontStyle.REGULAR)
                .color(ColorUtil.multiplyAlpha(0xFF52526B, a)).draw();

        // Колесо
        drawWheel(ctx, cx, cy, R, a);

        // Стрелка-указатель сверху
        drawArrow(ctx, cx, cy - R - 5f, a);

        // Карточка результата
        if (state == State.RESULT && progress > 0.15f)
            drawResult(cx, cy + R + 40f, a);

        // Кнопка закрытия
        if (state != State.SPINNING) {
            float bx = cx + R + 20f, by = cy - 14f;
            boolean hover = mx >= bx && mx <= bx + 28f && my >= by && my <= by + 28f;
            Render2DUtil.rect(bx, by, 28f, 28f)
                    .color(ColorUtil.multiplyAlpha(hover ? 0xFFEF4444 : 0xFF1C1D26, a))
                    .radius(7f)
                    .border(1f, ColorUtil.multiplyAlpha(hover ? 0xFFEF4444 : 0xFF2E2F3A, a))
                    .draw();
            Render2DUtil.text(bx + 14f, by + 5f, 16f, "\u2715")
                    .align(TextAlign.CENTER)
                    .font(UiFonts.sfPro(600))
                    .color(ColorUtil.multiplyAlpha(hover ? 0xFFFFFFFF : 0xFF9CA3AF, a)).draw();
            if (hover) close();
        }
    }

    // ─── колесо: клинья из узких повёрнутых прямоугольников ─────────

    private void drawWheel(GuiGraphicsExtractor ctx, float cx, float cy, float R, float a) {
        Matrix3x2fStack pose = ctx.pose();

        // Сегменты
        float sliceDeg = (DEG_PER_SEG - SEG_GAP) / SLICES;
        float w = 2f * R * (float) Math.tan(Math.toRadians(sliceDeg / 2.0)) * 1.05f;

        for (int i = 0; i < NUM_SEG; i++) {
            int segColor = ColorUtil.multiplyAlpha(SEG_COLORS[i], a);
            float segStart = wheelAngle + i * DEG_PER_SEG + SEG_GAP / 2f;

            for (int j = 0; j < SLICES; j++) {
                float phi = segStart + (j + 0.5f) * sliceDeg; // центр дольки, 0° = верх, по часовой
                pose.pushMatrix();
                pose.translate(cx, cy);
                pose.rotate((float) Math.toRadians(phi - 90f)); // +X направляем вдоль phi
                Render2DUtil.rect(0f, -w / 2f, R, w).color(segColor).draw();
                pose.popMatrix();
            }

            // Метка — посередине сегмента, ближе к краю
            float midDeg = segStart + (DEG_PER_SEG - SEG_GAP) / 2f;
            float labelR = R * 0.62f;
            float lx = cx + (float) Math.sin(Math.toRadians(midDeg)) * labelR;
            float ly = cy - (float) Math.cos(Math.toRadians(midDeg)) * labelR;
            Render2DUtil.text(lx, ly - 7f, 13f, PRIZE_TEXT[i])
                    .align(TextAlign.CENTER)
                    .font(UiFonts.sfPro(700)).style(UiFontStyle.MEDIUM)
                    .color(ColorUtil.multiplyAlpha(0xFFF3F4F6, a * 0.95f)).draw();
            Render2DUtil.text(lx, ly + 7f, 7f, "дн")
                    .align(TextAlign.CENTER)
                    .font(UiFonts.sfPro(500))
                    .color(ColorUtil.multiplyAlpha(0xFFD1D5DB, a * 0.55f)).draw();
        }

        // Ободок — маскирует края клиньев
        Render2DUtil.rect(cx - R - 3f, cy - R - 3f, (R + 3f) * 2f, (R + 3f) * 2f)
                .color(0).radius(R + 3f)
                .border(3f, ColorUtil.multiplyAlpha(0xFF13141C, a)).draw();
        Render2DUtil.rect(cx - R, cy - R, R * 2f, R * 2f)
                .color(0).radius(R)
                .border(1f, ColorUtil.multiplyAlpha(0xFF34364A, a)).draw();

        // Центральный хаб
        int cr = Math.round(R * 0.17f);
        Render2DUtil.rect(cx - cr, cy - cr, cr * 2f, cr * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF0A0B10, a)).radius(cr)
                .border(2f, ColorUtil.multiplyAlpha(0xFF34364A, a)).draw();
        Render2DUtil.rect(cx - cr + 3f, cy - cr + 3f, (cr - 3f) * 2f, (cr - 3f) * 2f)
                .color(ColorUtil.multiplyAlpha(0xFF161821, a)).radius(cr - 3f).draw();
    }

    // ─── указатель (треугольник вниз, над колесом) ───────────────────

    private void drawArrow(GuiGraphicsExtractor ctx, float x, float y, float a) {
        // Три узких прямоугольника, имитирующих треугольник
        float hgt = 9f, baseW = 16f;
        int col = ColorUtil.multiplyAlpha(0xFFFBBF24, a);
        int steps = 6;
        for (int s = 0; s < steps; s++) {
            float t0 = s / (float) steps, t1 = (s + 1) / (float) steps;
            float segW = baseW * (1f - (t0 + t1) / 2f);
            float segY = y + hgt * t0;
            float segH = hgt / steps + 0.5f;
            Render2DUtil.rect(x - segW / 2f, segY, segW, segH).color(col).draw();
        }
        // Стебель
        Render2DUtil.rect(x - 2f, y - 6f, 4f, 7f).color(col).radius(2f).draw();
    }

    // ─── карточка результата ─────────────────────────────────────────

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
        float t  = Math.max(0f, (a - 0.15f) / 0.85f);
        float pw = 240f, ph = 46f;
        Render2DUtil.rect(cx - pw / 2f, y, pw, ph)
                .color(ColorUtil.multiplyAlpha(0xFF111219, t))
                .radius(10f)
                .border(1.5f, ColorUtil.multiplyAlpha(color, t * 0.45f))
                .draw();
        Render2DUtil.text(cx, y + 14f, 13f, text)
                .align(TextAlign.CENTER)
                .font(UiFonts.sfPro(600)).style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(color, t)).draw();
    }

    // ─── клик / открытие / закрытие ──────────────────────────────────

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

    // ─── логика спина ────────────────────────────────────────────────

    private void initSpin() {
        String hwid = org.attackdlc.license.Hwid.get();
        String uid  = "";
        try { uid = (String) Class.forName("ru.xd.Session").getMethod("getUid").invoke(null); }
        catch (Throwable ignored) {}

        if (!GiftSpinManager.canSpin(hwid)) { state = State.CLOSED; progress = 1f; return; }

        GiftSpinManager.SpinResult r = GiftSpinManager.spin(hwid, uid);
        wonDays = r.ok() ? r.days() : 0;

        int prizeIdx = 0;
        for (int i = 0; i < PRIZE_DAYS.length; i++)
            if (PRIZE_DAYS[i] == wonDays) { prizeIdx = i; break; }

        // При wheelAngle=0 сегмент 0 наверху. Нужно под стрелку поставить prizeIdx:
        // колесо поворачиваем так, чтобы сегмент prizeIdx оказался в 0° (верх).
        float jitter = (float) (Math.random() - 0.5) * 14f;
        targetAngle = -(prizeIdx * DEG_PER_SEG) + 360f * 5f + jitter;
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
