package zxc.slieko.telegrambot.core;

public class ReplyHandler {

    public static class ItemEdit {
        private String itemName;
        private Object newValue;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public Object getNew() {
            return newValue;
        }

        public void setNew(Object newValue) {
            this.newValue = newValue;
        }

        public ItemEdit(String itemName, Object newValue) {
            this.itemName = itemName;
            this.newValue = newValue;
        }
        public ItemEdit() {
            this.itemName = "";
            this.newValue = "";
        }

        public void reset() {
            itemName = "";
            newValue = "";
        }
    }
    public static class ItemDelete {
        private String name;

        public ItemDelete(String name) {
            this.name = name;
        }

        public ItemDelete() {
            this.name = "";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void reset() {
            name = "";
        }
    }
    public static class ItemAdd {
        private String itemName, itemDesc;
        private int itemPrice;

        public String getItemDesc() {
            return itemDesc;
        }

        public void setItemDesc(String itemDesc) {
            this.itemDesc = itemDesc;
        }

        public int getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(int itemPrice) {
            this.itemPrice = itemPrice;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public ItemAdd(String itemName, int itemPrice, String itemDesc) {
            this.itemName = itemName;
            this.itemPrice = itemPrice;
            this.itemDesc = itemDesc;
        }
        public ItemAdd() {
            this.itemName = "";
            this.itemPrice = 0;
            this.itemDesc = "";
        }

        public void reset() {
            itemName = "";
            itemPrice = 0;
            itemDesc = "";
        }
    }
}
