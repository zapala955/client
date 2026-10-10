package net.client.visuals.gui.servers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.client.visuals.LiquidClient;
import net.client.visuals.gui.window.PillButton;
import net.client.visuals.gui.window.ScrollArea;
import net.client.visuals.gui.window.Segmented;
import net.client.visuals.gui.window.Sheet;
import net.client.visuals.gui.window.TextField;
import net.client.visuals.gui.window.Ui;
import net.client.visuals.gui.window.WindowScreen;
import net.client.visuals.render.ColorUtil;
import net.client.visuals.render.HighDpiEngine;
import net.client.visuals.render.IconFont;
import net.client.visuals.render.LiquidText;
import net.client.visuals.render.RenderUtils;
import net.client.visuals.render.StyledText;
import net.client.visuals.render.TextLabel;
import net.client.visuals.render.UiSounds;
import net.client.visuals.render.anim.Motion;
import net.client.visuals.render.anim.Smooth;
import net.client.visuals.render.anim.UiFrame;
import net.client.visuals.render.background.ImageDraw;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.jspecify.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Multiplayer as a Liquid window: server cards with favicon, full-colour MOTD, live player count and
 * signal bars, a Local Network section, drag-to-reorder, and sheets for Add / Edit / Direct Connect /
 * Delete. Pinging, LAN discovery and every list mutation follow {@code JoinMultiplayerScreen} and
 * {@code ServerSelectionList} exactly (hidden direct-connect entries, unhide on add, icon
 * validation, persistence callbacks).
 */
public final class ServersScreen extends WindowScreen {
	private static final float ROW_H = 46f;
	private static final float PITCH = 50f;
	private static final float FOOTER_H = 46f;
	private static final float EMPTY_H = 132f;
	private static final float SECTION_H = 22f;
	private static final int ICON_PLUS = 0xE13D;
	private static final int ICON_LINK = 0xE102;
	private static final int ICON_PLAY = 0xE13C;
	private static final int ICON_EDIT = 0xE1F9;
	private static final int ICON_TRASH = 0xE18E;
	private static final int ICON_REFRESH = 0xE145;
	private static final int ICON_SERVER = 0xE153;
	private static final int ICON_NETWORK = 0xE125;
	private static final int ICON_USERS = 0xE1A4;
	private static final int ICON_OFFLINE = 0xE1AF;
	private static final int ICON_ALERT = 0xE193;
	private static final String[] PACK_OPTIONS = {"Prompt", "Enabled", "Disabled"};
	private static final ServerData.ServerPackStatus[] PACK_VALUES = {
			ServerData.ServerPackStatus.PROMPT, ServerData.ServerPackStatus.ENABLED, ServerData.ServerPackStatus.DISABLED};

