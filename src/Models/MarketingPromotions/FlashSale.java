package Models.MarketingPromotions;

import java.sql.Timestamp; // Thư viện mở rộng từ java.util.Date, tương thích hoàn toàn với kiểu TIMESTAMP của SQL.

public class FlashSale {
    private int id;
    private String name;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean isActive;

    public FlashSale() {}

    public FlashSale(int id, String name, Timestamp startTime, Timestamp endTime, boolean isActive) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "FlashSale{" +
                "endTime=" + endTime +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", isActive=" + isActive +
                '}';
    }
}