package dev.zontreck.harbinger.data.types;

import com.google.common.collect.Lists;
import dev.zontreck.ariaslib.file.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class Version {
	public List<Integer> versionDigits = Lists.newArrayList();

	public static final Logger LOGGER = LoggerFactory.getLogger(Version.class.getName());

	public Version(String ver) {
		String[] str = ver.split("\\.");
		for (String string : str) {
			versionDigits.add(Integer.parseInt(string));
		}
	}

	public Version(Entry<int[]> tag) {
		versionDigits = Lists.newArrayList();
		for (Integer integer : tag.value) {
			versionDigits.add(integer);
		}
	}

	public Entry<int[]> save() {

		int[] arr = new int[versionDigits.size()];

		for (int i = 0; i < versionDigits.size(); i++) {
			int integer = versionDigits.get(i);
			arr[i] = integer;
		}

		return new Entry<int[]>(arr, "version");
	}

	@Override
	public String toString() {
		String ret = "";

		Iterator<Integer> it = versionDigits.iterator();
		while (it.hasNext()) {
			ret += it.next();

			if (it.hasNext()) {
				ret += ".";
			}
		}

		return ret;
	}

	/**
	 * Increments a version number
	 *
	 * @param position
	 * @return True if the number was incremented
	 */
	public boolean increment(int position) {
		if (versionDigits.size() <= position) return false;
		Integer it = versionDigits.get(position);
		it++;
		versionDigits.set(position, it);

		return true;
	}


	public boolean isNewer(Version other) throws Exception {
		if (other.versionDigits.size() != versionDigits.size()) {
			LOGGER.error("Cannot proceed, the versions do not have the same number of digits");
			return false;

		} else {
			for (int i = 0; i < versionDigits.size(); i++) {
				int v1 = versionDigits.get(i);
				int v2 = other.versionDigits.get(i);

				if (v1 < v2) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean isSame(Version other) {
		if (other.versionDigits.size() != versionDigits.size())
			return false;

		for (int i = 0; i < versionDigits.size(); i++) {
			int v1 = versionDigits.get(i);
			int v2 = other.versionDigits.get(i);

			if (v1 != v2) return false;
		}

		return true;
	}
}
