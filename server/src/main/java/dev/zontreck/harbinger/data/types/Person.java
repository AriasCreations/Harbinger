package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.List;
import java.util.UUID;

public class Person {
	public UUID ID;
	public String Name;
	public PermissionLevel Permissions;

	public Person(final UUID id, final String user, final PermissionLevel lvl) {
		this.ID = id;
		this.Name = user;
		this.Permissions = lvl;
	}

	public String print(final int indent) {
		String ind = "";
		for (int i = 0; i < indent; i++) {
			ind += "\t";
		}
		String s = "";
		s += ind + "{\n" + ind + "\tID: ";
		s += this.ID.toString() + "\n" + ind + "\tName: ";
		s += this.Name + "\n" + ind + "\tPermissions: ";
		s += this.Permissions.toString() + "\n";
		s += ind + "}";

		return s;
	}

	public Entry<List<Entry>> save() {
		final Entry<List<Entry>> tag = Folder.getNew(this.Name);

		tag.value.add(EntryUtils.mkUUID("id", this.ID));
		tag.value.add(EntryUtils.mkStr("name", this.Name));
		tag.value.add(EntryUtils.mkInt("permissions", this.Permissions.getFlag()));

		return tag;
	}

	public static Person deserialize(final Entry<List<Entry>> tag) {
		try {

			return new Person(
					EntryUtils.getUUID(Folder.getEntry(tag, "id")),
					EntryUtils.getStr(Folder.getEntry(tag, "name")),
					PermissionLevel.of(EntryUtils.getInt(Folder.getEntry(tag, "permissions"))));
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
