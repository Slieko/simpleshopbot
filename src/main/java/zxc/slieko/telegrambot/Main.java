package zxc.slieko.telegrambot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import zxc.slieko.telegrambot.catalogue.CatalogueService;
import zxc.slieko.telegrambot.core.Bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static ArrayList<String> ADMIN_IDS = new ArrayList<>();
    public static Bot bot;

    public static void main(String[] args) {
        try{
            CatalogueService catalogueService = new CatalogueService();
            catalogueService.load();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            List<String> argsList = List.of(args);
            if(argsList.get(0).isEmpty()) {
                bot = new Bot("picun");
                telegramBotsApi.registerBot(bot);
            }
            else {
                bot = new Bot(argsList.get(0));
                telegramBotsApi.registerBot(bot);
            }
            if(!argsList.get(1).isEmpty()) {
                ADMIN_IDS.addAll(Arrays.asList(argsList.get(1).split(";")));
            }

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}