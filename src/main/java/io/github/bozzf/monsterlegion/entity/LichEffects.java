package io.github.bozzf.monsterlegion.entity;

/** Client installs the handlers. Dedicated servers never load rendering classes. */
public final class LichEffects {
    public interface Handler {
        void particle(LichKingEntity lich, String effect);
        void sound(LichKingEntity lich, String effect);
    }
    public static Handler handler = new Handler() {
        public void particle(LichKingEntity lich, String effect) { }
        public void sound(LichKingEntity lich, String effect) { }
    };
    private LichEffects() { }
}
