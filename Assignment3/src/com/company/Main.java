package com.company;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.company.Main.Test.isValid;

public class Main {

    public static void main(String[] args) throws IOException {
        Set<String> parsed = new HashSet<>();
        Queue<String> pending = new PriorityQueue<>();
        System.out.println("Enter link of  website");
        String url;
        Scanner sc = new Scanner(System.in);
        url = sc.nextLine();
        pending.add(url);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet0 = workbook.createSheet(" text in html page ");
        XSSFSheet spreadsheet = workbook.createSheet(" tags in html page");
        XSSFSheet spreadsheetFaculty = workbook.createSheet(" faculty information ");
        XSSFRow row;

        Map<String, Object[]> crawledInfo = new TreeMap<>();
        crawledInfo.put("1", new Object[]{"link", "a tag name"});

        int count = 2;
        int spreadsheet0Row = 0;
        int spreadsheetFacultyRow = 0;
        while (pending.size() != 0 && parsed.size() < 1) {
            
            url = pending.poll();
            if (!isValid(url) || !url.contains("pec.ac.in"))
                continue;
            Document doc;
            Elements links;
            String text;
            try {
                doc = Jsoup.connect(url).get();
                links = doc.select("a[href]");
            } catch (Exception e) {
                continue;
            }
            System.out.println(links.size());
            for (Element link : links) {
                String newUrl = "";

                if (!isValid(link.attr("href"))) {
                    newUrl = "https://pec.ac.in/" + link.attr("href");
                    if (!isValid(newUrl)) continue;
                }
                System.out.println(newUrl);
                pending.add(newUrl);
                crawledInfo.put(Integer.toString(count++), new Object[]{newUrl, link.text()});
                try {
                    text = Jsoup.connect(newUrl).get().body().text();
                    row = spreadsheet0.createRow(spreadsheet0Row++);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(newUrl);
                    cell = row.createCell(1);
                    cell.setCellValue(text);
                } catch (Exception e) {
                    System.out.println("invalid url");
                }

                if (link.toString().contains("faculty")) {
                    try {
                        Document faculty = Jsoup.connect(newUrl).get();
                        String facultyInfo = faculty.getElementsByClass("panel-body").not("col-md-10").text();
                        row = spreadsheetFaculty.createRow(spreadsheetFacultyRow++);
                        Cell cell = row.createCell(0);
                        cell.setCellValue(link.attr("href").replace("faculty", ""));
                        row = spreadsheetFaculty.createRow(spreadsheetFacultyRow++);
                        cell = row.createCell(1);
                        cell.setCellValue(facultyInfo);
                        spreadsheetFacultyRow++;
                    } catch (Exception e) {
                        System.out.println("invalid url");
                    }
                }
            }
            parsed.add(url);
            System.out.println(parsed.size());

        }
        Set<String> keyid = crawledInfo.keySet();
        int rowid = 0;
        for (String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            Object[] objectArr = crawledInfo.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String) obj);
            }
        }
        FileOutputStream out = new FileOutputStream(new File("C:/Users/HP/Desktop/Data.xlsx"));

        workbook.write(out);
        out.close();
        System.out.println("Data Extract successfully");
    }

    static class Test {
        public static boolean isValid(String url) {
            try {
                new URL(url).toURI();
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
    }
}