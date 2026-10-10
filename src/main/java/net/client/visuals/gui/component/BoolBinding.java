package net.client.visuals.gui.component;

/**
 * Two-way boolean binding between a control and its backing state (config field, mock module...).
 * Created once at layout time, so toggling never allocates.
 */
public interface BoolBinding {
	boolean get();

	void set(boolean value);

	/** Self-contained state, used by the mock cards. */
	static BoolBinding of(boolean initial) {
		return new BoolBinding() {
			private boolean value = initial;

			@Override
			public boolean get() {
				return value;
			}

			@Override
			public void set(boolean value) {
				this.value = value;
			}
		};
	}
}
