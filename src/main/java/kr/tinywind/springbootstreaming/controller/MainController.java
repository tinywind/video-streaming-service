package kr.tinywind.springbootstreaming.controller;

import kr.tinywind.springbootstreaming.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static kr.tinywind.springbootstreaming.config.Constants.VIDEO_ROOT;

@Controller
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialDataRepository dataRepository;

    private String decodePath(HttpServletRequest request, String prefix) {
        String path = request.getRequestURI().substring(request.getContextPath().length() + prefix.length());
        while (path.startsWith("/")) path = path.substring(1);
        return UriUtils.decode(path, StandardCharsets.UTF_8);
    }

    private Path resolveVideoPath(String path) {
        try {
            Path root = Paths.get(VIDEO_ROOT).toRealPath();
            Path requested = root.resolve(path).normalize();
            if (!requested.startsWith(root)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            Path resolved = requested.toRealPath();
            if (!resolved.startsWith(root)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            return resolved;
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (InvalidPathException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private Path videoFile(HttpServletRequest request, String prefix) {
        Path path = resolveVideoPath(decodePath(request, prefix));
        if (!Files.isRegularFile(path)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return path;
    }

    @RequestMapping(value = {"/", "show"})
    public String show() {
        return "show";
    }

    @RequestMapping("list")
    @ResponseBody
    public List<FileNode> listNoSubUri(HttpServletRequest request) {
        return list(request);
    }

    @RequestMapping("list/**")
    @ResponseBody
    public List<FileNode> list(HttpServletRequest request) {
        final List<FileNode> list = new ArrayList<>();
        String relativePath = decodePath(request, "/list");
        Path directory = resolveVideoPath(relativePath);
        if (!Files.isDirectory(directory)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        try (Stream<Path> files = Files.list(directory)) {
            files.forEach(file -> {
                String fileName = file.getFileName().toString();
                try {
                    Path resolved = resolveVideoPath(relativePath.isEmpty() ? fileName : relativePath + "/" + fileName);
                    if (Files.isDirectory(resolved)) list.add(new FileNode(fileName, false));
                    else if (fileName.toLowerCase(Locale.ROOT).endsWith(".mp4")) list.add(new FileNode(fileName, true));
                } catch (ResponseStatusException ignored) {
                    // Entries outside the configured root and broken links are not browseable.
                }
            });
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        logger.info("File List Request. List size is " + list.size());
        return list;
    }

    @RequestMapping("video-random-accessible/**")
    public ResponseEntity<Resource> videoRandomAccessible(HttpServletRequest request) {
        Path path = videoFile(request, "/video-random-accessible");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("video/mp4"))
                .body(new FileSystemResource(path));
    }

    @RequestMapping("video/**")
    public ResponseEntity<Resource> video(HttpServletRequest request) {
        Path path = videoFile(request, "/video");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(path.getFileName().toString(), StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(path));
    }

    @RequestMapping("save")
    public String save(Material material) {
        if (StringUtils.hasLength(material.getVideoName()))
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
        final Material material = materialRepository.findById(id).orElse(null);
        if (material == null)
            throw new IllegalArgumentException("[" + id + "] data is not exist.");

        String videoName = material.getVideoName();
        int lastIndexOf = videoName.lastIndexOf("/");
        videoName = lastIndexOf >= 0 ? videoName.substring(lastIndexOf + 1, videoName.length()) : videoName;
        final String csvFileName = videoName + "_" + (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(material.getCreatedAt())) + ".csv";

        response.setContentType("text/csv");

        String headerKey = "Content-Disposition";
        String headerValue = ContentDisposition.attachment().filename(csvFileName, StandardCharsets.UTF_8).build().toString();
        response.setHeader(headerKey, headerValue);

        final ICsvListWriter csvWriter = new CsvListWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        final String[] header = {"timestamp", "key"};
        csvWriter.writeHeader(header);

        for (MaterialData data : material.getMaterialDataList())
            csvWriter.write(data.getTimestamp(), csvLiteralKey(data.getKey()));

        csvWriter.close();
    }

    private String csvLiteralKey(String key) {
        int first = 0;
        boolean leadingControl = false;
        while (first < key.length()) {
            char character = key.charAt(first);
            if (!Character.isWhitespace(character) && !Character.isSpaceChar(character) && !Character.isISOControl(character)) break;
            leadingControl |= Character.isISOControl(character);
            first++;
        }
        return leadingControl || (first < key.length() && "=+-@".indexOf(key.charAt(first)) >= 0) ? "'" + key : key;
    }
}