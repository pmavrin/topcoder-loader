import org.htmlcleaner.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: pashka
 */
public class LoadFromTopcoder {

    private final int problemId;

    public LoadFromTopcoder(int problemId) {
        this.problemId = problemId;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: LoadFromTopcoder <problem-id> [options]");
            return;
        }
        new LoadFromTopcoder(Integer.parseInt(args[0])).load();
    }

    private void load() throws IOException {
        TagNode tagNode = loadFile("https://community.topcoder.com/stat?c=problem_statement&pm=" + problemId);
        findStatement(tagNode);
        PrintWriter statementOut = new PrintWriter("statement.tex");
        statementOut.print(statement.toString());
        statementOut.close();
    }

    private TagNode loadFile(String url) {
        String htmlFile = loadHtml(url);
        CleanerProperties props = new CleanerProperties();
        HtmlCleaner htmlCleaner = new HtmlCleaner(props);
        TagNode tagNode = htmlCleaner.clean(htmlFile);
        return tagNode;
    }

    private String loadHtml(String s) {
        try {
            URL url = new URL(s);
            URLConnection con = url.openConnection();
            Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
            Matcher m = p.matcher(con.getContentType());
            String charset = m.matches() ? m.group(1) : "ISO-8859-1";
            Reader r = new InputStreamReader(con.getInputStream(), charset);
            StringBuilder buf = new StringBuilder();
            while (true) {
                int ch = r.read();
                if (ch < 0)
                    break;
                buf.append((char) ch);
            }
            return buf.toString();
        } catch (Exception e) {
        }
        return null;
    }

    private void findStatement(TagNode node) {
        if ("problemText".equals(node.getAttributeByName("class"))) {
            parseStatement(node);
        } else {
            for (BaseToken token : node.getAllChildren()) {
                if (token instanceof TagNode) {
                    findStatement((TagNode) token);
                }
            }
        }
    }

    StringBuilder statement = new StringBuilder();

    private void parseStatement(TagNode node) {
        node = node.getChildTags()[0].getChildTags()[0];
        parseStory(node);
        parseConstraints(node);
    }

    private void parseConstraints(TagNode node) {
        int i = 0;
        while (true) {
            try {
                String text = node.getChildTags()[i].getChildTags()[0].getChildTags()[0].getText().toString();
                text = text.trim();
                if (text.equals("Constraints")) {
                    break;
                }
            } catch (Exception e) {
            }
            i++;
        }
        i++;
        statement.append("\nConstraints\n\\begin{itemize}\n");
        while (node.getChildTags()[i].getChildTags().length > 1) {
            statement.append("\\item " + node.getChildTags()[i].getChildTags()[1].getText() + "\n");
            i++;
        }
        statement.append("\\end{itemize}\n");

    }

    private void parseStory(TagNode node) {
        node = node.getChildTags()[1].getChildTags()[1];
        for (TagNode p : node.getChildTags()) {
            statement.append(p.getText()).append("\n");
        }
    }
}
