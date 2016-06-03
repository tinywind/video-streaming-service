package kr.tinywind.springbootstreaming.controller;

import kr.tinywind.springbootstreaming.model.FileNode;
import kr.tinywind.springbootstreaming.model.Material;
import kr.tinywind.springbootstreaming.model.MaterialDataRepository;
import kr.tinywind.springbootstreaming.model.MaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialDataRepository dataRepository;

    private String decodePath(HttpServletRequest request, String prefix) throws UnsupportedEncodingException {
        return URLDecoder.decode(request.getRequestURI().substring(prefix.length()), "UTF-8");
    }

    @RequestMapping(value = {"", "show"})
    public String show() {
        return "show";
    }

    @RequestMapping("list")
    @ResponseBody
    public List<FileNode> listNoSubUri(HttpServletRequest request) throws UnsupportedEncodingException {
        return list(request);
    }

    @RequestMapping("list/**")
    @ResponseBody
    public List<FileNode> list(HttpServletRequest request) throws UnsupportedEncodingException {
        final List<FileNode> list = new ArrayList<>();
        final String filePath = VIDEO_ROOT + decodePath(request, "/list/");
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

    @RequestMapping("video-random-accessible/**")
    public ModelAndView videoRandomAccessible(HttpServletRequest request) throws UnsupportedEncodingException {
        return new ModelAndView("streamView", "movieName", decodePath(request, "/video-random-accessible/"));
    }

    @RequestMapping("video/**")
    public ResponseEntity<InputStreamResource> video(HttpServletRequest request)
            throws UnsupportedEncodingException, FileNotFoundException {
        final String decodeString = decodePath(request, "/video/");
        File file = new File(VIDEO_ROOT + decodeString);
        logger.info("DOWNLOAD " + file.getAbsolutePath());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new FileInputStream(file)));
    }

    @RequestMapping("material-list")
    @ResponseBody
    public List<Material> materialList() {
        List<Material> all = materialRepository.findAll();
        return all;
    }

    @RequestMapping("save")
    @ResponseBody
    public Material save(Material material) {
        final Material saved = materialRepository.saveAndFlush(material);
//        saved.getMaterialDataList().forEach(e -> e.setMaterial(saved));
//        dataRepository.flush();
        return saved;
    }
}