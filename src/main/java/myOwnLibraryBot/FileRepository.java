package myOwnLibraryBot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {

    public FileRepository(){


        try(Connection conn = Database.getConnection();
        Statement stmt = conn.createStatement()){
            stmt.execute("CREATE TABLE IF NOT EXISTS files("+
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "title TEXT NOT NULL,"+
                    "file_path TEXT NOT NULL,"+
                    "cover_path TEXT NOT NULL"+
                    ");");
            String sql = "INSERT INTO files (title, file_path, cover_path) "+
                    "VALUES (?, ?, ?)";

            PreparedStatement pstmt1 = conn.prepareStatement(sql);
            PreparedStatement pstmt2 = conn.prepareStatement(sql);

            pstmt1.setString(1, "hiragana");
            pstmt1.setString(2, "files/Books/hiragana.pdf");
            pstmt1.setString(3, "files/Covers/hiragana.png");

            pstmt1.executeUpdate();

            pstmt2.setString(1, "katakana");
            pstmt2.setString(2, "katakana.pdf");
            pstmt2.setString(3, "katakana.png");

            pstmt2.executeUpdate();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public List<FileEntry> searchFiles(String query) throws SQLException{
        List<FileEntry> result = new ArrayList<>();
        String sql = "SELECT * FROM files WHERE LOWER(title) LIKE ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new FileEntry(
                        rs.getString("title"),
                        rs.getString("file_path"),
                        rs.getString("cover_path")
                ));
            }
        }
        return result;
    }

    public static class FileEntry{
        public String title;
        public String filePath;
                public String coverPath;

        public FileEntry(String title, String filePath, String coverPath){
            this.title = title;
            this.filePath = filePath;
            this.coverPath = coverPath;
        }
    }
}
