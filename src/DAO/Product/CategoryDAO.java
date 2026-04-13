package DAO.Product;

import Models.CoreEntities.Category;
import Util.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // lấy và hiển thị
    public List<Category> getAllCategories() throws Exception {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE is_deleted = FALSE";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category cat = new Category();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                cat.setDeleted(rs.getBoolean("is_deleted"));
                list.add(cat);
            }
        }
        return list;
    }

    // thêm danh mục mới
    public boolean addCategory(Category category) throws Exception {
        String sql = "INSERT INTO categories (name, is_deleted) VALUES (?, ?)";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isDeleted());
            return ps.executeUpdate() > 0;
        }
    }

    // cập nhật tên danh muc
    public boolean updateCategory(Category category) throws Exception {
        String sql = "UPDATE categories SET name = ? WHERE id = ? AND is_deleted = FALSE";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // xóa danh mục bằng cách cho is_deleted = true (1)
    public boolean deleteCategory(int id) throws Exception {
        String sql = "UPDATE categories SET is_deleted = TRUE WHERE id = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // tìm kiếm danh mục theo tên
    public Category getCategoryByName(String name) throws Exception {
        String sql = "SELECT * FROM categories WHERE name = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category cat = new Category();
                    cat.setId(rs.getInt("id"));
                    cat.setName(rs.getString("name"));
                    cat.setDeleted(rs.getBoolean("is_deleted"));
                    return cat;
                }
            }
        }
        return null;
    }

    // tìm kiếm theo id
    public Category getCategoryById(int id) throws Exception {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category cat = new Category();
                    cat.setId(rs.getInt("id"));
                    cat.setName(rs.getString("name"));
                    cat.setDeleted(rs.getBoolean("is_deleted"));
                    return cat;
                }
            }
        }
        return null;
    }
}
