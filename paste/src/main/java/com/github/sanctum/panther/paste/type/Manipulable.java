package com.github.sanctum.panther.paste.type;

import com.github.sanctum.panther.paste.operative.PasteReader;
import com.github.sanctum.panther.paste.operative.PasteWriter;
import org.jetbrains.annotations.NotNull;

public interface Manipulable extends PasteWriter, PasteReader {

	@NotNull String getApiKey();

	@NotNull
	PasteOptions getOptions();


}
