package dev.zontreck.harbinger.data.types;

public enum PermissionLevel {
    NONE(0),
    Customer(1),
    Support(2),
    Mod(4),
    Developer(8),
    Admin(16);

    private int flag;
    public int getFlag(){
        return flag;
    }
    PermissionLevel(int value)
    {
        flag=value;
    }

    public static PermissionLevel of(int flags)
    {
        PermissionLevel p = PermissionLevel.NONE;

        for (PermissionLevel lvl :
                values()) {
            if(lvl.flag == flags)
            {
                return lvl;
            }
        }

        return p;
    }
}
