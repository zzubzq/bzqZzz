package com.bzq.tool.excel;

import com.alibaba.fastjson.JSON;
import com.bzq.util.DateUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zzubzq on 2019/3/6.
 */
public class ExcelUtil {
    private ExcelUtil() { }

    private static final int MAX_ROW = 100;

    public static <T extends ExcelObject> HSSFWorkbook generateExcelFile(List<T> contents, Class<T> tClass, List<CascadeBean> cascadeBeanList) {
        Excel excel = tClass.getDeclaredAnnotation(Excel.class);
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<Cell, Field> fieldCellAnnotationMap = getCellAnnotationFieldMap(tClass);
        List<Cell> sortedCell = new ArrayList<>(fieldCellAnnotationMap.keySet()).stream().sorted(Comparator.comparing(Cell::order)).collect(Collectors.toList());

        HSSFSheet sheet = wb.createSheet(excel.sheetName());
        //设置列宽
        for (int i = 0; i < sortedCell.size(); i++) {
            sheet.setColumnWidth(i, sortedCell.get(i).width() * 256);
        }
        //设置标题栏
        setMainSheetTitle(wb, sheet, sortedCell);
        //设置数据校验
        sheetSetting(wb, sheet, cascadeBeanList, fieldCellAnnotationMap, sortedCell);
        //设置内容
        setContent(wb, sheet, contents, fieldCellAnnotationMap, sortedCell);

        return wb;
    }

    private static void setMainSheetTitle(HSSFWorkbook wb, HSSFSheet sheet, List<Cell> sortedCell) {
        //设置title
        HSSFRow titleRow = sheet.createRow(0);
        //创建一个title格式
        HSSFCellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setWrapText(true);
        for (int i = 0; i < sortedCell.size(); i++) {
            HSSFCell titleCell = titleRow.createCell(i);
            titleCell.setCellValue(sortedCell.get(i).titleName());
            titleCell.setCellStyle(titleStyle);
        }
    }

    private static void sheetSetting(HSSFWorkbook wb, HSSFSheet sheet, List<CascadeBean> cascadeBeanList, Map<Cell, Field> fieldCellAnnotationMap, List<Cell> sortedCell) {
        //设置下拉列表、级联操作
        if (CollectionUtils.isEmpty(cascadeBeanList)) {
            return;
        }
        //隐藏sheet
        for (int i = 0; i < cascadeBeanList.size(); i++) {
            CascadeBean cascadeBean = cascadeBeanList.get(i);
            HSSFSheet area = wb.createSheet("hideSheet" + i);
            setCascadeData(wb, area, cascadeBean);
            wb.setSheetHidden(wb.getSheetIndex(area), true);
        }
        //设置验证
        for (CascadeBean cascadeBean : cascadeBeanList) {
            int keyColumn = -1;
            for (int j = 0; j < sortedCell.size(); j++) {
                Cell cell = sortedCell.get(j);
                Field field = fieldCellAnnotationMap.get(cell);
                if (cascadeBean.getKeyField().equals(field.getName())) {
                    keyColumn = j;
                    setDataValidation(sheet, field.getName(), 1, MAX_ROW, j, j);
                    break;
                }
            }
            if (keyColumn == -1) {
                throw new RuntimeException("级联数据key未匹配到Field");
            }
            if (StringUtils.isBlank(cascadeBean.getValueField())) {
                continue;
            }
            for (int j = 0; j < sortedCell.size(); j++) {
                Cell cell = sortedCell.get(j);
                Field field = fieldCellAnnotationMap.get(cell);
                if (field.getName().equals(cascadeBean.getValueField())) {
                    for (int i = 1; i < MAX_ROW; i++) {
                        setDataValidation(sheet, getIndirectFormulaExpression(getColumnCode(keyColumn), i), i, i, j, j);
                    }
                }
            }
        }
    }

