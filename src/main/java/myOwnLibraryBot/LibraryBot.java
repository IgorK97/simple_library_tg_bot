package myOwnLibraryBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
//import org.telegram.telegrambots.meta.api.objects.File;
import java.io.File;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.*;

public class LibraryBot extends TelegramLongPollingBot {

    private final Map<Long, List<FileRepository.FileEntry>> userSearchResults = new HashMap<>();
    private List<FileRepository.FileEntry> files = new ArrayList<>();
    private FileRepository fRepo = new FileRepository();
    @Override
    public String getBotUsername() {
        return BotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        String token = BotConfig.getBotToken();
        return token;
    }

    @Override
    public void onUpdateReceived(Update update){
        try{
            if(update.hasMessage() && update.getMessage().hasText()){
                String query = update.getMessage().getText().trim();
                Long chatId = update.getMessage().getChatId();

                files = searchFiles(query);
                List<FileRepository.FileEntry> result = files;
                userSearchResults.put(chatId, result);

                if(result.isEmpty()){
                    execute(new SendMessage(chatId.toString(), "Nothing is found"));

                }
                else{
                    sendPage(chatId, result, 0);
                }
            }
            if(update.hasCallbackQuery()){
                String data = update.getCallbackQuery().getData();
                Long chatId = update.getCallbackQuery().getMessage().getChatId();

                if(data.startsWith("page:")){
                    int page = Integer.parseInt(data.split(":")[1]);
                    sendPage(chatId, userSearchResults.get(chatId), page);
                }

                else if(data.startsWith("file:")){
                    int index = Integer.parseInt(data.split(":")[1]);
                    FileRepository.FileEntry file = userSearchResults.get(chatId).get(index);
                    sendFilePreview(chatId, file);
                }

                else if (data.startsWith("download:")){
                    int index = Integer.parseInt(data.split(":")[1]);
                    FileRepository.FileEntry file = userSearchResults.get(chatId).get(index);
                    sendFile(chatId, file);
                }
            }

        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private List<FileRepository.FileEntry> searchFiles(String query) throws SQLException {
//        List<FileRepository.FileEntry> result = new ArrayList<>();
//        for(FileRepository.FileEntry f:files){
//            if(f.title.toLowerCase().contains(query.toLowerCase())){
//                result.add(f);
//            }
//        }
        return fRepo.searchFiles(query);

    }

    private void sendPage(Long chatId, List<FileRepository.FileEntry> list, int page) throws TelegramApiException {
        int pageSize = 1;
        int totalPages = (int) Math.ceil((double)list.size()/pageSize);

        int start = page*pageSize;
        int end = Math.min(start + pageSize, list.size());

        StringBuilder sb = new StringBuilder("Results: (page "+(page+1) + "/" + totalPages +"):\n");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>>rows = new ArrayList<>();

        for(int i=start;i<end;i++){
            InlineKeyboardButton btn = new InlineKeyboardButton(list.get(i).title);
            btn.setCallbackData("file:"+i);
            rows.add(Collections.singletonList(btn));
        }

        List<InlineKeyboardButton> nav = new ArrayList<>();
        if(page>0){
            InlineKeyboardButton prev = new InlineKeyboardButton("\u2B05 Back");
            prev.setCallbackData("page:"+(page-1));
            nav.add(prev);
        }
        if(page<totalPages-1){
            InlineKeyboardButton next = new InlineKeyboardButton("\u27A1 Next");
            next.setCallbackData("page:"+(page+1));
            nav.add(next);
        }
        if(!nav.isEmpty()) rows.add(nav);

        markup.setKeyboard(rows);
        SendMessage msg = new SendMessage(chatId.toString(), sb.toString());
        msg.setReplyMarkup(markup);
        execute(msg);
    }

    private void sendFilePreview(Long chatId, FileRepository.FileEntry file) throws TelegramApiException{
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(new InputFile(new File(file.coverPath)));
        photo.setCaption("File: "+file.title);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton btn = new InlineKeyboardButton("\uD83D\uDCE5 Download");
        btn.setCallbackData("download:"+files.indexOf(file));

        markup.setKeyboard(Collections.singletonList(Collections.singletonList(btn)));
        photo.setReplyMarkup(markup);
        execute(photo);
    }

    private void sendFile(Long chatId, FileRepository.FileEntry file) throws TelegramApiException{
        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setDocument(new InputFile(new File(file.filePath)));
        execute(doc);
    }

    private void sendText(Long chatId, String text) throws TelegramApiException{
        execute(new SendMessage(chatId.toString(), text));
    }
}
