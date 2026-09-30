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