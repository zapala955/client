package net.client.visuals.hud.element;

import net.client.visuals.hud.HudContext;
import net.client.visuals.hud.StatElement;
import net.client.visuals.module.setting.BooleanSetting;

import java.time.LocalTime;

/** The real-world time. */
public final class ClockElement extends StatElement {
	public final BooleanSetting twentyFour = add(new BooleanSetting("24h", "liquidclient.hud.clock.24h", true));
	public final BooleanSetting seconds = add(new BooleanSetting("seconds", "liquidclient.hud.clock.seconds", false));
	private long shownSecond = -1L;
	private int shownFormat = -1;

	public ClockElement() {
		super("hud_clock", false, 0.995f, 0.25f, 0xE087, "Time", false);
	}

	@Override
	protected void refresh(HudContext ctx) {
		long second = System.currentTimeMillis() / 1000L;
		int format = (twentyFour.get() ? 1 : 0) | (seconds.get() ? 2 : 0);
		if (second == shownSecond && format == shownFormat) {
			return;
		}
		shownSecond = second;
		shownFormat = format;
		LocalTime now = LocalTime.now();
		int hour = now.getHour();
		StringBuilder text = new StringBuilder(11);
		text.append(twentyFour.get() ? hour : hour % 12 == 0 ? 12 : hour % 12);
		text.append(':');
		pad(text, now.getMinute());
		if (seconds.get()) {
			text.append(':');
			pad(text, now.getSecond());
		}
		setValue(text.toString());
		unit.setText(twentyFour.get() ? "" : hour < 12 ? "AM" : "PM");
	}

	private static void pad(StringBuilder text, int value) {
		if (value < 10) {
			text.append('0');
		}
		text.append(value);
	}
}
