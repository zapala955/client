package net.client.visuals.browser;

import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Finds an installed Chromium-based browser. Edge ships with Windows 10/11, so on Windows there is
 * practically always one. Nothing is downloaded or bundled.
 */
final class BrowserExecutable {
	private BrowserExecutable() {
	}

	/**
	 * @param preference 0 auto, 1 edge, 2 chrome, 3 brave (falls back to auto when not installed)
	 */
	static @Nullable Path find(int preference) {
		List<Path> edge = new ArrayList<>();
		List<Path> chrome = new ArrayList<>();
		List<Path> brave = new ArrayList<>();
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (os.contains("win")) {
			for (String root : new String[]{System.getenv("ProgramFiles(x86)"), System.getenv("ProgramFiles"), System.getenv("LOCALAPPDATA")}) {
				if (root == null) {
					continue;
				}
				edge.add(Path.of(root, "Microsoft", "Edge", "Application", "msedge.exe"));
				chrome.add(Path.of(root, "Google", "Chrome", "Application", "chrome.exe"));
				brave.add(Path.of(root, "BraveSoftware", "Brave-Browser", "Application", "brave.exe"));
			}
		} else if (os.contains("mac")) {
			edge.add(Path.of("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge"));
			chrome.add(Path.of("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"));
			brave.add(Path.of("/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"));
		} else {
			edge.add(Path.of("/usr/bin/microsoft-edge"));
			chrome.add(Path.of("/usr/bin/google-chrome"));
			chrome.add(Path.of("/usr/bin/chromium"));
			chrome.add(Path.of("/usr/bin/chromium-browser"));
			brave.add(Path.of("/usr/bin/brave-browser"));
		}
		List<List<Path>> order = switch (preference) {
			case 1 -> List.of(edge, chrome, brave);
			case 2 -> List.of(chrome, edge, brave);
			case 3 -> List.of(brave, edge, chrome);
			default -> List.of(edge, chrome, brave);
		};
		for (List<Path> candidates : order) {
			for (Path candidate : candidates) {
				if (Files.isRegularFile(candidate)) {
					return candidate;
				}
			}
		}
		return null;
	}
}
