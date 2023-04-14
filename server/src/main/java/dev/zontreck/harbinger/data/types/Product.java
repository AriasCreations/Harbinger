package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class Product {
    public String productName;
    public Version versionNumber;
    public String productItem;
    public Server containingServer;

    public UUID productID;



    public Entry<List<Entry>> save()
    {
        Entry<List<Entry>> tag = Folder.getNew(productName);

        tag.value.add(EntryUtils.mkStr("product", productName));
        tag.value.add(versionNumber.save());
        tag.value.add(EntryUtils.mkStr("item", productItem));
        tag.value.add(EntryUtils.mkUUID("id", productID));

        return tag;
    }

    public static Product deserialize(Entry<List<Entry>> tag)
    {
        try{

            Product p=new Product();
            p.productName = EntryUtils.getStr(Folder.getEntry(tag, "product"));
            p.versionNumber = new Version((Entry<int[]>) Folder.getEntry(tag,"ver"));
            p.productItem = EntryUtils.getStr(Folder.getEntry(tag,"item"));
            p.productID = EntryUtils.getUUID(Folder.getEntry(tag, "id"));


            return p;
        }catch (Exception e)
        {
            return null;
        }
    }

}
