package kr.tinywind.reactpracticeweb.config;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@Service
public class React {
    private ScriptEngine engine;

    @PostConstruct
    private void init() {
        try {
            engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval("var window = this;");
            engine.eval(read("lib/react-15.1.0/react.min.js"));
            engine.eval(read("lib/react-15.1.0/react-dom.min.js"));
            engine.eval(read("lib/react-15.1.0/react-dom-server.min.js"));
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
}
