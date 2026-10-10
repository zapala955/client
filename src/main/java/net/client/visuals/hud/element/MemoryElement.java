package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.StatElement;
import net.client.visuals.module.setting.ModeSetting;

/** Java heap in use. */
public final class MemoryElement extends StatElement {
	public final ModeSetting display = add(new ModeSetting("display", "liquidclient.hud.memory.display", new String[]{"usage", "percent"}, 0));
	private long nextUpdate;
	private int shownKey = -1;

	public MemoryElement() {
		super("hud_memory", false, 0.995f, 0.30f, 0xE445, "RAM", true);
	}

	@Override
	protected void refresh(HudContext ctx) {
		long now = System.currentTimeMillis();
		if (now < nextUpdate && shownKey >= 0) {
			return;
		}
		nextUpdate = now + 500L;
		Runtime runtime = Runtime.getRuntime();
		long used = runtime.totalMemory() - runtime.freeMemory();
		long max = runtime.maxMemory();
		int percent = (int) Math.round(used * 100.0 / Math.max(1L, max));
		int usedTenths = (int) Math.round(used / 107374182.4); // tenths of a GiB
		int maxTenths = (int) Math.round(max / 107374182.4);
		int key = display.index() == 1 ? percent | 1 << 30 : usedTenths << 15 | maxTenths;
		if (key != shownKey) {
			shownKey = key;
			if (display.index() == 1) {
				setValue(Integer.toString(percent));
				unit.setText("%");
			} else {
				setValue(usedTenths / 10 + "." + usedTenths % 10 + " / " + maxTenths / 10 + "." + maxTenths % 10);
				unit.setText("GB");
			}
		}
		valueColor = percent >= 90 ? HudContext.BAD : percent >= 75 ? HudContext.WARN : 0;
	}
}
