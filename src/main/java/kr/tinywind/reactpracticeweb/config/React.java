package kr.tinywind.reactpracticeweb.config;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Arrays;
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
            Map<String, String> loadingJsMap = getLoadingJsMap(new String[]{"react.min.js", "react-dom.min.js", "react-dom-server.min.js"});
            engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval("var window = this;");
            engine.eval(loadingJsMap.get("react.min.js"));
            engine.eval(loadingJsMap.get("react-dom.min.js"));
            engine.eval(loadingJsMap.get("react-dom-server.min.js"));

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

    public String renderJs(String jsFile) {
        try {
            Object html = engine.eval(read("build/" + jsFile));
            return String.valueOf(html);
        } catch (ScriptException e) {
            throw new IllegalStateException("failed to render react component", e);
        }
    }

    private Reader read(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        return new InputStreamReader(in);
    }

    private Map<String, String> getLoadingJsMap(String[] loadingJsList) throws IOException {
        final Map<String, String> loadingJsMap = Arrays.stream(loadingJsList).collect(Collectors.toMap(e -> e, e -> ""));
        final String[] classPathList = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        final byte[] buffer = new byte[1000 * 1000];
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (String classPath : classPathList) {
            if (classPath.replaceAll("[\\\\]", "/").matches("^.*/react[^/]*[.][jJ][aA][rR]$")) {
                final ZipInputStream inputStream = new JarInputStream(new FileInputStream(classPath));
                for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
                    if (entry.isDirectory())
                        continue;
                    final String entryName = entry.getName().replaceAll("[\\\\]", "/");
                    final int lastSeparator = entryName.lastIndexOf("/");
                    final String entryFileName = entryName.substring(lastSeparator >= 0 ? lastSeparator + 1 : 0);
                    final String entryContent = loadingJsMap.get(entryFileName);
                    if (entryContent == null || entryContent.length() > 0)
                        continue;
                    outputStream.reset();
                    for (int len; (len = inputStream.read(buffer)) >= 0; )
                        outputStream.write(buffer, 0, len);
                    loadingJsMap.put(entryFileName, new String(outputStream.toByteArray()));
                }
                break;
            }
        }
        return loadingJsMap;
    }
}
