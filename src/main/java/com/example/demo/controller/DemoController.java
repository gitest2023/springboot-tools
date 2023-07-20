package com.example.demo.controller;

import io.woo.htmltopdf.HtmlToPdf;
import io.woo.htmltopdf.HtmlToPdfObject;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "api")
@RestController
public class DemoController {
    @PostMapping(value = "ocr", produces = "application/json")
    public ResponseEntity ocr(@RequestPart(required = false) MultipartFile file) {
        System.out.println("DemoController - ocr - Start");
        String textOCR = "";
        try {
            // Convert to MultipartFile to File
            File convertFile = new File(file.getOriginalFilename());
            System.out.println("tesseract => " + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }

            System.out.println("jna.library.path => " + System.getProperty("jna.library.path"));
            System.out.println("getTesseractLibName => " + LoadLibs.getTesseractLibName());
            System.out.println("TESS4J_TEMP_DIR => " + LoadLibs.TESS4J_TEMP_DIR);
            System.out.println("getTessAPIInstance => " + LoadLibs.getTessAPIInstance());

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("src/main/resources/tessdata");
            tesseract.setLanguage("vie");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);
            textOCR = tesseract.doOCR(convertFile);
            convertFile.delete();
        } catch (Exception e) {
            System.out.println("DemoController - error: " + e);
        }
        System.out.println("OCRController - ocr - End");
        return new ResponseEntity(textOCR, HttpStatus.OK);
    }

    @PostMapping(value = "html2pdf", produces = "application/json")
    public ResponseEntity html2pdf(@RequestBody Map<String, Object> params) {
        System.out.println("DemoController - html2pdf - Start");
        String typeDoc = (String) params.get("typeDoc");
        var content = params.get("content");
        boolean success = false;
        String filePath = "test.pdf";
        try {
            switch (typeDoc) {
                case "url":
                    content = content != null ? content : "https://github.com/wooio/htmltopdf-java";
                    System.out.println("DemoController - html2pdf => converting from url: " + content);
                    success = HtmlToPdf.create()
                            .object(HtmlToPdfObject.forUrl((String) content))
                            .convert(filePath);
                    break;
                case "multiple":
                    HtmlToPdf htmlToPdf = HtmlToPdf.create();
                    System.out.println("DemoController - html2pdf => converting from multiple sources ");
                    if (content instanceof List<?>) {
                        List<?> contentList = (List<?>) content;
                        for (int i=0; i < contentList.size(); i++) {
                            Object item = contentList.get(i);
                            if (item instanceof Map) {
                                Map<String, String> itemMap = (Map<String, String>) item;
                                if (itemMap.get("type").equals("url")) {
                                    htmlToPdf = htmlToPdf.object(HtmlToPdfObject.forUrl(itemMap.get("value")));
                                } else if (itemMap.get("type").equals("html")) {
                                    htmlToPdf = htmlToPdf.object(HtmlToPdfObject.forHtml(itemMap.get("value")));
                                }
                            }
                        }
                        success = htmlToPdf.convert(filePath);
                    }
                    break;
                default:
                    content = content != null ? content : "<p><em>Apples</em>, not oranges</p>";
                    System.out.println("DemoController - html2pdf => converting from html string: " + content);
                    success = HtmlToPdf.create()
                            .object(HtmlToPdfObject.forHtml((String) content))
                            .convert(filePath);
            }


        } catch (Exception e) {
            System.out.println("DemoController - error: " + e);
        }
        System.out.println("OCRController - html2pdf - End");
        return new ResponseEntity(success, HttpStatus.OK);
    }
}
