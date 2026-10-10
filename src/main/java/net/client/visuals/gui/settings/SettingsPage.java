package net.client.visuals.gui.settings;

import net.client.visuals.gui.window.Ui;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A sidebar entry: either a pane of grouped sections (built on first use) or a shortcut that opens
 * another screen (resource packs, shader packs, mods).
 */
final class SettingsPage {
	/** A titled group of rows with an optional footnote. */
	record Section(@Nullable TextLabel title, List<SettingRow> rows, List<TextLabel> footer) {
		static Section of(@Nullable String title, List<SettingRow> rows) {
			return new Section(title == null ? null : Ui.sectionLabel(title), rows, List.of());
		}

		Section footnote(String text) {
			return new Section(title, rows, Ui.wrap(text, 340f, LiquidText.TextStyle.CAPTION));
		}

		boolean hasVisibleRows() {
			for (SettingRow row : rows) {
				if (row.visible()) {
					return true;
				}
			}
			return false;
		}
	}

	final String name;
	final int icon;
	final int color;
	final boolean groupStart;
	final TextLabel sidebarLabel;
	final TextLabel titleLabel;
	final List<TextLabel> descriptionLines;
	final Smooth hover = new Smooth(0f, Motion.HOVER);
	private final @Nullable Consumer<List<Section>> builder;
	private final @Nullable Runnable open;
	private @Nullable List<Section> sections;

	private SettingsPage(String name, String description, int icon, int color, boolean groupStart,
			@Nullable Consumer<List<Section>> builder, @Nullable Runnable open) {
		this.name = name;
		this.icon = icon;
		this.color = color;
		this.groupStart = groupStart;
		this.builder = builder;
		this.open = open;
		this.sidebarLabel = new TextLabel(name, LiquidText.TextStyle.BODY);
		this.titleLabel = new TextLabel(name, LiquidText.TextStyle.TITLE);
		this.descriptionLines = description.isEmpty() ? List.of() : Ui.wrap(description, 340f, LiquidText.TextStyle.BODY);
	}

	static SettingsPage pane(String name, String description, int icon, int color, boolean groupStart, Consumer<List<Section>> builder) {
		return new SettingsPage(name, description, icon, color, groupStart, builder, null);
	}

	static SettingsPage shortcut(String name, int icon, int color, boolean groupStart, Runnable open) {
		return new SettingsPage(name, "", icon, color, groupStart, null, open);
	}

	boolean isShortcut() {
		return open != null;
	}

	void open() {
		if (open != null) {
			open.run();
		}
	}

	List<Section> sections() {
		if (sections == null) {
			List<Section> built = new ArrayList<>();
			if (builder != null) {
				builder.accept(built);
			}
			sections = built;
		}
		return sections;
	}
}
