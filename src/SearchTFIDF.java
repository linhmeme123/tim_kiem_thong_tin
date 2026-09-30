import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchTFIDF {

    private static final String DEFAULT_QUERY = "sinh viên nghiên cứu khoa học";
    private static final Path INDEX_DIR = Paths.get("lucene_index");

    static class VietnameseAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            StandardTokenizer tokenizer = new StandardTokenizer();
            TokenStream stream = new LowerCaseFilter(tokenizer);
            stream = new ASCIIFoldingFilter(stream);
            return new TokenStreamComponents(tokenizer, stream);
        }
    }

    private static Path timThuMucCol0() {
        Path col0TrongRepo = Paths.get("hust.edu.vn-2026", "col0");
        if (Files.isDirectory(col0TrongRepo)) {
            return col0TrongRepo;
        }
        return Paths.get("col0");
    }

    private static int taoChiMuc(Path thuMucDuLieu, Analyzer analyzer) throws Exception {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setSimilarity(new ClassicSimilarity());
        int count = 0;
        try (Directory directory = FSDirectory.open(INDEX_DIR);
             IndexWriter writer = new IndexWriter(directory, config);
             DirectoryStream<Path> files = Files.newDirectoryStream(thuMucDuLieu)) {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) continue;
                Document document = new Document();
                document.add(new StringField("name", file.getFileName().toString(), Field.Store.YES));
                document.add(new StringField("path", file.toString(), Field.Store.YES));
                document.add(new TextField("content", Files.readString(file, StandardCharsets.UTF_8), Field.Store.NO));
                writer.addDocument(document);
                count++;
            }
        }
        return count;
    }

    private static void timKiem(String queryText, Analyzer analyzer) throws Exception {
        try (Directory directory = FSDirectory.open(INDEX_DIR);
             DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new ClassicSimilarity());

            QueryParser parser = new QueryParser("content", analyzer);
            parser.setDefaultOperator(QueryParser.Operator.OR);
            Query query = parser.parse(queryText);

            TopDocs results = searcher.search(query, 10);
            System.out.println("Truy vấn: " + queryText);
            System.out.println("Query Lucene: " + query);
            System.out.println("Kết quả:");

            int rank = 1;
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document document = searcher.storedFields().document(scoreDoc.doc);
                System.out.printf("%d. %s : %.6f%n", rank, document.get("name"), scoreDoc.score);
                rank++;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String queryText = args.length > 0 ? args[0] : DEFAULT_QUERY;
        Path thuMucDuLieu = args.length > 1 ? Paths.get(args[1]) : timThuMucCol0();

        try (Analyzer analyzer = new VietnameseAnalyzer()) {
            int soTaiLieu = taoChiMuc(thuMucDuLieu, analyzer);
            System.out.println("Da tao chi muc cho " + soTaiLieu + " tai lieu trong " + thuMucDuLieu);
            timKiem(queryText, analyzer);
        }
    }
}
