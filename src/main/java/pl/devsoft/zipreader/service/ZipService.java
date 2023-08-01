package pl.devsoft.zipreader.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {


    public File filterZipEntries(MultipartFile multipartFile, String searchText) {

        // Tworzymy InputStream z MultipartFile
        try (InputStream is = multipartFile.getInputStream()) {
            // Tworzymy ZipInputStream, aby odczytać pliki z archiwum ZIP
            try (ZipInputStream zipIn = new ZipInputStream(is)) {

                // Tworzymy tymczasowy plik ZIP, w którym będziemy przechowywać pliki spełniające warunek
                File tempZipFile = File.createTempFile("filtered_archive", ".zip");
                try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempZipFile))) {

                    // Iterujemy po wszystkich plikach wewnątrz archiwum ZIP
                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            // Odczytujemy zawartość pliku jako tablicę bajtów
                            byte[] fileData = this.getData(zipIn);

                            if (containsTextInZipEntry(entry, fileData, searchText)) {
                                // Dodajemy ten plik do nowego archiwum ZIP
                                zipOut.putNextEntry(entry);

                                // Zapisujemy zawartość pliku
                                zipOut.write(fileData);

                                // Ustawiamy komentarz dla tego ZipEntry
                                zipOut.setComment("Komentarz dla " + entry.getName());

                                // Zamykamy wpis dla tego pliku
                                zipOut.closeEntry();
                            }
                        }
                    }
                }
                return tempZipFile;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getData(InputStream zipEntryStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = zipEntryStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    private boolean containsTextInZipEntry(ZipEntry zipEntry, byte[] fileData, String searchText) throws IOException {
        // Odczytujemy zawartość pliku ZIP jako tekst
        String fileContent = new String(fileData, "UTF-8");
        return fileContent.contains(searchText);
    }


}
