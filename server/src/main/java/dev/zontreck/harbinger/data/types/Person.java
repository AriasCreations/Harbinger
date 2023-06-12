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

	public Person(UUID id, String user, PermissionLevel lvl) {
		ID = id;
		Name = user;
		Permissions = lvl;
	}

	public String print(int indent) {
		String ind = "";
		for (int i = 0; i < indent; i++) {
			ind += "\t";
		}
		String s = "";
		s += ind + "{\n" + ind + "\tID: ";
		s += ID.toString() + "\n" + ind + "\tName: ";
		s += Name + "\n" + ind + "\tPermissions: ";
		s += Permissions.toString() + "\n";
		s += ind + "}";

		return s;
	}

	public Entry<List<Entry>> save() {
		Entry<List<Entry>> tag = Folder.getNew(Name);

		tag.value.add(EntryUtils.mkUUID("id", ID));
		tag.value.add(EntryUtils.mkStr("name", Name));
		tag.value.add(EntryUtils.mkInt("permissions", Permissions.getFlag()));

		return tag;
	}

	public static Person deserialize(Entry<List<Entry>> tag) {
		try {

			return new Person(
					EntryUtils.getUUID(Folder.getEntry(tag, "id")),
					EntryUtils.getStr(Folder.getEntry(tag, "name")),
					PermissionLevel.of(EntryUtils.getInt(Folder.getEntry(tag, "permissions"))));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
