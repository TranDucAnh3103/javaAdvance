package Service.Sales;

import Models.DTO.TopProductDTO;
import Exceptions.DatabaseException;
import Exceptions.InvalidInputException;

import java.util.List;

public interface StatisticsService {
    /**
     * Lấy Top N sản phẩm bán chạy nhất trong tháng/năm được chỉ định.
     */
    List<TopProductDTO> getTopSellingProducts(int month, int year, int limit) throws InvalidInputException, DatabaseException;
}
