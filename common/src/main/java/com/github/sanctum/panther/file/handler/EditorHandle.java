package com.github.sanctum.panther.file.handler;

import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.MemorySpace;
import java.io.InputStream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Configurable.Handle} that is responsible for {@link Configurable.Editor}
 * internal operations such as initialization and overall value management.
 *
 * @since 1.0.2
 */
public abstract class EditorHandle extends Configurable.Handle {

	/**
	 * The event that takes place when a {@link Configurable.Editor} gets instantiated through its constructor.
	 * Supply a configurable with the provided parameters.
	 *
	 * @param host The host constructing the configurable editor to be cached.
	 * @param name The name of the file.
	 * @param desc The possible extra directory of the file.
	 * @param extension The extension of the file (the type)
	 * @return The configurable to be used with file construction.
	 */
	public abstract @NotNull Configurable onInstantiate(@NotNull Configurable.Host host, @NotNull final String name, @Nullable final String desc, @NotNull Configurable.Extension extension);

	/**
	 * The event that takes place when a {@link Configurable.Editor} has its {@link Configurable.Editor#reset()} method executed.
	 * Supply the {@link InputStream} containing the bytes to copy over.
	 *
	 * @param host The host resetting this editor.
	 * @param name The name of the file being reset.
	 * @param fileName The new name of the file being reset (or null)
	 * @return An {@link InputStream} containing the information to be copied.
	 */
	public abstract @Nullable InputStream onReset(@NotNull Configurable.Host host, @NotNull String name, @Nullable String fileName);

	/**
	 * The event that takes place on each object provided from a {@link com.github.sanctum.panther.file.DataTable} write operation.
	 * Supply overwrite information regarding value reception.
	 *
	 * @param value The value being written.
	 * @param key The key the value is tagged to.
	 * @param memorySpace The memory space the information belongs to.
	 * @param toReplace whether the object is to overwrite an existing link.
	 * @return the value as written; possibly null if replacement is disabled
	 */
	@Contract("_, _, _, true -> param1")
	public abstract <V> @Nullable V onWriteFromTable(@NotNull V value, @NotNull String key, @NotNull MemorySpace memorySpace, boolean toReplace);

}
