package kr.tinywind.reactpracticeweb.config;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class React {
    private ScriptEngine engine;

    @PostConstruct
    private void init() throws IOException {
        try {
            final String reactJsFile = "react.min.js";
            final String reactDomJsFile = "react-dom.min.js";
            final String reactDomServerJsFile = "react-dom-server.min.js";
            final Map<String, String> loadingJsMap = getLoadingJsMap(reactJsFile, reactDomJsFile, reactDomServerJsFile);
            assert loadingJsMap != null;

            engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval("var window = this;");
            engine.eval(loadingJsMap.get(reactJsFile));
            engine.eval(loadingJsMap.get(reactDomJsFile));
            engine.eval(loadingJsMap.get(reactDomServerJsFile));
            engine.eval(read("build/example.js"));
        } catch (ScriptException e) {
            throw new IllegalStateException("could not init nashorn", e);
        }
    }

    public String render(String element) throws ScriptException {
        return engine.eval("ReactDOMServer.renderToString(React.createElement(" + element + "))").toString();
    }

    public String render(String element, Object... options) throws ScriptException {
        String option = "";
        for (int i = 0; i + 1 < options.length; i += 2)
            option += options[i].toString() + ":\"" + options[i + 1].toString().replaceAll("[\"]", "\\\"") + "\",";
        option = option.substring(0, option.length() - 1);

        return engine.eval("ReactDOMServer.renderToString(React.createElement(" + element + ", {" + option + "}))").toString();
    }

    public String renderJs(String jsFile) throws IOException {
        try {
            Object html = engine.eval(read("build/" + jsFile));
            return String.valueOf(html);
        } catch (ScriptException e) {
            throw new IllegalStateException("failed to render react component", e);
        }
    }

    private Reader read(String path) throws IOException {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path));
    }

    private Map<String, String> getLoadingJsMap(ZipInputStream jarFileInputStream, String[] loadingJsList) throws IOException {
        final byte[] buffer = new byte[1000 * 1000];
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Map<String, String> loadingJsMap = Arrays.stream(loadingJsList).collect(Collectors.toMap(e -> e, e -> ""));
        for (ZipEntry entry = jarFileInputStream.getNextEntry(); entry != null; entry = jarFileInputStream.getNextEntry()) {
            if (entry.isDirectory())
                continue;
            final String entryName = entry.getName().replaceAll("[\\\\]", "/");
            final int lastSeparator = entryName.lastIndexOf("/");
            final String entryFileName = entryName.substring(lastSeparator >= 0 ? lastSeparator + 1 : 0);
            final String entryContent = loadingJsMap.get(entryFileName);
            if (entryContent == null || entryContent.length() > 0)
                continue;
            outputStream.reset();
            for (int len; (len = jarFileInputStream.read(buffer)) >= 0; )
                outputStream.write(buffer, 0, len);
            loadingJsMap.put(entryFileName, new String(outputStream.toByteArray()));
        }

        return loadingJsMap;
    }

    private Map<String, String> getLoadingJsMap(String... loadingJsList) throws IOException {
        final byte[] buffer = new byte[1000 * 1000];
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final List<File> classList = Arrays.stream(System.getProperty("java.class.path").split(System.getProperty("path.separator"))).map(File::new).collect(Collectors.toList());
        classList.addAll(Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs()).map(FileUtils::toFile).collect(Collectors.toList()));
        final List<File> finalClassList = classList.stream().filter(e -> e != null && e.exists()).distinct().collect(Collectors.toList());
        for (File classFile : finalClassList) {
            if (classFile.getPath().replaceAll("[\\\\]", "/").matches("^.*/react-[^/]*[.][jJ][aA][rR]$"))
                return getLoadingJsMap(new JarInputStream(new FileInputStream(classFile)), loadingJsList);
            if (classFile.getPath().matches("^.*[.][jJ][aA][rR]$")) {
                final JarInputStream inputStream = new JarInputStream(new FileInputStream(classFile));
                for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
                    if (entry.getName().replaceAll("[\\\\]", "/").matches("^.*/react-[^/]*[.][jJ][aA][rR]$")) {
                        outputStream.reset();
                        for (int len; (len = inputStream.read(buffer)) >= 0; )
                            outputStream.write(buffer, 0, len);
                        return getLoadingJsMap(new JarInputStream(new ByteArrayInputStream(outputStream.toByteArray())), loadingJsList);
                    }
                }
            }
        }
        return null;
    }
}
