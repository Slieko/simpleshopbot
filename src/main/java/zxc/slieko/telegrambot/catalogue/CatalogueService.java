package zxc.slieko.telegrambot.catalogue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class CatalogueService {
    private final Gson gson = new Gson();
    public static ArrayList<Product> list = new ArrayList<>();
    private final File products = new File(System.getProperty("user.home")+File.separator+"catalogue.json");
    public final File images = new File(System.getProperty("user.home")+File.separator+"images");

    public void load() throws Exception {
        if(!images.exists()) images.mkdir();
        if(!products.exists()) {
            products.createNewFile();
            save();
        }
        FileReader reader = new FileReader(products);
        JsonArray array = gson.fromJson(reader, JsonArray.class);
        array.forEach((jsonElement -> {
            list.add(new Product(jsonElement.getAsJsonObject().get("name").getAsString(), jsonElement.getAsJsonObject().get("price").getAsInt(), jsonElement.getAsJsonObject().get("desc").getAsString()));
        }));
        reader.close();
    }

    public void save() throws Exception {
        FileWriter writer = new FileWriter(products);
        JsonArray array = new JsonArray();
        list.forEach(product -> {
            JsonObject object = new JsonObject();
            object.addProperty("name", product.getName());
            object.addProperty("price", product.getPrice());
            object.addProperty("desc", product.getDesc());

            array.add(object);
        });
        gson.toJson(array, writer);
        writer.close();
    }

    public Product getByName(String name) {
        for (Product product : list) {
            if(product.getName().equals(name)) return product;
        }
        return null;
    }
}
