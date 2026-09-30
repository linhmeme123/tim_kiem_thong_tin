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
import org.apache.lucene.search.similarities.BM25Similarity;//thêm import cho BM25Similarity

public class SearchHopTacDaoTao {

    private static final String DEFAULT_QUERY = "hợp tác đào tạo";
    private static final Path INDEX_DIR = Paths.get("lucene_index_hop_tac_dao_tao");

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
        return Paths.get("hust.edu.vn-2026", "col0");
    }

    // BƯỚC 1: Tạo chỉ mục từ các file trong thư mục col0.
    private static int taoChiMuc(Path thuMucDuLieu, Analyzer analyzer) throws Exception {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // config.setSimilarity(new ClassicSimilarity());//tạo chỉ mục bằng TF-IDF
        config.setSimilarity(new BM25Similarity());
        int count = 0;

        try (Directory directory = FSDirectory.open(INDEX_DIR);
             IndexWriter writer = new IndexWriter(directory, config);
             DirectoryStream<Path> files = Files.newDirectoryStream(thuMucDuLieu)) {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) {
                    continue;
                }

                Document document = new Document();
                document.add(new StringField("name", file.getFileName().toString(), Field.Store.YES));
                document.add(new StringField("path", file.toString(), Field.Store.YES));
                document.add(new TextField("content", Files.readString(file, StandardCharsets.UTF_8), Field.Store.NO));//nội dung các file trong col0 đã được đưa vào index
                writer.addDocument(document);
                count++;
            }
        }

        return count;
    }

    // BƯỚC 2: Viết đoạn mã tìm kiếm trên chỉ mục đã tạo.
    private static void timKiem(String queryText, Analyzer analyzer) throws Exception {
        try (Directory directory = FSDirectory.open(INDEX_DIR);
             DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            // searcher.setSimilarity(new ClassicSimilarity());//Chấm điểm bằng TF-IDF
            searcher.setSimilarity(new BM25Similarity());//chấm điểm bằng MB25

            QueryParser parser = new QueryParser("content", analyzer);//tìm trong field content của các document trong index dòng analyzer
            parser.setDefaultOperator(QueryParser.Operator.OR);//các từ được nối bằng OR
            Query query = parser.parse(queryText);//biến đổi tượng text ban đầu thành query của lucence

            TopDocs results = searcher.search(query, 10);//Lucene lấy query đã tạo và tìm trong chỉ mục. Số 10 nghĩa là lấy tối đa 10 kết quả tốt nhất.
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
