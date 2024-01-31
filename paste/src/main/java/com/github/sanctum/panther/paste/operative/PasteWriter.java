package com.github.sanctum.panther.paste.operative;

import com.github.sanctum.panther.file.JsonAdapter;
import com.github.sanctum.panther.util.JsonIntermediate;
import java.io.NotSerializableException;
import java.util.Collection;

/**
 * Signifies an object that can write information to the web using provided data
 */
public interface PasteWriter {

	/**
	 * Write information to a web connection.
	 *
	 * @param info the information to write.
	 * @return a response from the web.
	 */
	PasteResponse write(String... info);

	/**
	 * Write information to a web connection.
	 *
	 * @param info the information to write.
	 * @return a response from the web.
	 */
	PasteResponse write(Collection<? extends CharSequence> info);

	/**
	 * Write information to a web connection using serializable data.
	 *
	 * @param t the data to write
	 * @param <T> the data type.
	 * @return a response from the web.
	 */
	default <T> PasteResponse write(T t) {
		if (t instanceof JsonIntermediate) {
			return write(((JsonIntermediate)t).toJsonString());
		}
		if (t instanceof JsonAdapter) {
			return write(((JsonAdapter)t).write(t).toString());
		}
		return write(t.toString());
	}

}