    private static <T> void setContent(HSSFWorkbook wb, HSSFSheet sheet, List<T> contents, Map<Cell, Field> fieldCellAnnotationMap, List<Cell> sortedCell) {
        if (CollectionUtils.isEmpty(contents)) {
            return;
        }
        //设置默认文本格式
        CellStyle textStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        textStyle.setDataFormat(format.getFormat("@"));
        textStyle.setAlignment(HorizontalAlignment.CENTER);
        textStyle.setWrapText(true);
        //设置内容
        for (int i = 0; i < contents.size(); i++) {
            T t = contents.get(i);
            HSSFRow contentRow = sheet.createRow(i + 1);
            for (int j = 0; j < sortedCell.size(); j++) {
                HSSFCell contentCell = contentRow.createCell(j);
                contentCell.setCellStyle(textStyle);
                Cell cell = sortedCell.get(j);
                Field field = fieldCellAnnotationMap.get(cell);
                field.setAccessible(true);
                Object o;
                try {
                    o = field.get(t);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("系统异常，请联系技术解决");
                }
                if (o instanceof Date) {
                    contentCell.setCellValue(DateUtil.getDateString((Date) o, cell.format()));
                } else {
                    contentCell.setCellValue(String.valueOf(o));
                }
            }
        }
    }

    private static void setCascadeData(HSSFWorkbook wb, HSSFSheet area, CascadeBean cascadeBean) {
        List<String> keyList = Lists.newArrayList();
        List<String> valueList = Lists.newArrayList();
        List<CascadeData> cascadeDataList = cascadeBean.getCascadeDataList();
        if (CollectionUtils.isEmpty(cascadeDataList)) {
            throw new RuntimeException("cascadeBean未设置数据, cascadeBean = " + JSON.toJSONString(cascadeBean));
        }
        keyList.addAll(cascadeDataList.stream().map(CascadeData::getKey).collect(Collectors.toList()));
        cascadeDataList.forEach(item -> {
            if (CollectionUtils.isNotEmpty(item.getValueList())) {
                valueList.addAll(item.getValueList());
            }
        });

        //导出sheet数据
        for (int i = 0; ; i++) {
            if (i > keyList.size() && i > valueList.size()) {
                break;
            }
            HSSFRow row = area.createRow(i);
            if (i < keyList.size()) {
                HSSFCell cell = row.createCell(0);
                cell.setCellValue(keyList.get(i));
            }
            if (i < valueList.size()) {
                HSSFCell cell = row.createCell(1);
                cell.setCellValue(valueList.get(i));
            }
        }

        //名称管理器 设置名称
        //设置key名称
        createName(wb, cascadeBean.getKeyField(), getRefersNameFormulaExpression(area.getSheetName(), "A", 1, cascadeDataList.size()));

        int row = 0;
        for (CascadeData cascadeData : cascadeDataList) {
            if (CollectionUtils.isNotEmpty(cascadeData.getValueList())) {
                createName(wb, cascadeData.getKey(), getRefersNameFormulaExpression(area.getSheetName(), "B", (row + 1), row += cascadeData.getValueList().size()));
            }
        }
    }

    private static HSSFName createName(HSSFWorkbook wb, String name, String expression) {
        HSSFName refer = wb.createName();
        refer.setRefersToFormula(expression);
        refer.setNameName(name);
        return refer;
    }

    private static void setDataValidation(HSSFSheet sheet, String name, int firstRow, int endRow, int firstCol, int endCol) {
        //log.info("起始行:" + firstRow + "___起始列:" + firstCol + "___终止行:" + endRow + "___终止列:" + endCol);
        //加载下拉列表内容
        DVConstraint constraint = DVConstraint.createFormulaListConstraint(name);
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList((short) firstRow, (short) endRow, (short) firstCol, (short) endCol);
        // 数据有效性对象
        HSSFDataValidation hssfDataValidation = new HSSFDataValidation(regions, constraint);
        hssfDataValidation.createErrorBox("error", "输入有误，请下拉选择");
        hssfDataValidation.setShowErrorBox(true);
        //hssfDataValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(hssfDataValidation);
    }

    private static String getRefersNameFormulaExpression(String sheetName, String columnCode, int beginRow, int endRow) {
        return sheetName + "!$" + columnCode + "$" + beginRow + ":$" + columnCode + "$" + endRow;
    }

