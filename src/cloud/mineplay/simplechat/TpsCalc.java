package cloud.mineplay.simplechat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TpsCalc {

	private final JavaPlugin plugin;
	private long lastTime;
	private double tps = 20.0;
	private static final int INTERVAL_TICKS = 100;
	
	public TpsCalc(JavaPlugin plugin) {
        this.plugin = plugin;
        startMonitoring();
    }

    private void startMonitoring() {
        lastTime = System.nanoTime();

        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.nanoTime();
                long elapsed = now - lastTime;
                lastTime = now;

                double seconds = elapsed / 1.0E9;
                double currentTps = INTERVAL_TICKS / seconds;

                // Clamp the value to 20 max TPS
                tps = Math.min(20.0, currentTps);
            }
        }.runTaskTimer(plugin, INTERVAL_TICKS, INTERVAL_TICKS);
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public double getTps() {
        return round(tps, 2);
    }

}
