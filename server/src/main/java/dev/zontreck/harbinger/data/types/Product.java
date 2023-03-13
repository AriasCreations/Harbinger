package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.nbt.CompoundTag;

public class Product {
    public String productName;
    public Version versionNumber;
    public String productItem;
    public Server containingServer;


    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("product", productName);
        tag.put("ver", versionNumber.save());
        tag.putString("item", productItem);
        return tag;
    }

    public static Product deserialize(CompoundTag tag)
    {
        Product p=new Product();
        p.productName = tag.getString("product");
        p.versionNumber = new Version(tag.getIntArray("ver"));
        p.productItem = tag.getString("item");

        return p;
    }
}
