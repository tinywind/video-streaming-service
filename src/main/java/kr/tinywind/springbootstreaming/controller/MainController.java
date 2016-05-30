package kr.tinywind.springbootstreaming.controller;

import kr.tinywind.springbootstreaming.model.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final String FOLDER_PATH = "D:\\Music\\Music Video\\Mike Tompkins\\";

    @RequestMapping(value = {"", "list"})
    @ResponseBody
    public List<FileItem> list(@RequestParam(required = false) String path) {
        final List<FileItem> list = new ArrayList<>();
        final String filePath = FOLDER_PATH + (path == null ? "" : "" + path);
        try {
            final File directory = new File(filePath);
            final File[] files = directory.listFiles();
            for (File file : files)
                list.add(new FileItem(file.getName(), file.getName().contains(".mp4")));
        } catch (Exception e) {
            logger.error("ERROR DURING FOR LOOP");
        }
        logger.info("File List Request. List size is " + list.size());
        return list;
    }

    @RequestMapping("download/{file}")
    public ResponseEntity<InputStreamResource> download(@PathVariable("file") String file, HttpServletRequest request)
            throws UnsupportedEncodingException, FileNotFoundException {
        final String decodeString = URLDecoder.decode(file, "UTF-8");
        File f = new File(FOLDER_PATH + decodeString);
        if (!f.exists())
            f = new File(FOLDER_PATH + request.getRequestURI().substring("/download/".length()));
        if (!f.exists())
            f = new File(FOLDER_PATH + file + ".mp4");
        logger.info("DOWNLOAD " + f.getAbsolutePath());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-disposition", "attachment; filename=" + decodeString);
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(f)));
    }
}