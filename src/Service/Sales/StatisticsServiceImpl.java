package Service.Sales;

import DAO.Sales.StatisticsDAO;
import DAO.Sales.StatisticsDAOImpl;
import Models.DTO.TopProductDTO;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.util.List;

public class StatisticsServiceImpl implements StatisticsService {

    private StatisticsDAO statsDAO = new StatisticsDAOImpl();

    /**
     * Validate tháng/năm rồi chuyển xuống DAO truy vấn.
     * Tại sao phải validate ở đây thay vì để DAO tự xử?
     *   - Vì nếu truyền tháng = 13 hoặc năm = -1, câu WHERE vẫn chạy bình thường
     *     nhưng sẽ trả về danh sách rỗng → Admin tưởng hệ thống lỗi. Chặn sớm cho rõ ràng.
     */
    @Override
    public List<TopProductDTO> getTopSellingProducts(int month, int year, int limit) throws InvalidInputException, DatabaseException {
        if (month < 1 || month > 12) {
            throw new InvalidInputException("Tháng phải nằm trong khoảng 1-12! Giá trị nhập: " + month);
        }
        if (year < 2000 || year > 2100) {
            throw new InvalidInputException("Năm không hợp lệ! Hệ thống chỉ hỗ trợ từ 2000 đến 2100.");
        }
        if (limit <= 0) {
            throw new InvalidInputException("Số lượng Top phải > 0!");
        }

        try {
            return statsDAO.getTopSellingProducts(month, year, limit);
        } catch (Exception e) {
            throw new DatabaseException("Lỗi khi truy vấn thống kê: " + e.getMessage(), e);
        }
    }
}
