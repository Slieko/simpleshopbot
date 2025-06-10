package zxc.slieko.telegrambot.core;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import zxc.slieko.telegrambot.Main;

import java.util.ArrayList;

public class CustomMessages {
    public static void sendMainMenu(long id) {
        KeyboardButton menuButton = KeyboardButton.builder().text("Меню").build();
        KeyboardButton contactsButton = KeyboardButton.builder().text("Контакты").build();
        KeyboardButton catalogueButton = KeyboardButton.builder().text("Каталог").build();
        KeyboardButton reviewsButton = KeyboardButton.builder().text("Отзывы").build();
        KeyboardButton orderButton = KeyboardButton.builder().text("Заказать").build();

        ArrayList<KeyboardRow> buttons = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(menuButton);
        row.add(catalogueButton);
        row.add(orderButton);

        KeyboardRow row1 = new KeyboardRow();
        row1.add(contactsButton);
        row1.add(reviewsButton);

        buttons.add(row);
        buttons.add(row1);

        Main.bot.sendMenuMessage(id, "Меню", buttons);
    }

    public static void sendContactsMessage(long id) {
        ArrayList<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton button = InlineKeyboardButton.builder().url("https://t.me/pulsenxva").text("admin").build();
        buttons.add(button);
        Main.bot.sendInlineKeyboard(id, "Контакты", buttons);
    }

    public static void sendAdminMenu(long id) {
        KeyboardButton menuButton = KeyboardButton.builder().text("Меню").build();
        KeyboardButton addButton = KeyboardButton.builder().text("Добавить товар").build();
        KeyboardButton editButton = KeyboardButton.builder().text("Изменить товары").build();

        ArrayList<KeyboardRow> buttons = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(addButton);
        row.add(menuButton);
        row.add(editButton);
        buttons.add(row);

        Main.bot.sendMenuMessage(id, "Админ-меню", buttons);
    }
}
