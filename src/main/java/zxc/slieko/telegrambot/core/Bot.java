package zxc.slieko.telegrambot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import zxc.slieko.telegrambot.Constants;
import zxc.slieko.telegrambot.Main;
import zxc.slieko.telegrambot.catalogue.CatalogueService;
import zxc.slieko.telegrambot.catalogue.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static zxc.slieko.telegrambot.core.CustomMessages.*;

public class Bot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private final CatalogueService service = new CatalogueService();

    private String token;
    private int dialogID = Constants.NONE_DIALOG;

    public Bot(String token) {
        this.token = token;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    private final ReplyHandler.ItemEdit itemEditHandler = new ReplyHandler.ItemEdit();
    private final ReplyHandler.ItemDelete itemDeleteHandler = new ReplyHandler.ItemDelete();
    private final ReplyHandler.ItemAdd itemAddHandler = new ReplyHandler.ItemAdd();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            switch (update.getMessage().getText().toLowerCase()) {
                case "/start", "меню": {
                    if (dialogID == Constants.NONE_DIALOG) sendMainMenu(chatId);
                    break;
                }
                case "каталог": {
                    if (dialogID == Constants.NONE_DIALOG) {
                        StringBuilder stringBuilder = new StringBuilder();

                        CatalogueService.list.forEach(product -> {
                            stringBuilder.append(product.getName() + ":\n").append(product.getPrice() + "\n").append(product.getDesc() + "\n\n");
                        });

                        ArrayList<InlineKeyboardButton> buttons = new ArrayList<>();
                        CatalogueService.list.forEach(item -> {
                            InlineKeyboardButton info = InlineKeyboardButton.builder().text(item.getName()).callbackData("getInfo_" + item.getName()).build();
                            buttons.add(info);
                        });
                        sendInlineKeyboard(chatId, stringBuilder.toString(), buttons);
                    }
                    break;
                }
                case "контакты": {
                    if (dialogID == Constants.NONE_DIALOG) sendContactsMessage(chatId);
                    break;
                }
                case "заказать": {
                    if (dialogID == Constants.NONE_DIALOG) {
                        sendMessage(chatId, """
                                Чтобы заказать товар отправь сообщение в форме:
                                
                                1. @username
                                2. Товар
                                
                                Чтобы отменить, ответь словом "назад".""");
                        dialogID = Constants.ORDER_DIALOG;
                    }
                    break;
                }
                case "/admin_panel": {
                    if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                    if (dialogID == Constants.NONE_DIALOG) sendAdminMenu(chatId);
                    break;
                }
                case "добавить товар": {
                    if (dialogID == Constants.NONE_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        sendMessage(chatId, """
                                Чтобы добавить товар пришли сообщение в формате:
                                
                                Название:Цена:Описание
                                
                                Чтобы отменить, напиши слово "назад".""");
                        dialogID = Constants.ADD_DIALOG;
                    }
                    break;
                }
                case "изменить товары": {
                    if (dialogID == Constants.NONE_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        sendMessage(chatId, "Чтобы изменить товар, напишите команду /edit <название товара>");
                        sendAdminMenu(chatId);
                    }
                    break;
                }
                default: {
                    if (update.getMessage().getText().contains("/edit")) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        if (dialogID == Constants.NONE_DIALOG) {
                            String editCommand[] = update.getMessage().getText().split(" ");
                            String itemName = Arrays.stream(editCommand).toList().get(1);
                            Product item = service.getByName(itemName);

                            ArrayList<InlineKeyboardButton> buttons = new ArrayList<>();
                            InlineKeyboardButton editName = InlineKeyboardButton.builder().text("Имя").callbackData("editName_" + item.getName()).build();
                            InlineKeyboardButton editPrice = InlineKeyboardButton.builder().text("Цена").callbackData("editPrice_" + item.getName()).build();
                            InlineKeyboardButton editDesc = InlineKeyboardButton.builder().text("Описание").callbackData("editDesc_" + item.getName()).build();
                            InlineKeyboardButton deleteItem = InlineKeyboardButton.builder().text("Удалить").callbackData("delete_" + item.getName()).build();

                            buttons.add(editName);
                            buttons.add(editPrice);
                            buttons.add(editDesc);
                            buttons.add(deleteItem);
                            sendInlineKeyboard(chatId, "Изменение товара: " + item.getName(), buttons);
                        }
                    }
                    if (dialogID == Constants.ORDER_DIALOG) {
                        if (update.getMessage().getText().toLowerCase().contains("назад")) {
                            sendMessage(chatId, "Заказ отменён");
                            sendMainMenu(chatId);
                            dialogID = Constants.NONE_DIALOG;
                            return;
                        }

                        Main.ADMIN_IDS.forEach(id -> sendMessage(new User(Long.parseLong(id), "", false).getId(), "Новый заказ:\n" + update.getMessage().getText()));

                        sendMessage(chatId, "Спасибо! Мы свяжемся с вами в течение часа.");
                        dialogID = Constants.NONE_DIALOG;
                        sendMainMenu(chatId);
                        break;
                    }
                    if (dialogID == Constants.ADD_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        if (update.getMessage().getText().toLowerCase().contains("назад")) {
                            sendAdminMenu(chatId);
                            return;
                        }
                        List<String> properties = List.of(update.getMessage().getText().split(":"));
                        itemAddHandler.setItemName(properties.get(0));
                        itemAddHandler.setItemPrice(Integer.parseInt(properties.get(1)));
                        itemAddHandler.setItemDesc(properties.get(2));

                        try {
                            Product product = new Product(itemAddHandler.getItemName(), itemAddHandler.getItemPrice(), itemAddHandler.getItemDesc());
                            CatalogueService.list.add(product);
                            sendMessage(chatId, "Новый товар был успешно добавлен!");
                            service.save();
                            sendAdminMenu(chatId);
                            itemAddHandler.reset();
                        } catch (Exception e) {
                            sendMessage(chatId, e.getMessage());
                        }
                        dialogID = Constants.NONE_DIALOG;
                        break;
                    }
                    if (dialogID == Constants.DELETE_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        if (update.getMessage().getText().toLowerCase().contains("назад")) {
                            sendAdminMenu(chatId);
                            dialogID = Constants.NONE_DIALOG;
                            return;
                        } else if (update.getMessage().getText().equalsIgnoreCase("да"))
                            try {
                                CatalogueService.list.remove(service.getByName(itemDeleteHandler.getName()));
                                service.save();
                                itemDeleteHandler.reset();
                                dialogID = Constants.NONE_DIALOG;
                                sendMessage(chatId, "Товар был успешно удалён");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                    }
                    if(dialogID == Constants.EDIT_PRICE_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        if (update.getMessage().getText().toLowerCase().contains("назад")) {
                            sendAdminMenu(chatId);
                            dialogID = Constants.NONE_DIALOG;
                            return;
                        }

                        try {
                            itemEditHandler.setNew(Integer.parseInt(update.getMessage().getText()));
                            Product item = service.getByName(itemEditHandler.getItemName());
                            item.setPrice((Integer) itemEditHandler.getNew());
                            sendMessage(chatId, "Цена товара успешно изменено на " + itemEditHandler.getNew());
                            service.save();
                            sendAdminMenu(chatId);
                            itemEditHandler.reset();
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "Укажи только число!");
                            sendAdminMenu(chatId);
                            itemEditHandler.reset();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        dialogID = Constants.NONE_DIALOG;
                    }
                    if (dialogID == Constants.EDIT_NAME_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        if (update.getMessage().getText().toLowerCase().contains("назад")) {
                            sendAdminMenu(chatId);
                            dialogID = Constants.NONE_DIALOG;
                            return;
                        }
                        itemEditHandler.setNew(update.getMessage().getText());
                        try {
                            File image = new File(service.images + "/" + itemEditHandler.getItemName() + ".png");
                            if (image.exists())
                                image.renameTo(new File(service.images + "/" + itemEditHandler.getNew() + ".png"));
                            Product item = service.getByName(itemEditHandler.getItemName());
                            item.setName((String) itemEditHandler.getNew());
                            sendMessage(chatId, "Название товара успешно изменено на " + itemEditHandler.getItemName());
                            service.save();
                            sendAdminMenu(chatId);
                            itemEditHandler.reset();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        dialogID = Constants.NONE_DIALOG;
                    }
                    if (dialogID == Constants.EDIT_DESC_DIALOG) {
                        if (!Main.ADMIN_IDS.contains(update.getMessage().getFrom().getId().toString())) return;
                        if (update.getMessage().getText().toLowerCase().contains("назад")) {
                            sendAdminMenu(chatId);
                            dialogID = Constants.NONE_DIALOG;
                            return;
                        }
                        itemEditHandler.setNew(update.getMessage().getText());
                        try {
                            Product item = service.getByName(itemEditHandler.getItemName());
                            item.setDesc((String) itemEditHandler.getNew());
                            sendMessage(chatId, "Описание товара успешно изменено на " + itemEditHandler.getItemName());
                            service.save();
                            sendAdminMenu(chatId);
                            itemEditHandler.reset();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        dialogID = Constants.NONE_DIALOG;
                    }
                    break;
                }
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.contains("editName")) {
                if (dialogID == Constants.NONE_DIALOG) {
                    String itemName = Arrays.stream(data.split("_")).toList().get(1);
                    sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Напиши сообщение с новым именем товара либо \"назад\" чтобы вернуться назад");
                    itemEditHandler.setItemName(itemName);
                    dialogID = Constants.EDIT_NAME_DIALOG;
                }
            }
            if (data.contains("editDesc")) {
                if (dialogID == Constants.NONE_DIALOG) {
                    String itemName = Arrays.stream(data.split("_")).toList().get(1);
                    sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Напиши сообщение с новым описанием товара либо \"назад\" чтобы вернуться назад");
                    itemEditHandler.setItemName(itemName);
                    dialogID = Constants.EDIT_DESC_DIALOG;
                }
            }
            if (data.contains("editPrice")) {
                if (dialogID == Constants.NONE_DIALOG) {
                    String itemName = Arrays.stream(data.split("_")).toList().get(1);
                    sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Напиши сообщение с новой ценой товара либо \"назад\" чтобы вернуться назад");
                    itemEditHandler.setItemName(itemName);
                    dialogID = Constants.EDIT_PRICE_DIALOG;
                }
            }
            if (data.contains("delete")) {
                if (dialogID == Constants.NONE_DIALOG) {
                    String itemName = Arrays.stream(data.split("_")).toList().get(1);
                    sendMessage(update.getCallbackQuery().getMessage().getChatId(), "Если хочешь удалить товар напиши Да либо \"назад\" чтобы вернуться назад");
                    itemDeleteHandler.setName(itemName);
                    dialogID = Constants.DELETE_DIALOG;
                }
            }
            if (data.contains("getInfo")) {
                String itemName = Arrays.stream(data.split("_")).toList().get(1);
                Product item = service.getByName(itemName);
                String info = String.format("""
                        Название: %1$s
                        Цена: %2$d
                        Описание: %3$s
                        """, item.getName(), item.getPrice(), item.getDesc());

                try {
                    File file = new File(service.images+"/"+ item.getName()+".png");
                    if(file.exists()) {
                        SendPhoto sendPhoto = SendPhoto.builder()
                                .chatId(update.getCallbackQuery().getMessage().getChatId())
                                .photo(new InputFile(file))
                                .caption(info)
                                .build();
                        execute(sendPhoto);
                    }
                    else sendMessage(update.getCallbackQuery().getMessage().getChatId(), info);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return "ShopBot";
    }

    public void sendMessage(long id, String s) {
        try {
            SendMessage sendMessage = new SendMessage(String.valueOf(id), s);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendInlineKeyboard(long id, String s, List<InlineKeyboardButton> buttons) {
        SendMessage sendMessage = new SendMessage(String.valueOf(id), s);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(buttons);
        inlineKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMenuMessage(long id, String s, ArrayList<KeyboardRow> buttons) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(buttons);
        markup.setResizeKeyboard(true);
        SendMessage message = SendMessage.builder().chatId(id).text(s).replyMarkup(markup).build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
