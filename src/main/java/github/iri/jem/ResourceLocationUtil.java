package github.iri.jem;

import net.minecraft.resources.*;

public class ResourceLocationUtil {
	public static String sanitizePath(String path) {
		char[] charArray = path.toCharArray();
		boolean valid = true;
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (!ResourceLocation.validPathChar(c)) {
				charArray[i] = '.';
				valid = false;
			}
		}
		if (valid) {
			return path;
		}
		return new String(charArray);
	}
}