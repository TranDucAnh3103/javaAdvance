package Service.Product;

import DAO.Product.CategoryDAO;
import Exceptions.*;
import Models.CoreEntities.Category;

import java.util.List;

public class CategoryService {
    private CategoryDAO categoryDAO = new CategoryDAO();


    public List<Category> getAllCategories() throws DatabaseException {
        try {
            return categoryDAO.getAllCategories();
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi lấy danh sách danh mục: " + e.getMessage(), e);
        }
    }

    public boolean addCategory(String name) throws InvalidInputException, DuplicateResourceException, DatabaseException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Tên danh mục không được để trống!");
        }
        try {
            Category exist = categoryDAO.getCategoryByName(name.trim());
            if (exist != null && !exist.isDeleted()) { // kiểm tra nếu chưa delete -> trùng tên
                throw new DuplicateResourceException("Tên danh mục đã tồn tại!");
            } else if (exist != null && exist.isDeleted()) { // kiểm tra nếu đã delete -> cập nhật
                // Phục hồi lại danh mục đã bị xóa mềm và cập nhật tên (hoặc dùng lại tên cũ)
                exist.setDeleted(false);
                return categoryDAO.updateCategory(exist);
            }
            Category cat = new Category();
            cat.setName(name.trim());
            cat.setDeleted(false);
            return categoryDAO.addCategory(cat);
        } catch (InvalidInputException | DuplicateResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi thêm danh mục: " + e.getMessage(), e);
        }
    }

    public boolean updateCategory(int id, String newName) throws InvalidInputException, DuplicateResourceException, DatabaseException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new InvalidInputException("Tên danh mục không được để trống!");
        }
        try {
            Category existById = categoryDAO.getCategoryById(id);
            if (existById == null || existById.isDeleted()) {
                throw new InvalidInputException("ID danh mục không tồn tại hoặc đã bị xóa!");
            }

            Category existByName = categoryDAO.getCategoryByName(newName.trim());
            if (existByName != null && existByName.getId() != id && !existByName.isDeleted()) {
                throw new DuplicateResourceException("Tên mới trùng với một danh mục khác đã tồn tại!");
            }

            // Nếu trùng tên cũ thì vẫn cho update bình thường (hoặc bỏ qua)
            existById.setName(newName.trim());
            return categoryDAO.updateCategory(existById);
        } catch (InvalidInputException | DuplicateResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi sửa danh mục: " + e.getMessage(), e);
        }
    }

    public boolean deleteCategory(int id) throws InvalidInputException, DatabaseException {
        try {
            Category existById = categoryDAO.getCategoryById(id);
            if (existById == null || existById.isDeleted()) {
                throw new InvalidInputException("ID danh mục không tồn tại hoặc đã bị xóa!");
            }
            return categoryDAO.deleteCategory(id);
        } catch (InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi xóa danh mục: " + e.getMessage(), e);
        }
    }
}
