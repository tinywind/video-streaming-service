package kr.tinywind.springbootstreaming.controller;

import kr.tinywind.springbootstreaming.model.FileNode;
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
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static kr.tinywind.springbootstreaming.config.Constants.VIDEO_ROOT;

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @RequestMapping(value = {"", "show"})
    public String show() {
        return "show";
    }

    @RequestMapping("list")
    @ResponseBody
    public List<FileNode> list(@RequestParam(required = false) String path) {
        final List<FileNode> list = new ArrayList<>();
        final String filePath = VIDEO_ROOT + (path == null ? "" : "" + path);
        try {
            final File directory = new File(filePath);
            final File[] files = directory.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory())
                    list.add(new FileNode(fileName, false));
                else if (fileName.toLowerCase().matches("^.*\\.mp4$"))
                    list.add(new FileNode(fileName, true));
            }
        } catch (Exception e) {
            logger.error("ERROR DURING FOR LOOP: " + e.getMessage());
            if (e.getCause() != null)
                logger.error("ERROR DURING FOR LOOP: " + e.getCause().getMessage());
        }
        logger.info("File List Request. List size is " + list.size());
        return list;
    }

    @RequestMapping("video2/{path}/**")
    public ModelAndView video2(@PathVariable("path") String path) {
        return new ModelAndView("streamView", "movieName", path);
    }

    @RequestMapping("video/{path}/**")
    public ResponseEntity<InputStreamResource> video(@PathVariable("path") String path)
            throws UnsupportedEncodingException, FileNotFoundException {
        final String decodeString = URLDecoder.decode(path, "UTF-8");
        File file = new File(VIDEO_ROOT + decodeString);
        logger.info("DOWNLOAD " + file.getAbsolutePath());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(file)));
    }
}