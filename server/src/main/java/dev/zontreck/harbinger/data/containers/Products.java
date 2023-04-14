package dev.zontreck.harbinger.data.containers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.types.Product;

public class Products {
    public List<Product> products = new ArrayList<>();


    public Entry<List<Entry>> write(){
        Entry<List<Entry>> tag = Folder.getNew("products");
        for (Product prod :
                products) {
            tag.value.add(prod.save());
        }
        return tag;

    }

    public boolean hasProduct(UUID prodID)
    {
        if(products.stream().filter(v->v.productID.equals(prodID)).collect(Collectors.toList()).size()>0)
        {
            return true;
        }else return false;
    }

    public Products()
    {

    }
    public Products(Entry<List<Entry>> tag)
    {
        try{

            for(Entry<?> E : tag.value)
            {
                products.add(Product.deserialize((Entry<List<Entry>>)E));
            }
        }catch ( Exception e)
        {
            products = new ArrayList<>();
        }
    }
}
