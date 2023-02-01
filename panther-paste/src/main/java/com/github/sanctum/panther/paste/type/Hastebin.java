package com.github.sanctum.panther.paste.type;

import org.jetbrains.annotations.NotNull;

/**
 * An object for handling <strong>hastebin</strong> services over the web!
 *
 * @see <a href="https://www.toptal.com/developers/hastebin">https://www.toptal.com/developers/hastebin</a>
 */
public interface Hastebin extends Manipulable {
	/**
	 * The only valid methods for this container is {@link HasteOptions#isRaw()} & {@link HasteOptions#setRaw(boolean)}
	 *
	 * @return The options object for this hastebin connection.
	 */
	@Override
	// There are very few options for hastebin most of the provision is empty.
	// FIXME
	//  - doc which ones do apply
	//  - reduce PasteOptions to only those that apply
	@NotNull HasteOptions getOptions();

}
