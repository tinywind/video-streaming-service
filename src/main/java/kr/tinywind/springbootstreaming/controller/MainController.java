package kr.tinywind.springbootstreaming.controller;

import kr.tinywind.springbootstreaming.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
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
        return URLDecoder.decode(request.getRequestURI().replaceAll("[/]+", "/").substring(prefix.length()), "UTF-8");
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

    @RequestMapping("save")
    public String save(Material material) {
        if (!StringUtils.isEmpty(material.getVideoName()))
//            if (material.getMaterialDataList().size() > 0)
            materialRepository.saveAndFlush(material);
        return "redirect:/material-list";
    }

    @RequestMapping("material-list")
    public String materialList(Model model, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pagingOption) {
        model.addAttribute("postPage", materialRepository.findAll(pagingOption));
        return "material-list";
    }

    @RequestMapping("download-csv/{id}")
    public void downloadCSV(HttpServletResponse response, @PathVariable("id") Long id) throws IOException {
        final Material material = materialRepository.findOne(id);
        if (material == null)
            throw new IllegalArgumentException("[" + id + "] data is not exist.");

        String videoName = material.getVideoName();
        int lastIndexOf = videoName.lastIndexOf("/");
        videoName = lastIndexOf >= 0 ? videoName.substring(lastIndexOf + 1, videoName.length()) : videoName;
        final String csvFileName = videoName + "_" + (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(material.getCreatedAt())) + ".csv";

        response.setContentType("text/csv");

        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
        response.setHeader(headerKey, headerValue);

        final ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        final String[] header = {"timestamp", "key"};
        csvWriter.writeHeader(header);

        for (MaterialData data : material.getMaterialDataList())
            csvWriter.write(data, header);

        csvWriter.close();
    }
}