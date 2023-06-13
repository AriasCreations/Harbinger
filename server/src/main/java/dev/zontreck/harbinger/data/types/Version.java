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

	public Version(final String ver) {
		final String[] str = ver.split("\\.");
		for (final String string : str) {
			this.versionDigits.add(Integer.parseInt(string));
		}
	}

	public Version(final Entry<int[]> tag) {
		this.versionDigits = Lists.newArrayList();
		for (final Integer integer : tag.value) {
			this.versionDigits.add(integer);
		}
	}

	public Entry<int[]> save() {

		final int[] arr = new int[this.versionDigits.size()];

		for (int i = 0; i < this.versionDigits.size(); i++) {
			final int integer = this.versionDigits.get(i);
			arr[i] = integer;
		}

		return new Entry<int[]>(arr, "version");
	}

	@Override
	public String toString() {
		String ret = "";

		final Iterator<Integer> it = this.versionDigits.iterator();
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
	public boolean increment(final int position) {
		if (this.versionDigits.size() <= position) return false;
		Integer it = this.versionDigits.get(position);
		it++;
		this.versionDigits.set(position, it);

		return true;
	}


	public boolean isNewer(final Version other) throws Exception {
		if (other.versionDigits.size() != this.versionDigits.size()) {
			Version.LOGGER.error("Cannot proceed, the versions do not have the same number of digits");
			return false;

		} else {
			for (int i = 0; i < this.versionDigits.size(); i++) {
				final int v1 = this.versionDigits.get(i);
				final int v2 = other.versionDigits.get(i);

				if (v1 < v2) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean isSame(final Version other) {
		if (other.versionDigits.size() != this.versionDigits.size())
			return false;

		for (int i = 0; i < this.versionDigits.size(); i++) {
			final int v1 = this.versionDigits.get(i);
			final int v2 = other.versionDigits.get(i);

			if (v1 != v2) return false;
		}

		return true;
	}
}
