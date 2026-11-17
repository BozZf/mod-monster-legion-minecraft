package io.github.bozzf.monsterlegion.combat;

import java.util.ArrayList;
import java.util.List;

/** Each successful hit contributes its own fixed healing budget over ten active seconds. */
public final class HealingBudget {
    public record Window(float remaining, int ticks) { }
    private final List<Window> windows = new ArrayList<>();
    public void add(float damage) { restore(damage, LichRules.EFFECT_TICKS); }
    public void restore(float remaining, int ticks) {
        if (Float.isFinite(remaining) && remaining > 0 && ticks > 0)
            windows.add(new Window(remaining, Math.min(ticks, LichRules.EFFECT_TICKS)));
    }
    public float tick() {
        float total = 0;
        for (var iterator = windows.listIterator(); iterator.hasNext();) {
            Window window = iterator.next();
            float slice = window.remaining() / window.ticks();
            total += slice;
            if (window.ticks() == 1) iterator.remove();
            else iterator.set(new Window(window.remaining() - slice, window.ticks() - 1));
        }
        return total;
    }
    public List<Window> snapshot() { return List.copyOf(windows); }
    public void clear() { windows.clear(); }
}
