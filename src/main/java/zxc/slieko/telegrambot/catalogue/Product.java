package zxc.slieko.telegrambot.catalogue;


public class Product {
    private String name;
    private int price;
    private String desc;

    public Product(String name, int price, String desc) {
        this.name = name;
        this.price = price;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "name: "+name +
                "; price: "+price+
                "; desc: "+desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
