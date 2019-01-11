package common.utils;


import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.out;

public class ExcelUtil {

    /**
     * @param title
     * @param headers
     * @param dataset
     * @param out
     * @param <T>
     */
    public static <T> void exportExcel(String title, String[] headers, Collection<T> dataset, OutputStream out) {

        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();

        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(title);

        exportBaisc(sheet, workbook, headers, 0, dataset);

        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * @param workbook
     * @param file
     * @param sheetName
     * @param dataset
     * @param headers
     * @param <T>
     * @throws Exception
     */
    public static <T> void exportExcel(HSSFWorkbook workbook, File file, String sheetName, Collection<T> dataset, String[] headers) throws Exception {

        HSSFSheet sheetNew = workbook.getSheet(sheetName);
        exportBaisc(sheetNew, workbook, headers, sheetNew.getPhysicalNumberOfRows(), dataset);
        OutputStream out = new FileOutputStream(file);
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static <T> void exportBaisc(HSSFSheet sheet, HSSFWorkbook workbook, String[] headers, int startRow, Collection<T> dataset) {

        sheet.setDefaultColumnWidth(30);

        // 生成一个样式
        HSSFCellStyle style = workbook.createCellStyle();

        // 设置这些样式
        style.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.BLACK.index);
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        // 把字体应用到当前的样式
        style.setFont(font);

        // 生成并设置另一个样式
        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.WHITE.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style2.setWrapText(true);
        HSSFDataFormat format = workbook.createDataFormat();
        style2.setDataFormat(format.getFormat("@"));

        // 生成另一个字体
        HSSFFont font2 = workbook.createFont();
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);

        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellStyle(style);
            cell.setCellValue(text);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = startRow == 0 ? startRow : startRow - 1;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = (T) it.next();
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                Field field = fields[i];
                String fieldName = field.getName();
                String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                try {
                    Class tCls = t.getClass();
                    Method getMethod = tCls.getMethod(getMethodName, new Class[]{});
                    Object value = getMethod.invoke(t, new Object[]{}) != null ? getMethod.invoke(t, new Object[]{}) : "";
                    String textValue = null;
                    if (value instanceof Date) {
                        Date date = (Date) value;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        textValue = sdf.format(date);
                    }
                    // 其它数据类型都当作字符串简单处理
                    else {
                        textValue = value.toString();
                    }
                    // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
                    if (textValue != null) {
                        HSSFRichTextString richString = new HSSFRichTextString(textValue);
                        HSSFFont font3 = workbook.createFont();
                        font3.setColor(HSSFColor.BLACK.index);
                        richString.applyFont(font3);
                        cell.setCellValue(richString);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } finally {
                    // 清理资源
                }
            }

        }
    }


    /**
     * @param fieldName  excel数据的抬头栏，即名称栏
     * @param fieldData  excel导出的实际数据
     * @param splitCount Excel每个工作表的行数
     * @return HSSFWorkbook
     */
    public static HSSFWorkbook createWorkbook(List<String> fieldName, List<?> fieldData, int splitCount) {
        HSSFWorkbook workBook = new HSSFWorkbook();//创建一个工作簿
        int rows = fieldData.size();//清点出输入数据的行数
        int sheetNum = 0;//将工作表个数清零
        //根据数据的行数与每个工作表所能容纳的行数，求出需要创建工作表的个数
        if (rows % splitCount == 0) {
            sheetNum = rows / splitCount;
        } else {
            sheetNum = rows / splitCount + 1;
        }

        for (int i = 1; i <= sheetNum; i++) {
            HSSFSheet sheet = workBook.createSheet("Page " + i);//创建工作表
            HSSFRow headRow = sheet.createRow(0); //创建第一栏，抬头栏
            for (int j = 0; j < fieldName.size(); j++) {
                HSSFCell cell = headRow.createCell(j);//创建抬头栏单元格
                //设置单元格格式
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                sheet.setColumnWidth(j, 6000);
                HSSFCellStyle style = workBook.createCellStyle();
                HSSFFont font = workBook.createFont();
                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                short color = HSSFColor.RED.index;
                font.setColor(color);
                style.setFont(font);
                //将数据填入单元格
                if (fieldName.get(j) != null) {
                    cell.setCellStyle(style);
                    cell.setCellValue((String) fieldName.get(j));
                } else {
                    cell.setCellStyle(style);
                    cell.setCellValue("-");
                }
            }
            //创建数据栏单元格并填入数据
            for (int k = 0; k < (rows < splitCount ? rows : splitCount); k++) {
                if (((i - 1) * splitCount + k) >= rows) {
                    break;
                }
                HSSFRow row = sheet.createRow(k + 1);
                List<?> rowList = (List<?>) fieldData.get((i - 1)
                        * splitCount + k);
                for (int n = 0; n < rowList.size(); n++) {
                    HSSFCell cell = row.createCell(n);
                    if (rowList.get(n) != null) {
                        cell.setCellValue((String) rowList.get(n).toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            }
        }
        return workBook;
    }

    public static Map<Integer, String> readExcelHeaders(String filePath) throws Exception {
        String extensionName = getExtensionName(filePath);

        if (extensionName.equalsIgnoreCase("xlsx")) {
            return readHeadersExcel2007(filePath);
        } else {
            return readHeadersExcel2003(filePath);
        }
    }

    private static Map<Integer, String> readHeadersExcel2007(String filePath) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            XSSFWorkbook xwb = new XSSFWorkbook(fis);   // 构造 XSSFWorkbook 对象，strPath 传入文件路径
            XSSFSheet sheet = xwb.getSheetAt(0);            // 读取第一章表格内容
            // 定义 row、cell
            XSSFRow row;
            // 循环输出表格中的第一行内容   表头
            Map<Integer, String> keys = new HashMap<Integer, String>();
            row = sheet.getRow(0);
            if (row != null) {
                for (int j = row.getFirstCellNum(); j <= row.getPhysicalNumberOfCells(); j++) {
                    // 通过 row.getCell(j).toString() 获取单元格内容，
                    if (row.getCell(j) != null) {
                        if (!row.getCell(j).toString().isEmpty()) {
                            keys.put(j, row.getCell(j).toString());
                        }
                    } else {
                        keys.put(j, "K-R1C" + j + "E");
                    }
                }
            }
            return keys;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

    }


    private static Map<Integer, String> readHeadersExcel2003(String filePath) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            HSSFWorkbook wookbook = new HSSFWorkbook(fis);  // 创建对Excel工作簿文件的引用
            HSSFSheet sheet = wookbook.getSheetAt(0);   // 在Excel文档中，第一张工作表的缺省索引是0
            Map<Integer, String> keys = new HashMap<Integer, String>();
            int cells = 0;
            // 遍历行­（第1行  表头） 准备Map里的key
            HSSFRow firstRow = sheet.getRow(0);
            if (firstRow != null) {
                // 获取到Excel文件中的所有的列
                cells = firstRow.getPhysicalNumberOfCells();
                // 遍历列
                for (int j = 0; j < cells; j++) {
                    // 获取到列的值­
                    try {
                        HSSFCell cell = firstRow.getCell(j);
                        String cellValue = getCellValue(cell);
                        keys.put(j, cellValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return keys;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

    }

    /**
     * 读取excel
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> readExcel(String filePath) throws Exception {
        String extensionName = getExtensionName(filePath);
        if (extensionName.equalsIgnoreCase("xlsx")) {
            return readExcel2007(filePath);
        } else {
            return readExcel2003(filePath);
        }

    }

    private static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }


    private static List<Map<String, Object>> readExcel2007(String filePath) throws IOException {
        List<Map<String, Object>> valueList = new ArrayList<Map<String, Object>>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            XSSFWorkbook xwb = new XSSFWorkbook(fis);   // 构造 XSSFWorkbook 对象，strPath 传入文件路径
            XSSFSheet sheet = xwb.getSheetAt(0);            // 读取第一章表格内容
            // 定义 row、cell
            XSSFRow row;
            // 循环输出表格中的第一行内容   表头
            Map<Integer, String> keys = new HashMap<Integer, String>();
            row = sheet.getRow(0);
            if (row != null) {
                for (int j = row.getFirstCellNum(); j <= row.getPhysicalNumberOfCells(); j++) {
                    // 通过 row.getCell(j).toString() 获取单元格内容，
                    if (row.getCell(j) != null) {
                        if (!row.getCell(j).toString().isEmpty()) {
                            keys.put(j, row.getCell(j).toString());
                        }
                    } else {
                        keys.put(j, "K-R1C" + j + "E");
                    }
                }
            }
            // 循环输出表格中的从第二行开始内容
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getPhysicalNumberOfRows(); i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    boolean isValidRow = false;
                    Map<String, Object> val = new HashMap<String, Object>();
                    for (int j = row.getFirstCellNum(); j <= row.getPhysicalNumberOfCells(); j++) {
                        XSSFCell cell = row.getCell(j);
                        if (cell != null) {
                            String cellValue = null;
                            if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    cellValue = new DataFormatter().formatRawCellContents(cell.getNumericCellValue(), 0, "yyyy-MM-dd");
                                } else {
                                    DecimalFormat df = new DecimalFormat("#");
                                    cellValue = df.format(cell.getNumericCellValue());
                                }
                            } else {
                                cellValue = cell.toString().replaceAll(" ", "");
                            }
                            if (cellValue != null && cellValue.trim().length() <= 0) {
                                cellValue = null;
                            }
                            val.put(keys.get(j), cellValue);
                            if (!isValidRow && cellValue != null && cellValue.trim().length() > 0) {
                                isValidRow = true;
                            }
                        }
                    }

                    // 第I行所有的列数据读取完毕，放入valuelist
                    if (isValidRow) {
                        valueList.add(val);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fis.close();
        }

        return valueList;
    }


    private static List<Map<String, Object>> readExcel2003(String filePath) throws IOException {
        //返回结果集
        List<Map<String, Object>> valueList = new ArrayList<Map<String, Object>>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            HSSFWorkbook wookbook = new HSSFWorkbook(fis);  // 创建对Excel工作簿文件的引用
            HSSFSheet sheet = wookbook.getSheetAt(0);   // 在Excel文档中，第一张工作表的缺省索引是0
            int rows = sheet.getPhysicalNumberOfRows(); // 获取到Excel文件中的所有行数­
            Map<Integer, String> keys = new HashMap<Integer, String>();
            int cells = 0;
            // 遍历行­（第1行  表头） 准备Map里的key
            HSSFRow firstRow = sheet.getRow(0);
            if (firstRow != null) {
                // 获取到Excel文件中的所有的列
                cells = firstRow.getPhysicalNumberOfCells();
                // 遍历列
                for (int j = 0; j < cells; j++) {
                    // 获取到列的值­
                    try {
                        HSSFCell cell = firstRow.getCell(j);
                        String cellValue = getCellValue(cell);
                        keys.put(j, cellValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // 遍历行­（从第二行开始）
            for (int i = 1; i < rows; i++) {
                // 读取左上端单元格(从第二行开始)
                HSSFRow row = sheet.getRow(i);
                // 行不为空
                if (row != null) {
                    //准备当前行 所储存值的map
                    Map<String, Object> val = new HashMap<String, Object>();

                    boolean isValidRow = false;

                    // 遍历列
                    for (int j = 0; j < cells; j++) {
                        // 获取到列的值­
                        try {
                            HSSFCell cell = row.getCell(j);
                            String cellValue = getCellValue(cell);
                            val.put(keys.get(j), cellValue.replaceAll(" ", ""));
                            if (!isValidRow && cellValue != null && cellValue.trim().length() > 0) {
                                isValidRow = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //第I行所有的列数据读取完毕，放入valuelist
                    if (isValidRow) {
                        valueList.add(val);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fis.close();
        }
        return valueList;
    }

    private static String getCellValue(HSSFCell cell) {
        DecimalFormat df = new DecimalFormat("#");
        String cellValue = null;
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    cellValue = sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
                    break;
                }
                cellValue = df.format(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case HSSFCell.CELL_TYPE_FORMULA:
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                cellValue = null;
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                cellValue = String.valueOf(cell.getErrorCellValue());
                break;
        }
        if (cellValue != null && cellValue.trim().length() <= 0) {
            cellValue = null;
        }
        return cellValue;
    }

    /**
     * 遍历excel
     *
     * @param file
     * @throws Exception
     */
    public static void showExcel(File file) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet sheet = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {//获取每个Sheet表
            sheet = workbook.getSheetAt(i);
            for (int j = 0; j < sheet.getPhysicalNumberOfRows(); j++) {//获取每行
                HSSFRow row = sheet.getRow(j);
                for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {//获取每个单元格
                    out.print(row.getCell(k) + "\t");
                }
                out.println("---Sheet表" + i + "处理完毕---");
            }
        }
    }


    //得到指定页最后一行指定列的数据
    public static String getLastRowAndIndexCellValue(HSSFWorkbook workbook, String sheetName, int index) {
        HSSFSheet fsheet = workbook.getSheet(sheetName);
        HSSFRow row = fsheet.getRow(fsheet.getPhysicalNumberOfRows() - 1);
        Cell cell = row.getCell(index);
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.toString();
        }
        if (HSSFDateUtil.isCellDateFormatted(cell)) {// 处理日期格式、时间格式
            SimpleDateFormat sdf = null;
            if (cell.getCellStyle().getDataFormat() == HSSFDataFormat
                    .getBuiltinFormat("h:mm")) {
                sdf = new SimpleDateFormat("HH:mm");
            } else {// 日期
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            Date date = cell.getDateCellValue();
            return sdf.format(date);
        }
        return cell.toString();
    }


    /**
     * 获得每页行号
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static List getSheetAndRow(File file) throws Exception {
        List sheetAndRow = new ArrayList<>();

        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet sheet = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {//获取每个Sheet表
            sheet = workbook.getSheetAt(i);

            Object[] sheetS = new Object[2];
            sheetS[0] = sheet.getSheetName();
            sheetS[1] = sheet.getPhysicalNumberOfRows();
            sheetAndRow.add(sheetS);
        }

        return sheetAndRow;
    }


}