    private static String getIndirectFormulaExpression(String columnCode, int row) {
        return "INDIRECT($" + columnCode + "$" + (row + 1) + ")";
    }

    private static Map<Integer, Character> dic = new HashMap<>(32);

    private static int base ;

    static {
        char begin = 'A';
        char end = 'Z';
        base = end - begin + 1;
        int init = 0;
        for (char i = begin; i <= end; i++) {
            dic.put(init++, i);
        }
    }

    private static String getColumnCode(int i) {
        StringBuilder resultContainer = new StringBuilder();
        int mod;

        if (i < base) {
            return String.valueOf(dic.get(i));
        }

        while (true) {
            mod = i % base;
            resultContainer.append(dic.get(mod));
            i /= base;
            if (i < base) {
                resultContainer.append((char)(dic.get(i)-1));
                break;
            }
        }
        return StringUtils.reverse(resultContainer.toString());
    }

    public static <T extends ExcelObject> List<T> parseExcelFile(HSSFWorkbook sheets, Class<T> tClass) {

        Map<Cell, Field> fieldCellAnnotationMap = getCellAnnotationFieldMap(tClass);

        List<Cell> sortedCell = new ArrayList<>(fieldCellAnnotationMap.keySet()).stream().sorted(Comparator.comparing(Cell::order)).collect(Collectors.toList());

        HSSFSheet sheet = sheets.getSheetAt(0);

        Map<Integer, Field> indexFieldMap = Maps.newHashMap();

        for (int i = 0; i < sortedCell.size(); i++) {
            indexFieldMap.put(i, fieldCellAnnotationMap.get(sortedCell.get(i)));
        }

        List<T> result = Lists.newArrayList();

        for(int i = 1; i < sheet.getPhysicalNumberOfRows() ; i++) {
            HSSFRow row = sheet.getRow(i);
            if(Objects.isNull(row)){
                continue;
            }
            T t;
            try {
                t = tClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("系统异常，请联系技术解决");
            }
            //只读和声明cell的filed数量相同的列，多余的列不处理
            for (int j = 0; j < fieldCellAnnotationMap.size(); j++) {
                HSSFCell valueCell = row.getCell(j);
                if (valueCell == null) {
                    //如果该列没有值，不处理
                    continue;
                }
                Cell cell = sortedCell.get(j);
                try {
                    valueCell.setCellType(CellType.STRING);
                    Field field = indexFieldMap.get(j);
                    field.setAccessible(true);
                    if (StringUtils.isBlank(valueCell.getStringCellValue())) {
                        continue;
                    }
                    Class<?> type = field.getType();
                    if (type == Date.class) {
                        field.set(t, DateUtil.getDateByString(valueCell.getStringCellValue(), cell.format()));
                    } else if (type == Integer.class) {
                        field.set(t, Integer.valueOf(valueCell.getStringCellValue()));
                    } else if (type == String.class) {
                        field.set(t, valueCell.getStringCellValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("系统异常，请联系技术解决");
                }
            }
            result.add(t);
        }
        return result;
    }


    private static <T extends ExcelObject> Map<Cell, Field> getCellAnnotationFieldMap(Class<T> tClass) {
        Field[] declaredFields = tClass.getDeclaredFields();

        Map<Cell, Field> fieldCellAnnotationMap = Maps.newHashMap();

        for (Field declaredField : declaredFields) {
            Annotation[] annotations = declaredField.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Cell.class) {
                    fieldCellAnnotationMap.put((Cell) annotation, declaredField);
                    break;
                }
            }
        }
        return fieldCellAnnotationMap;
    }


    /**
     * 是否excel文件
     */
    public static boolean isExcel (String path) {
        if (StringUtils.isBlank(path) || !path.contains(".")) {
            return false;
        }
        String suffix = path.substring(path.lastIndexOf(".") + 1).trim();


        return StringUtils.equals("xlsx", suffix) || StringUtils.equals("xls", suffix);
    }

}
