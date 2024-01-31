package com.github.sanctum.panther.paste.type;

import org.jetbrains.annotations.NotNull;

public interface PastebinUser extends Manipulable {

	@NotNull String getId();

	boolean remove(@NotNull String id);


}
