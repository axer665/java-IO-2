import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) {
        String filePath = "D://Games/savegames";
        File logFile = new File(filePath);
        GameProgress save1 = new GameProgress(94, 10, 2, 254.32);
        GameProgress save2 = new GameProgress(67, 6, 4, 455.48);
        GameProgress save3 = new GameProgress(99, 9, 6, 612.03);

        List<String> saveList = new ArrayList<>();

        if (logFile.exists()){
            System.out.println("Каталог для сохранений найден");

            // Формируем список сохранений
            saveList.add(saveGame(filePath, "save1", save1));
            saveList.add(saveGame(filePath, "save2", save2));
            saveList.add(saveGame(filePath, "save3", save3));

            // Почистим список от null, т.к. saveGame может вернуть null
            while (saveList.remove(null)){}

            // Архивируем сохранения
            zipFiles(filePath, saveList);

            // Очищаем каталог с сохранениями от лишних файлов
            clearSaves(filePath);

            // Разархивируем сохраненные файлы
            openZip(filePath+"/zip_saves.zip", filePath);

            // Десериализуем сохранение 2
            GameProgress load2 = openProgress(filePath+"/save2.dat");
            System.out.println(load2);

        } else {
            System.out.println("Отсутствует каталог для сохранений");
        }
    }

    private static String saveGame(String filePath, String fileName, GameProgress gameProgress) {
        try (FileOutputStream fos = new FileOutputStream(filePath+"/"+fileName+".dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(gameProgress);
            return fileName+".dat";
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static void openZip(String archivePath, String exportCatalogPath) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(archivePath))) {
            ZipEntry entry;
            String name;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                // получим название файла
                // распаковка
                FileOutputStream fout = new FileOutputStream(exportCatalogPath+"/"+name);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void zipFiles(String archivePath, List<String> saveFiles) {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(archivePath+"/zip_saves.zip"));
             ) {
            for (String saveFile: saveFiles) {
                try (FileInputStream fis = new FileInputStream(archivePath + "/" + saveFile)) {
                    ZipEntry entry = new ZipEntry(saveFile);
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    zout.write(buffer);
                    zout.closeEntry();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void clearSaves(String directoryPath) {
        File dir = new File(directoryPath);
        for (File item : dir.listFiles()) {
            if (!item.getName().endsWith(".zip")){
                item.delete();
            }
        }
    }

    private static GameProgress openProgress(String filePath) {
        GameProgress gameProgress = null;
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            gameProgress = (GameProgress) ois.readObject();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return gameProgress;
    }
}