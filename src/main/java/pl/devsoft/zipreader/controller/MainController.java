package pl.devsoft.zipreader.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import pl.devsoft.zipreader.dto.FormData;
import pl.devsoft.zipreader.service.ZipService;

import java.io.File;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ZipService zipService;

    @GetMapping("/")
    public String welcomePage(Model model) {
        model.addAttribute("formData", new FormData());
        return "welcome";
    }

    @PostMapping("/submitForm")
    public ResponseEntity<Resource> handleFormSubmission(FormData formData) {
        final var name = formData.getFileInput().getName();
        File tempZipFile = zipService.filterZipEntries(formData.getFileInput(), formData.getSearchText());

        // Utwórz Resource dla pliku tymczasowego
        Resource resource = new FileSystemResource(tempZipFile);

        // Ustaw odpowiednie nagłówki dla odpowiedzi
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"result-archive.zip\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

        // Zwróć odpowiedź zawierającą plik ZIP
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(tempZipFile.length())
                .body(resource);
    }
}
