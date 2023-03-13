package dev.zontreck.harbinger.data.containers;

import java.util.ArrayList;
import java.util.List;

import dev.zontreck.ariaslib.nbt.CompoundTag;
import dev.zontreck.ariaslib.nbt.ListTag;
import dev.zontreck.harbinger.data.types.Product;

public class Products {
    public List<Product> products = new ArrayList<>();

    public static Products deserialize(CompoundTag tag)
    {
        Products prods = new Products();
        ListTag lst = tag.getList("products");
        for (int i = 0; i < lst.size(); i++) {
            Product prod = Product.deserialize((CompoundTag)lst.get(i));

            prods.products.add(prod);
        }

        return prods;
    }


    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        ListTag lst = new ListTag();

        for (Product product : products) {
            lst.add(product.save());
        }
        tag.put("products", lst);

        return tag;
    }
}
