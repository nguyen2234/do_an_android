package com.example.android_app.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.android_app.model.GiaoDich;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Lớp tiện ích xuất báo cáo ra file CSV (Tương thích hoàn hảo với Excel).
 * Phương pháp này đảm bảo không gây crash app do thiếu thư viện java.awt.
 */
public class ExcelExporter {

    public static String exportTransactionsToExcel(Context context, List<GiaoDich> transactions) {
        // Tên file báo cáo
        String fileName = "BaoCao_GiaoDich_" + System.currentTimeMillis() + ".csv";
        
        // Thư mục lưu: Android/data/com.example.android_app/files/Download
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (directory == null) return null;
        
        File filePath = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            
            // Ghi Byte Order Mark (BOM) để Excel nhận diện được tiếng Việt (UTF-8)
            fos.write(0xef);
            fos.write(0xbb);
            fos.write(0xbf);

            // Ghi tiêu đề cột (Sử dụng dấu phẩy hoặc dấu chấm phẩy tùy cài đặt Excel, 
            // thông dụng nhất là dấu phẩy)
            osw.write("ID,Tiêu đề,Số tiền,Danh mục,Loại,Ngày,Ghi chú\n");

            // Ghi dữ liệu từng dòng
            for (GiaoDich gd : transactions) {
                StringBuilder sb = new StringBuilder();
                sb.append(gd.getId()).append(",");
                sb.append(escapeCsvField(gd.getTitle())).append(",");
                sb.append(gd.getSoTien()).append(",");
                sb.append(escapeCsvField(gd.getCategory())).append(",");
                sb.append("income".equalsIgnoreCase(gd.getLoai()) ? "Thu nhập" : "Chi tiêu").append(",");
                sb.append(gd.getNgay()).append(",");
                sb.append(escapeCsvField(gd.getNote() != null ? gd.getNote() : ""));
                sb.append("\n");
                
                osw.write(sb.toString());
            }

            osw.flush();
            return filePath.getAbsolutePath();

        } catch (Exception e) {
            Log.e("ExcelExporter", "Lỗi khi ghi file CSV: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xử lý chuỗi để không làm hỏng cấu trúc file CSV (tránh lỗi nếu ghi chú có dấu phẩy).
     */
    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
