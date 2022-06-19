package com.datascraper.qa.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.sql.*;

public class GoogleKeywordScraperTools {
	static WebDriver driver;
	// ChromeDriver driver;
	static FileInputStream file;
	static JavascriptExecutor je;
	public static String projectpath;
	static XSSFWorkbook workbook;
	static XSSFSheet sheet;
	static String[] lines;
	static XSSFWorkbook wb;
	static XSSFSheet sh;
	// public static String keyword = "software company in texas";
	public static String keyword = "";
	public String website = "";
	static String link = "";
	static String details = "";
	static String title = "";
	static String strlinkText2;
	static String strlinkText3;
	static int rowNumber;
	public static Connection conn;
	public static PreparedStatement pstmt;
	public static String sql = "";
	public static String createtablesql = "";
	public static String tablename = "";
	public static String databasename = "";

	public static void main(String[] args) throws InterruptedException, IOException, SQLException {
		projectpath = System.getProperty("user.dir");
		file = new FileInputStream(projectpath + "/Resources/worksheet/GoogleKeywordscraper.xlsx");
		workbook = new XSSFWorkbook(file);
		sheet = workbook.getSheet("Sheet1");
		rowNumber = sheet.getPhysicalNumberOfRows();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			databasename = sheet.getRow(1).getCell(1).getStringCellValue();
			databasename = databasename.replaceAll("\\s", "");
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/" + databasename + "", "root", "");
			if (!conn.isClosed())
				System.out.println("Successfully connected to MySQL server...");

		} catch (Exception e) {

		}

		for (int i = 1; i <= rowNumber; i++) {
			keyword = sheet.getRow(i).getCell(0).getStringCellValue();
			tablename = keyword.replaceAll("\\s", "");
			System.setProperty("webdriver.chrome.driver", projectpath + "\\Resources\\Drivers\\chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
			Map<String, Object> prefs = new HashMap<>();
			Map<String, Object> langs = new HashMap<String, Object>();
			prefs.put("translate", "{'enabled' : true}");
			prefs.put("translate_whitelists", langs);
			options.setExperimentalOption("prefs", prefs);
			driver = new ChromeDriver(options);
			driver.get("https://www.google.com/");
			je = (JavascriptExecutor) driver;
			Thread.sleep(500);
			driver.manage().window().maximize();
			driver.manage().deleteAllCookies();
			driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

			// for database table creation
			System.out.println("Table name: " + tablename);
			// for create table into database
			createtablesql = "create table " + tablename + "(Title VARCHAR(255), " + " Link VARCHAR(255), "
					+ " Details VARCHAR(5000))";
			pstmt = conn.prepareStatement(createtablesql);
			pstmt.executeUpdate();

			// For search in google
			driver.findElement(By.name("q")).sendKeys(keyword);
			driver.findElement(By.name("q")).sendKeys(Keys.ENTER);
			driver.findElement(By.linkText("Change to English")).click();
			while (true) {
				Thread.sleep(1000);
				List<WebElement> element = driver.findElements(By.className("tF2Cxc"));
				Thread.sleep(500);
				System.out.println(element.size());
				for (WebElement objelements : element) {

					List<WebElement> webEleList2 = objelements.findElements(By.xpath(".//div[@class=\"yuRUbf\"]/a"));
					for (WebElement objText2 : webEleList2) {
						Thread.sleep(500);
						strlinkText2 = objText2.getText();
						Thread.sleep(500);
						System.out.println(strlinkText2);
						// for split newline string and extract phone number from string
						lines = strlinkText2.split("\r?\n|\r");
						System.out.println(Arrays.asList(lines));
					}
					List<WebElement> webEleList3 = objelements
							.findElements(By.xpath(".//div[@class=\"VwiC3b yXK7lf MUxGbd yDYNvb lyLwlc lEBKkf\"]"));
					for (WebElement objText3 : webEleList3) {
						Thread.sleep(500);
						strlinkText3 = objText3.getText();

						Thread.sleep(500);
						System.out.println(strlinkText3);
					}

					sql = "insert into " + tablename + " value (?,?,?)";
					pstmt = conn.prepareStatement(sql);

					int n = lines.length;
					if (n == 2) {
						pstmt.setString(1, lines[0]);
						Thread.sleep(500);
						pstmt.setString(2, lines[1]);
						Thread.sleep(500);
						pstmt.setString(3, strlinkText3);
						Thread.sleep(500);
						// For executeUpdate
						pstmt.executeUpdate();
					}

				}
				List<WebElement> nextPageList = driver.findElements(By.xpath("//*[@id=\"pnnext\"]"));
				if (nextPageList.size() > 0) {
				    System.out.println("Next page link is present page = "+nextPageList.size());
				    Thread.sleep(2000);
					WebElement element2 = driver.findElement(By.xpath("//*[@id=\"pnnext\"]"));
					je.executeScript("arguments[0].scrollIntoView(true);", element2);
					Thread.sleep(1000);
					driver.findElement(By.linkText("Next")).click();
					Thread.sleep(2000);
				}
				else {
				    System.out.println("Next page link is not present");
				    driver.close();
					break;
				}
				
				

			}

		}
	}
}
