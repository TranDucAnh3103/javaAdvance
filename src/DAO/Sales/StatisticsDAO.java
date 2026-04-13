package DAO.Sales;

import Models.DTO.TopProductDTO;

import java.util.List;

/**
 * Interface truy vấn dữ liệu thống kê.
 * Tách riêng khỏi OrderDAO vì đây là truy vấn phân tích (aggregate), không phải CRUD đơn thuần.
 */
public interface StatisticsDAO {
    /**
     * Lấy Top N sản phẩm bán chạy nhất trong 1 tháng/năm cụ thể.
     * Dựa trên dữ liệu bảng order_details (JOIN products), loại đơn bị CANCELLED.
     */
    List<TopProductDTO> getTopSellingProducts(int month, int year, int limit) throws Exception;
}
