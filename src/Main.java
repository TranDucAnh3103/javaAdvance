import Presentation.Auth.LoginRegister;
String pad = " ".repeat(40);

void main() {
    printSplashScreen();


    try {
        // Tạo hiệu ứng giống như ứng dụng đang thực sự khởi động các module (Bản chất là delay Console)
//        System.out.println("[System] Đang nạp cấu hình...");
//        Thread.sleep(300);
//        System.out.println("[System] Đang kết nối Cơ sở dữ liệu...");
//        Thread.sleep(400);
//        System.out.println("[System] Sẵn sàng phục vụ!\n");
//        Thread.sleep(300);

        // Khởi tạo app
        LoginRegister lg = new LoginRegister();
        lg.displayMenu();

    } catch (Exception e) {
        // Bắt lỗi toàn cục
        System.err.println("\n[Critical Error] Ứng dụng dính lỗi nghiêm trọng: " + e.getMessage());
        e.printStackTrace();
    } finally {
        System.out.println("\n" + pad + "=======================================================");
        System.out.println(pad +"   CẢM ƠN BẠN ĐÃ SỬ DỤNG PHẦN MỀM. HẸN GẶP LẠI! ^^");
        System.out.println(pad +"=======================================================");
    }
}


// Phương thức để vẽ Logo ASCII nghệ thuật ra Console
private void printSplashScreen() {
    // Tạo 1 biến chứa 40 khoảng trắng (bạn có thể tăng giảm số 40 này để căn chỉnh lề trái theo ý muốn)
    
    System.out.println("\n" + pad + "=================================================================");
    System.out.println(pad + "     ____  _                      _____ __                       ");
    System.out.println(pad + "    / __ \\/ /_  ____  ____  ___  / ___// /_____  ________        ");
    System.out.println(pad + "   / /_/ / __ \\/ __ \\/ __ \\/ _ \\ \\__ \\/ __/ __ \\/ ___/ _ \\       ");
    System.out.println(pad + "  / ____/ / / / /_/ / / / /  __/___/ / /_/ /_/ / /  /  __/       ");
    System.out.println(pad + " /_/   /_/ /_/\\____/_/ /_/\\___//____/\\__/\\____/_/   \\___/        ");
    System.out.println(pad + "                                                                 ");
    System.out.println(pad + "           HỆ THỐNG QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI                  ");
    System.out.println(pad + "=================================================================\n");
}
