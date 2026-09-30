**QUY TRÌNH ĐỂ THỰC HIỆN TRUY VẤN**
Người dùng nhập: "hợp tác đào tạo"
        ↓
Analyzer tách từ: hợp | tác | đào | tạo
        ↓
Lowercase: hợp | tác | đào | tạo
        ↓
Bỏ dấu: hop | tac | dao | tao
        ↓
QueryParser tạo query:
content:hop OR content:tac OR content:dao OR content:tao
        ↓
IndexSearcher tìm trong field content
        ↓
ClassicSimilarity tính điểm TF-IDF
        ↓
In ra top tài liệu có điểm cao nhất

**So sánh TF-IDF và BM25**
TF-IDF thường ưu tiên tài liệu có từ khóa xuất hiện nhiều và từ đó hiếm trong toàn bộ tập tài liệu. Nếu một file lặp lại nhiều lần từ "hợp tác" hoặc "đào tạo", điểm có thể tăng khá mạnh.
BM25 cũng quan tâm từ khóa có xuất hiện nhiều hay không, nhưng có cơ chế “bão hòa”. Nghĩa là một từ xuất hiện 1, 2, 3 lần thì điểm tăng, nhưng xuất hiện quá nhiều lần thì điểm không tăng mãi. BM25 cũng xử lý độ dài tài liệu tốt hơn, nên tài liệu ngắn nhưng tập trung đúng nội dung có thể được xếp cao hơn tài liệu dài lan man.
- TF-IDF: từ xuất hiện càng nhiều thì càng dễ tăng điểm
- BM25: từ xuất hiện nhiều vẫn tốt, nhưng tăng điểm có giới hạn hơn