	private static final ThreadPoolExecutor PING_POOL = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder()
			.setNameFormat("Liquid Server Pinger #%d")
			.setDaemon(true)
			.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LiquidClient.LOGGER))
			.build());
	private static final Component CANT_RESOLVE = Component.translatable("multiplayer.status.cannot_resolve").withColor(0xFF6961);
	private static final Component CANT_CONNECT = Component.translatable("multiplayer.status.cannot_connect").withColor(0xFF6961);

	private final ServerStatusPinger pinger = new ServerStatusPinger();
	private final ScrollArea scroll = new ScrollArea();
	private final List<ServerRow> online = new ArrayList<>();
	private final List<LanRow> lan = new ArrayList<>();
	private final TextLabel lanSection = Ui.sectionLabel("Local Network");
	private final TextLabel scanning = new TextLabel("Looking for games on your network…", LiquidText.TextStyle.BODY);
	private final TextLabel emptyTitle = new TextLabel("No servers yet", LiquidText.TextStyle.HEADLINE);
	private final TextLabel emptyText = new TextLabel("Add a server, or connect to one directly.", LiquidText.TextStyle.BODY);
	private final List<TextLabel> refreshTip = List.of(new TextLabel("Refresh  (F5)", LiquidText.TextStyle.BODY));
	private @Nullable ServerList servers;
	private LanServerDetection.@Nullable LanServerList lanList;
	private LanServerDetection.@Nullable LanServerDetector lanDetector;
	private boolean started;
	private @Nullable Entry selected;
	private @Nullable String reselect;

	private PillButton add;
	private PillButton direct;
	private PillButton refresh;
	private PillButton join;
	private PillButton edit;
	private PillButton delete;
	private PillButton emptyAdd;
	private PillButton emptyDirect;
	private float listX;
	private float listY;
	private float listW;
	private float listH;

	// Drag-to-reorder
	private @Nullable ServerRow pressed;
	private double pressY;
	private @Nullable ServerRow dragging;
	private float dragGrab;
	private float dragMouseY;
	private int dragTarget;

	// Content layout (content coordinates, recomputed every frame)
	private float onlineTop;
	private float lanHeaderTop;
	private float lanTop;

	public ServersScreen(@Nullable Screen parent) {
		super(Component.translatable("liquidclient.servers.title"), parent);
	}

	// ---- Layout --------------------------------------------------------------------------------

	@Override
	protected float preferredWidth() {
		return 560f;
	}

	@Override
	protected float preferredHeight() {
		return 370f;
	}

	@Override
	protected void layoutWindow(boolean first) {
		if (first) {
			add = new PillButton("Add Server", ICON_PLUS, PillButton.Kind.PRIMARY, () -> openServerSheet(null));
			direct = new PillButton("Direct Connect", ICON_LINK, PillButton.Kind.SECONDARY, this::openDirectSheet);
			refresh = new PillButton("", ICON_REFRESH, PillButton.Kind.PLAIN, this::refresh);
			join = new PillButton("Join", ICON_PLAY, PillButton.Kind.PRIMARY, this::joinSelected)
					.enabledWhen(() -> selected != null);
			edit = new PillButton("Edit", ICON_EDIT, PillButton.Kind.SECONDARY, () -> {
				if (selected instanceof ServerRow row) {
					openServerSheet(row);
				}
			}).enabledWhen(() -> selected instanceof ServerRow);
			delete = new PillButton("Delete", ICON_TRASH, PillButton.Kind.DESTRUCTIVE, this::confirmDelete)
					.enabledWhen(() -> selected instanceof ServerRow);
			emptyAdd = new PillButton("Add Server", ICON_PLUS, PillButton.Kind.PRIMARY, () -> openServerSheet(null));
			emptyDirect = new PillButton("Direct Connect", ICON_LINK, PillButton.Kind.SECONDARY, this::openDirectSheet);
			widgets.add(add);
			widgets.add(direct);
			widgets.add(refresh);
			widgets.add(join);
			widgets.add(edit);
			widgets.add(delete);
		}
		if (!started) {
			start();
		}
		float right = wx + ww - 12f;
		float ty = wy + (TITLE_H - Ui.CONTROL_H) * 0.5f;
		add.setBounds(right - add.naturalWidth(), ty, add.naturalWidth(), Ui.CONTROL_H);
		right -= add.naturalWidth() + 8f;
		boolean roomy = right - direct.naturalWidth() > titleRight() + 30f;
		direct.setBounds(roomy ? right - direct.naturalWidth() : -10000f, ty, direct.naturalWidth(), Ui.CONTROL_H);
		if (roomy) {
			right -= direct.naturalWidth() + 6f;
		}
		refresh.setBounds(right - refresh.naturalWidth(), ty, refresh.naturalWidth(), Ui.CONTROL_H);

		float fy = wy + wh - FOOTER_H + (FOOTER_H - Ui.CONTROL_H) * 0.5f;
		float fx = wx + ww - 12f;
		for (PillButton button : new PillButton[]{join, edit, delete}) {
			float bw = Math.max(64f, button.naturalWidth());
			fx -= bw;
			button.setBounds(fx, fy, bw, Ui.CONTROL_H);
			fx -= 6f;
		}

		listX = wx + 10f;
		listY = wy + TITLE_H + 6f;
		listW = ww - 20f;
		listH = wh - TITLE_H - FOOTER_H - 6f;
	}

	// ---- Lifecycle (JoinMultiplayerScreen#init / #removed / #tick) -----------------------------

	private void start() {
		started = true;
		servers = new ServerList(minecraft);
		servers.load();
		lanList = new LanServerDetection.LanServerList();
		try {
			lanDetector = new LanServerDetection.LanServerDetector(lanList);
			lanDetector.start();
		} catch (Exception e) {
			LiquidClient.LOGGER.warn("Unable to start LAN server detection", e);
		}
		syncOnline();
		if (selected == null) {
			select(online.isEmpty() ? null : online.getFirst());
		}
	}

	@Override
	public void removed() {
		super.removed();
		if (lanDetector != null) {
			lanDetector.interrupt();
			lanDetector = null;
		}
		pinger.removeAll();
		if (selected instanceof ServerRow row) {
			reselect = row.data.ip;
		}
		for (ServerRow row : online) {
			row.close();
		}
		online.clear();
		lan.clear();
		selected = null;
		pressed = null;
		dragging = null;
		started = false;
	}

	@Override
	public void tick() {
		super.tick();
		if (!started || lanList == null) {
			return;
		}
		List<LanServer> found = lanList.takeDirtyServers();
		if (found != null) {
			updateLan(found);
		}
		pinger.tick();
	}

	/** Matches rows to the list by identity, so unchanged servers keep their icon and animation. */
	private void syncOnline() {
		ServerList list = servers;
		if (list == null) {
			return;
		}
		List<ServerRow> next = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			ServerData data = list.get(i);
			ServerRow reuse = null;
			for (ServerRow row : online) {
				if (row.data == data && row.iconAddress.equals(data.ip)) {
					reuse = row;
					break;
				}
			}
			next.add(reuse != null ? reuse : new ServerRow(data, i));
		}
		for (ServerRow row : online) {
			if (!next.contains(row)) {
				row.close();
				if (selected == row) {
					selected = null;
				}
			}
		}
		online.clear();
		online.addAll(next);
		if (reselect != null) {
			for (ServerRow row : online) {
				if (row.data.ip.equals(reselect)) {
					selected = row;
				}
			}
			reselect = null;
		}
		updateSubtitle();
	}

	private void updateLan(List<LanServer> found) {
		LanServer keep = selected instanceof LanRow row ? row.server : null;
		lan.clear();
		for (LanServer server : found) {
			LanRow row = new LanRow(server);
			lan.add(row);
			if (server == keep) {
				selected = row;
			}
		}
		if (keep != null && !(selected instanceof LanRow)) {
			selected = null;
		}
		updateSubtitle();
	}

	private void updateSubtitle() {
		int count = online.size();
		int up = 0;
		for (ServerRow row : online) {
			if (row.data.state() == ServerData.State.SUCCESSFUL) {
				up++;
			}
		}
		String text = count == 1 ? "1 server" : count + " servers";
		if (up > 0) {
			text += "  ·  " + up + " online";
		}
		if (!lan.isEmpty()) {
			text += "  ·  " + lan.size() + " on LAN";
		}
		setSubtitle(text);
	}

	private void refresh() {
		if (selected instanceof ServerRow row) {
			reselect = row.data.ip;
		}
		pinger.removeAll();
		if (servers != null) {
			servers.load(); // fresh ServerData objects: every row re-pings
		}
		syncOnline();
	}

	// ---- Actions -------------------------------------------------------------------------------

	private void joinSelected() {
		if (selected != null) {
			selected.join();
		}
	}

	private void join(ServerData data) {
		ConnectScreen.startConnecting(this, minecraft, ServerAddress.parseString(data.ip), data, false, null);
	}

	private void openServerSheet(@Nullable ServerRow row) {
		boolean adding = row == null;
		TextField name = new TextField(I18n.get("selectServer.defaultName"), false).maxLength(64);
		TextField address = new TextField("play.example.net", false).maxLength(128);
		ServerData.ServerPackStatus initial = ServerData.ServerPackStatus.PROMPT;
		if (row != null) {
			name.setValue(row.data.name);
			address.setValue(row.data.ip);
			initial = row.data.getResourcePackStatus();
		}
		int[] pack = {Math.max(0, Arrays.asList(PACK_VALUES).indexOf(initial))};
		Segmented packs = new Segmented(PACK_OPTIONS, pack[0], index -> pack[0] = index);
		openSheet(new Sheet(this, adding ? "Add Server" : "Edit Server")
				.icon(ICON_SERVER, Ui.ACCENT)
				.field("Server Name", name)
				.field("Server Address", address)
				.field("Server Resource Packs", packs)
				.button("Cancel", PillButton.Kind.SECONDARY, () -> {
				})
				.button(adding ? "Add Server" : "Save", PillButton.Kind.PRIMARY,
						() -> ServerAddress.isValidAddress(address.value()),
						() -> {
							String serverName = name.value().isEmpty() ? I18n.get("selectServer.defaultName") : name.value();
							if (row == null) {
								addServer(serverName, address.value(), PACK_VALUES[pack[0]]);
							} else {
								editServer(row, serverName, address.value(), PACK_VALUES[pack[0]]);
							}
						}));
		if (!adding) {
			focus(address);
		}
	}

	/** JoinMultiplayerScreen#addServerCallback. */
	private void addServer(String name, String ip, ServerData.ServerPackStatus pack) {
		ServerList list = servers;
		if (list == null) {
			return;
		}
		ServerData editing = new ServerData(name, ip, ServerData.Type.OTHER);
		editing.setResourcePackStatus(pack);
		ServerData existing = list.unhide(editing.ip);
		if (existing != null) {
			existing.copyNameIconFrom(editing);
		} else {
			list.add(editing, false);
		}
		list.save();
		reselect = ip;
		syncOnline();
		if (selected != null) {
			select(selected);
		}
	}

	/** JoinMultiplayerScreen#editServerCallback. */
	private void editServer(ServerRow row, String name, String ip, ServerData.ServerPackStatus pack) {
		ServerList list = servers;
		if (list == null) {
			return;
		}
		ServerData current = row.data;
		current.name = name;
		current.ip = ip;
		current.setState(ServerData.State.INITIAL);
		current.setResourcePackStatus(pack);
		list.save();
		reselect = ip;
		syncOnline();
	}

	private void openDirectSheet() {
		TextField address = new TextField("play.example.net", false).maxLength(128);
		address.setValue(minecraft.options.lastMpIp);
		Runnable remember = () -> {
			minecraft.options.lastMpIp = address.value();
			minecraft.options.save();
		};
		openSheet(new Sheet(this, "Direct Connect")
				.icon(ICON_LINK, 0xFF5E5CE6)
				.message("Join a server without adding it to your list.")
				.field("Server Address", address)
				.button("Cancel", PillButton.Kind.SECONDARY, remember)
				.button("Join", PillButton.Kind.PRIMARY, () -> ServerAddress.isValidAddress(address.value()), () -> {
					remember.run();
					directJoin(address.value());
				})
				.onCancel(remember));
	}

	/** JoinMultiplayerScreen#directJoinCallback: direct connections are remembered as hidden entries. */
	private void directJoin(String ip) {
		ServerList list = servers;
		if (list == null) {
			return;
		}
		ServerData existing = list.get(ip);
		if (existing == null) {
			ServerData data = new ServerData(I18n.get("selectServer.defaultName"), ip, ServerData.Type.OTHER);
			list.add(data, true);
			list.save();
			join(data);
		} else {
			join(existing);
		}
	}

	private void confirmDelete() {
		if (!(selected instanceof ServerRow row)) {
			return;
		}
		openSheet(new Sheet(this, "Remove “" + row.data.name + "”?")
				.icon(ICON_TRASH, Ui.DESTRUCTIVE)
				.message("The server will be removed from your list. You can add it again any time.")
				.button("Cancel", PillButton.Kind.SECONDARY, () -> {
				})
				.button("Remove", PillButton.Kind.DANGER, () -> deleteServer(row)));
	}

	/** JoinMultiplayerScreen#deleteCallback. */
	private void deleteServer(ServerRow row) {
		ServerList list = servers;
		if (list == null) {
			return;
		}
		int index = online.indexOf(row);
		list.remove(row.data);
		list.save();
		selected = null;
		syncOnline();
		if (!online.isEmpty()) {
			select(online.get(Math.clamp(index, 0, online.size() - 1)));
		}
	}

	/** Shift+Up/Down: ServerSelectionList.OnlineServerEntry#swap. */
	private void move(ServerRow row, int direction) {
		ServerList list = servers;
		int index = online.indexOf(row);
		int target = index + direction;
		if (list == null || index < 0 || target < 0 || target >= online.size()) {
			return;
		}
		list.swap(index, target);
		Collections.swap(online, index, target);
		UiSounds.click();
		select(row);
	}

	private void commitDrag(ServerRow row, int target) {
		ServerList list = servers;
		int index = online.indexOf(row);
		if (list == null || index < 0 || target == index) {
			return;
		}
		online.remove(index);
		online.add(target, row);
		for (int i = 0; i < online.size(); i++) {
			list.replace(i, online.get(i).data);
		}
		list.save();
	}

	private void select(@Nullable Entry entry) {
		selected = entry;
		if (entry != null) {
			float top = entry.contentTop();
			scroll.reveal(top - 2f, top + ROW_H + 2f);
		}
	}

	private List<Entry> entries() {
		List<Entry> all = new ArrayList<>(online.size() + lan.size());
		all.addAll(online);
		all.addAll(lan);
		return all;
	}

	// ---- Rendering -----------------------------------------------------------------------------

	private float layoutContent() {
		float y = 2f;
		onlineTop = y;
		y += online.isEmpty() ? EMPTY_H : online.size() * PITCH - (PITCH - ROW_H);
		lanHeaderTop = y + 12f;
		lanTop = lanHeaderTop + SECTION_H;
		y = lanTop + (lan.isEmpty() ? 18f : lan.size() * PITCH - (PITCH - ROW_H));
		return y + 6f;
	}

	@Override
	protected void extractBody(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		float dt = UiFrame.dt;
		float content = layoutContent();
		if (dragging != null) {
			// Auto-scroll near the edges while dragging.
			if (mouseY < listY + 20f) {
				scroll.nudge(-260f * dt * (1f - Math.max(0f, mouseY - listY) / 20f));
			} else if (mouseY > listY + listH - 20f) {
				scroll.nudge(260f * dt * (1f - Math.max(0f, listY + listH - mouseY) / 20f));
			}
		}
		float offset = scroll.begin(listX, listY, listW, listH, content);
		Ui.separator(g, wx, wy + wh - FOOTER_H, ww);
		if (refresh.contains(mouseX, mouseY)) {
			tooltip(refresh, refreshTip);
		}

		boolean inList = scroll.contains(mouseX, mouseY);
		int mx = inList && dragging == null ? mouseX : -10000;
		int my = inList && dragging == null ? mouseY : -10000;
		float originY = listY - offset;
		scroll.scissor(g);

		// Saved servers (with the drag gap opened where the dragged card would land).
		if (online.isEmpty()) {
			extractEmpty(g, originY + onlineTop, mouseX, mouseY);
		} else {
			if (dragging != null) {
				float topOfCard = dragMouseY - dragGrab - originY - onlineTop;
				dragTarget = Math.clamp(Math.round(topOfCard / PITCH), 0, online.size() - 1);
			}
			int slot = 0;
			for (ServerRow row : online) {
				if (row == dragging) {
					continue;
				}
				if (dragging != null && slot == dragTarget) {
					slot++;
				}
				row.slot.target(onlineTop + slot * PITCH);
				slot++;
			}
			for (ServerRow row : online) {
				if (row == dragging) {
					continue;
				}
				float y = originY + row.slot.update(dt);
				if (y + ROW_H >= listY && y <= listY + listH) {
					row.extract(g, listX, y, listW, mx, my, 0f);
				} else {
					row.cull();
				}
			}
		}

		// Local network
		float hy = originY + lanHeaderTop;
		LiquidText.draw(g, lanSection, listX + 8f, hy + 6f, Ui.TEXT_3, false);
		if (lan.isEmpty()) {
			Ui.spinner(g, listX + 14f, originY + lanTop + 5f, 4f, 1.2f, Ui.TEXT_3);
			LiquidText.draw(g, scanning, listX + 26f, originY + lanTop + 5f - scanning.capHeight() * 0.5f, Ui.TEXT_3, false);
		} else {
			Ui.spinner(g, listX + 14f + lanSection.width() + 4f, hy + 6f + lanSection.capHeight() * 0.5f, 3f, 1f, Ui.TEXT_3);
			for (int i = 0; i < lan.size(); i++) {
				LanRow row = lan.get(i);
				row.top = lanTop + i * PITCH;
				float y = originY + row.top;
				if (y + ROW_H >= listY && y <= listY + listH) {
					row.extract(g, listX, y, listW, mx, my);
				} else {
					row.drawnY = -100000f;
				}
			}
		}

		// The dragged card floats above everything, following the pointer.
		if (dragging != null) {
			float y = Math.clamp(dragMouseY - dragGrab, listY - 8f, listY + listH - ROW_H + 8f);
			dragging.slot.snap(y - originY);
			dragging.extract(g, listX, y, listW, -10000, -10000, 1f);
		}
		g.disableScissor();
		scroll.scrollbar(g, mouseX, mouseY);
	}

	private void extractEmpty(GuiGraphicsExtractor g, float top, int mouseX, int mouseY) {
		float cx = listX + listW * 0.5f;
		float y = top + 14f;
		Ui.tile(g, cx - 20f, y, 40f, Ui.ACCENT);
		IconFont.draw(g, ICON_SERVER, cx, y + 20f, 20f, 0xFFFFFFFF, 0);
		LiquidText.drawCentered(g, emptyTitle, cx, y + 52f, Ui.TEXT, false);
		LiquidText.drawCentered(g, emptyText, cx, y + 66f, Ui.TEXT_2, false);
		float aw = emptyAdd.naturalWidth();
		float dw = emptyDirect.naturalWidth();
		float bx = cx - (aw + dw + 8f) * 0.5f;
		emptyDirect.setBounds(bx, y + 82f, dw, Ui.CONTROL_H);
		emptyAdd.setBounds(bx + dw + 8f, y + 82f, aw, Ui.CONTROL_H);
		emptyDirect.extract(g, mouseX, mouseY);
		emptyAdd.extract(g, mouseX, mouseY);
	}

	/** Ping as four signal bars; while pinging, a wave runs through them. */
	private static void signal(GuiGraphicsExtractor g, float right, float bottom, int lit, int color, boolean pinging, float phase) {
		float bw = 2.5f;
		float gap = 1.5f;
		float x = right - (bw * 4f + gap * 3f);
		for (int i = 0; i < 4; i++) {
			float bh = 3.5f + i * 2.25f;
			int c;
			if (pinging) {
				float wave = 0.5f + 0.5f * (float) Math.sin(UiFrame.time * 6f - i * 0.9f + phase);
				c = ColorUtil.fade(0xFFFFFFFF, 0.18f + 0.45f * wave);
			} else {
				c = i < lit ? color : 0x2EFFFFFF;
			}
			RenderUtils.fill(g, x, bottom - bh, bw, bh, 1f, c);
			x += bw + gap;
		}
	}

	// ---- Input ---------------------------------------------------------------------------------

	private @Nullable Entry entryAt(double mx, double my) {
		if (!scroll.contains(mx, my)) {
			return null;
		}
		for (ServerRow row : online) {
			if (my >= row.drawnY && my < row.drawnY + ROW_H) {
				return row;
			}
		}
		for (LanRow row : lan) {
			if (my >= row.drawnY && my < row.drawnY + ROW_H) {
				return row;
			}
		}
		return null;
	}

	@Override
	protected boolean bodyClicked(double mx, double my, int button, boolean doubleClick) {
		if (online.isEmpty() && (emptyAdd.mouseClicked(mx, my, button) || emptyDirect.mouseClicked(mx, my, button))) {
			return true;
		}
		Entry entry = entryAt(mx, my);
		if (entry == null || button != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		if (entry.overIcon(mx, my) || doubleClick) {
			UiSounds.click();
			select(entry);
			entry.join();
			return true;
		}
		if (selected != entry) {
			UiSounds.click();
		}
		select(entry);
		if (entry instanceof ServerRow row) {
			pressed = row;
			pressY = my;
		}
		return true;
	}

	@Override
	protected boolean bodyDragged(double mx, double my, int button, double dx, double dy) {
		if (pressed == null) {
			return false;
		}
		if (dragging == null && Math.abs(my - pressY) > 4.0 && online.size() > 1) {
			dragging = pressed;
			dragGrab = (float) (pressY - pressed.drawnY);
			UiSounds.click();
		}
		dragMouseY = (float) my;
		return true;
	}

	@Override
	protected boolean bodyReleased(double mx, double my, int button) {
		if (online.isEmpty() && (emptyAdd.mouseReleased(mx, my, button) || emptyDirect.mouseReleased(mx, my, button))) {
			return true;
		}
		ServerRow row = dragging;
		pressed = null;
		dragging = null;
		if (row != null) {
			commitDrag(row, dragTarget);
			return true;
		}
		return false;
	}

	@Override
	protected boolean bodyScrolled(double mx, double my, double amount) {
		return scroll.scroll(mx, my, amount);
	}

	@Override
	protected boolean bodyKey(KeyEvent event) {
		if (event.key() == InputConstants.KEY_F5) {
			refresh();
			return true;
		}
		List<Entry> all = entries();
		if (all.isEmpty()) {
			return false;
		}
		int index = selected == null ? -1 : all.indexOf(selected);
		if (event.hasShiftDown() && selected instanceof ServerRow row && (event.isUp() || event.isDown())) {
			move(row, event.isDown() ? 1 : -1);
			return true;
		}
		if (event.isDown()) {
			select(all.get(Math.min(all.size() - 1, index + 1)));
			return true;
		}
		if (event.isUp()) {
			select(all.get(Math.max(0, index - 1)));
			return true;
		}
		if ((event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) && selected != null) {
			selected.join();
			return true;
		}
		if (event.key() == InputConstants.KEY_DELETE && selected instanceof ServerRow) {
			confirmDelete();
			return true;
		}
		return false;
	}

	// ---- Rows ----------------------------------------------------------------------------------

	private abstract sealed class Entry permits ServerRow, LanRow {
		final Smooth hover = new Smooth(0f, Motion.HOVER);
		final Smooth chosen = new Smooth(0f, Motion.HOVER);
		final Smooth iconHover = new Smooth(0f, Motion.HOVER);
		float drawnY = -100000f;
		float iconX;
		float iconY;

		abstract void join();

		abstract float contentTop();

		boolean overIcon(double mx, double my) {
			float size = ROW_H - 12f;
			return mx >= iconX && mx < iconX + size && my >= iconY && my < iconY + size;
		}

		/** Hover / selection highlight; returns the eased hover amount. */
		float background(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY) {
			boolean over = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW_H;
			float h = hover.target(over).update(UiFrame.dt);
			float s = chosen.target(selected == this).update(UiFrame.dt);
			Ui.rowHighlight(g, x, y, w, ROW_H, h, s);
			drawnY = y;
			iconX = x + 6f;
			iconY = y + 6f;
			return h;
		}

		/** Hovering the row reveals a play glyph over the icon; hovering the icon enlarges it. */
		void playOverlay(GuiGraphicsExtractor g, float h, int mouseX, int mouseY) {
			float size = ROW_H - 12f;
			float ih = iconHover.target(h > 0.5f && overIcon(mouseX, mouseY)).update(UiFrame.dt);
			if (h > 0.01f) {
				RenderUtils.fill(g, iconX, iconY, size, size, 8f, ColorUtil.fade(0x8C000000, h * (0.7f + 0.3f * ih)));
				IconFont.draw(g, IconFont.FILLED | 0xE037, iconX + size * 0.5f + 0.5f, iconY + size * 0.5f,
						13f + 3f * ih, ColorUtil.fade(0xFFFFFFFF, h * (0.85f + 0.15f * ih)), 0);
			}
		}

		void cull() {
			drawnY = -100000f;
			hover.target(0f).update(UiFrame.dt);
		}
	}

	private enum Status { PINGING, ONLINE, OFFLINE, INCOMPATIBLE }

	private final class ServerRow extends Entry {
		final ServerData data;
		final String iconAddress;
		final FaviconTexture icon;
		@Nullable TextureSetup iconSetup;
		byte @Nullable [] lastIconBytes;
		final Smooth slot = new Smooth(0f, 16f);
		final Smooth lift = new Smooth(0f, Motion.HOVER);
		final TextLabel name = new TextLabel("", LiquidText.TextStyle.LABEL);
		final StyledText motd = new StyledText(LiquidText.TextStyle.BODY, LiquidText.TextStyle.LABEL, 2);
		final TextLabel statusText = new TextLabel("", LiquidText.TextStyle.CAPTION);
		final TextLabel players = new TextLabel("", LiquidText.TextStyle.CAPTION);
		List<TextLabel> statusTip = List.of();
		List<TextLabel> playersTip = List.of();
		ServerData.@Nullable State shownState;
		Status status = Status.PINGING;
		int bars;
		int barColor;
		final float phase;

		ServerRow(ServerData data, int index) {
			this.data = data;
			this.iconAddress = data.ip;
			this.icon = FaviconTexture.forServer(minecraft.getTextureManager(), data.ip);
			this.slot.snap(onlineTop + index * PITCH);
			this.phase = index * 1.3f;
		}

		@Override
		void join() {
			ServersScreen.this.join(data);
		}

		@Override
		float contentTop() {
			return slot.target();
		}

		/** ServerSelectionList.OnlineServerEntry: the first time the card is shown, ping it. */
		private void ping() {
			data.setState(ServerData.State.PINGING);
			data.motd = CommonComponents.EMPTY;
			data.status = CommonComponents.EMPTY;
			ServerList list = servers;
			PING_POOL.submit(() -> {
				try {
					pinger.pingServer(data, () -> minecraft.execute(() -> {
						if (list != null && list == servers) {
							list.save();
						}
					}), () -> {
						data.setState(data.protocol == SharedConstants.getCurrentVersion().protocolVersion()
								? ServerData.State.SUCCESSFUL : ServerData.State.INCOMPATIBLE);
						minecraft.execute(ServersScreen.this::updateSubtitle);
					}, EventLoopGroupHolder.remote(minecraft.options.useNativeTransport()));
				} catch (UnknownHostException e) {
					data.setState(ServerData.State.UNREACHABLE);
					data.motd = CANT_RESOLVE;
				} catch (Exception e) {
					data.setState(ServerData.State.UNREACHABLE);
					data.motd = CANT_CONNECT;
				}
			});
		}

		/** Rebuilds the status labels; only when the state changes (they're cached afterwards). */
		private void refreshStatus(ServerData.State state) {
			shownState = state;
			playersTip = List.of();
			players.setText("");
			switch (state) {
				case INITIAL, PINGING -> {
					status = Status.PINGING;
					statusText.setText("Pinging…");
					statusTip = List.of();
				}
				case UNREACHABLE -> {
					status = Status.OFFLINE;
					statusText.setText("Offline");
					statusTip = List.of(new TextLabel("No connection", LiquidText.TextStyle.BODY));
				}
				case INCOMPATIBLE -> {
					status = Status.INCOMPATIBLE;
					statusText.setText(data.version.getString());
					statusTip = List.of(new TextLabel("Incompatible version", LiquidText.TextStyle.LABEL),
							new TextLabel("Server runs " + data.version.getString(), LiquidText.TextStyle.BODY));
					playerInfo();
				}
				case SUCCESSFUL -> {
					status = Status.ONLINE;
					long ping = data.ping;
					bars = ping < 150L ? 4 : ping < 300L ? 3 : ping < 600L ? 2 : 1;
					barColor = ping < 150L ? Ui.SUCCESS : ping < 300L ? 0xFFA6E22E : ping < 600L ? 0xFFFFD60A : ping < 1000L ? Ui.WARNING : Ui.DESTRUCTIVE;
					statusText.setText(ping + " ms");
					statusTip = List.of(new TextLabel("Latency", LiquidText.TextStyle.LABEL),
							new TextLabel(ping + " ms", LiquidText.TextStyle.BODY));
					playerInfo();
				}
			}
			if (ServersScreen.this.started) {
				updateSubtitle();
			}
		}

		private void playerInfo() {
			if (data.players != null) {
				players.setText(data.players.online() + " / " + data.players.max());
				List<TextLabel> tip = new ArrayList<>();
				tip.add(new TextLabel(data.players.online() + (data.players.online() == 1 ? " player online" : " players online"),
						LiquidText.TextStyle.LABEL));
				for (Component line : data.playerList) {
					if (tip.size() > 12) {
						break;
					}
					tip.add(new TextLabel(line.getString(), LiquidText.TextStyle.BODY));
				}
				playersTip = tip;
			} else if (!data.status.getString().isEmpty()) {
				players.setText(data.status.getString());
			}
		}

		private void syncIcon() {
			byte[] bytes = data.getIconBytes();
			if (bytes == lastIconBytes || Arrays.equals(bytes, lastIconBytes)) {
				lastIconBytes = bytes;
				return;
			}
			if (bytes == null) {
				icon.clear();
				iconSetup = null;
				lastIconBytes = null;
				return;
			}
			try {
				icon.upload(NativeImage.read(bytes));
				AbstractTexture texture = minecraft.getTextureManager().getTexture(icon.textureLocation());
				iconSetup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
				lastIconBytes = bytes;
			} catch (Throwable t) {
				LiquidClient.LOGGER.error("Invalid icon for server {} ({})", data.name, data.ip, t);
				data.setIconBytes(null);
				if (servers != null) {
					servers.save();
				}
			}
		}

		void extract(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY, float dragged) {
			if (data.state() == ServerData.State.INITIAL) {
				ping();
			}
			ServerData.State state = data.state();
			if (state != shownState) {
				refreshStatus(state);
			}
			syncIcon();
			float dt = UiFrame.dt;
			float l = lift.target(dragged).update(dt);
			var pose = g.pose();
			if (l > 0.01f) {
				float s = 1f + 0.018f * l;
				pose.pushMatrix();
				pose.translate(x + w * 0.5f, y + ROW_H * 0.5f);
				pose.scale(s, s);
				pose.translate(-(x + w * 0.5f), -(y + ROW_H * 0.5f));
				RenderUtils.shadow(g, x, y + 6f, w, ROW_H, Ui.ROW_RADIUS, 14f, ColorUtil.fade(0x99000000, l));
				RenderUtils.fill(g, x, y, w, ROW_H, Ui.ROW_RADIUS, ColorUtil.fade(0xF22C2D33, l));
				RenderUtils.stroke(g, x, y, w, ROW_H, Ui.ROW_RADIUS, HighDpiEngine.px(), ColorUtil.fade(0x40FFFFFF, l), ColorUtil.fade(0x14FFFFFF, l));
			}
			float h = background(g, x, y, w, mouseX, mouseY);
			if (dragged > 0.5f) {
				drawnY = -100000f;
			}

			float size = ROW_H - 12f;
			if (iconSetup != null) {
				RenderUtils.shadow(g, iconX, iconY + 1f, size, size, 8f, 3f, 0x59000000);
				ImageDraw.rounded(g, iconSetup, iconX, iconY, size, size, 8f, 0xFFFFFFFF);
			} else {
				Ui.tile(g, iconX, iconY, size, 0xFF3A3A3C);
				IconFont.draw(g, ICON_SERVER, iconX + size * 0.5f, iconY + size * 0.5f, 15f, 0xD9FFFFFF, 0);
			}
			playOverlay(g, h, mouseX, mouseY);
			RenderUtils.stroke(g, iconX, iconY, size, size, 8f, HighDpiEngine.px(), 0x26FFFFFF, 0x0AFFFFFF);

			// Status cluster (right): signal + latency on the name line, players below.
			float right = x + w - 12f;
			float topLine = y + 8.5f;
			float clusterW;
			switch (status) {
				case ONLINE -> {
					signal(g, right, topLine + 7f, bars, barColor, false, 0f);
					LiquidText.draw(g, statusText, right - 16f - statusText.width(), topLine + 0.75f, Ui.TEXT_2, false);
					clusterW = 18f + statusText.width();
					hoverTip(mouseX, mouseY, right - clusterW, topLine - 2f, clusterW, 11f, statusTip);
				}
				case PINGING -> {
					signal(g, right, topLine + 7f, 0, 0, true, phase);
					LiquidText.draw(g, statusText, right - 16f - statusText.width(), topLine + 0.75f, Ui.TEXT_3, false);
					clusterW = 18f + statusText.width();
				}
				case OFFLINE -> {
					IconFont.draw(g, ICON_OFFLINE, right - 5f, topLine + 3.5f, 10f, Ui.TEXT_3, 0);
					LiquidText.draw(g, statusText, right - 14f - statusText.width(), topLine + 0.75f, Ui.TEXT_3, false);
					clusterW = 16f + statusText.width();
					hoverTip(mouseX, mouseY, right - clusterW, topLine - 2f, clusterW, 11f, statusTip);
				}
				default -> {
					float pw = statusText.width() + 18f;
					float px = right - pw;
					RenderUtils.fill(g, px, topLine - 3.5f, pw, 14f, 7f, ColorUtil.fade(Ui.DESTRUCTIVE, 0.18f));
					IconFont.draw(g, ICON_ALERT, px + 7f, topLine + 3.5f, 7.5f, Ui.DESTRUCTIVE, 0);
					LiquidText.draw(g, statusText, px + 12.5f, topLine + 3.5f - statusText.capHeight() * 0.5f, Ui.DESTRUCTIVE, false);
					clusterW = pw;
					hoverTip(mouseX, mouseY, px, topLine - 3.5f, pw, 14f, statusTip);
				}
			}
			if (!players.text().isEmpty()) {
				float py = y + 22f;
				LiquidText.draw(g, players, right - players.width(), py, Ui.TEXT_2, false);
				IconFont.draw(g, ICON_USERS, right - players.width() - 7f, py + players.capHeight() * 0.5f, 8f, Ui.TEXT_3, 0);
				float pw = players.width() + 14f;
				clusterW = Math.max(clusterW, pw);
				hoverTip(mouseX, mouseY, right - pw, py - 3f, pw, 11f, playersTip);
			}

			float tx = iconX + size + 10f;
			float textW = right - clusterW - 10f - tx;
			name.setText(data.name);
			name.maxWidth(textW);
			LiquidText.draw(g, name, tx, topLine, Ui.TEXT, false);
			motd.set(data.motd, right - 8f - tx - (players.text().isEmpty() ? 0f : players.width() + 16f));
			motd.draw(g, tx, y + 21f, 10.5f, Ui.TEXT_2);
			if (l > 0.01f) {
				pose.popMatrix();
			}
		}

		private void hoverTip(int mouseX, int mouseY, float x, float y, float w, float h, List<TextLabel> lines) {
			if (!lines.isEmpty() && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
				tooltip(lines, lines);
			}
		}

		void close() {
			if (!icon.isClosed()) {
				icon.close();
			}
		}
	}

	private final class LanRow extends Entry {
		final LanServer server;
		final TextLabel name;
		final TextLabel detail;
		final TextLabel badge = new TextLabel("LAN", LiquidText.TextStyle.CAPTION);
		float top;

		LanRow(LanServer server) {
			this.server = server;
			this.name = new TextLabel(server.getMotd(), LiquidText.TextStyle.LABEL);
			this.detail = new TextLabel(minecraft.options.hideServerAddress ? "Address hidden" : server.getAddress(), LiquidText.TextStyle.BODY);
		}

		@Override
		void join() {
			ServersScreen.this.join(new ServerData(server.getMotd(), server.getAddress(), ServerData.Type.LAN));
		}

		@Override
		float contentTop() {
			return top;
		}

		void extract(GuiGraphicsExtractor g, float x, float y, float w, int mouseX, int mouseY) {
			float h = background(g, x, y, w, mouseX, mouseY);
			float size = ROW_H - 12f;
			Ui.tile(g, iconX, iconY, size, 0xFF30B0C7);
			IconFont.draw(g, ICON_NETWORK, iconX + size * 0.5f, iconY + size * 0.5f, 15f, 0xF2FFFFFF, 0);
			playOverlay(g, h, mouseX, mouseY);

			float right = x + w - 12f;
			float bw = badge.width() + 12f;
			float by = y + ROW_H * 0.5f - 7f;
			RenderUtils.fill(g, right - bw, by, bw, 14f, 7f, 0x2E30B0C7);
			LiquidText.draw(g, badge, right - bw + 6f, by + 7f - badge.capHeight() * 0.5f, 0xFF64D2FF, false);

			float tx = iconX + size + 10f;
			float textW = right - bw - 10f - tx;
			name.maxWidth(textW);
			detail.maxWidth(textW);
			LiquidText.draw(g, name, tx, y + 13f, Ui.TEXT, false);
			LiquidText.draw(g, detail, tx, y + 25f, Ui.TEXT_2, false);
		}
	}
}
