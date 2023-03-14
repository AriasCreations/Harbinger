package dev.zontreck.harbinger.data.types;

import java.util.UUID;

import dev.zontreck.ariaslib.nbt.CompoundTag;

public class Person {
    public UUID ID;
    public String Name;
    public PermissionLevel Permissions;

    public Person(UUID id, String user, PermissionLevel lvl)
    {
        ID=id;
        Name=user;
        Permissions=lvl;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", ID.toString());
        tag.putString("name", Name);
        tag.putInt("permissions", Permissions.getFlag());

        return tag;
    }

    public static Person deserialize(CompoundTag tag)
    {
        return new Person(UUID.fromString(tag.getString("id")), tag.getString("name"), PermissionLevel.of(tag.getInt("permissions")));
    }
}
