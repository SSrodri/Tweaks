package org.tweak.tweaks.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.util.Util;
import org.tweak.tweaks.client.config.TweaksConfig;

/**
 * Measures the local player's real round-trip time with the same query-ping
 * packets the F3 ping chart uses (4 pings per second), instead of relying on
 * the tab-list latency value that servers only refresh every few seconds.
 */
public final class PingMeter {
    private static final int PING_INTERVAL_TICKS = 5;
    private static final long STALE_AFTER_MS = 10_000L;

    private static volatile long lastRoundTrip = -1L;
    private static volatile long lastPongAt;
    private static int ticks;

    private PingMeter() {
    }

    /** Called every client tick; sends a ping while the numeric ping display is active. */
    public static void tick(MinecraftClient client) {
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        if (handler == null || !cfg.enabled || !cfg.numericPing) {
            lastRoundTrip = -1L;
            ticks = 0;
            return;
        }
        if (++ticks >= PING_INTERVAL_TICKS) {
            ticks = 0;
            handler.sendPacket(new QueryPingC2SPacket(Util.getMeasuringTimeMs()));
        }
    }

    /** Called from the network thread when the server answers a ping. */
    public static void onPong(long startTime) {
        long now = Util.getMeasuringTimeMs();
        lastRoundTrip = Math.max(0L, now - startTime);
        lastPongAt = now;
    }

    /** The measured round trip in ms, or -1 if there is no fresh measurement. */
    public static long getRoundTrip() {
        return Util.getMeasuringTimeMs() - lastPongAt > STALE_AFTER_MS ? -1L : lastRoundTrip;
    }
}
