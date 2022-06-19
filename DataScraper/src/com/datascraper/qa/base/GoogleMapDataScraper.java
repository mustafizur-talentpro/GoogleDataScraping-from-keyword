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

public class GoogleMapDataScraper {
	static WebDriver driver;
	// ChromeDriver driver;
	static FileInputStream file;
	static JavascriptExecutor je;
	public static String projectpath;
	static XSSFWorkbook workbook;
	static XSSFSheet sheet;
	static XSSFWorkbook wb;
	static XSSFSheet sh;
	// public static String keyword = "software company in texas";
	public static String keyword = "";
	public String website = "";
	static String[] lines;
	static String strlinkText2;
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
	            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/" +databasename+"", "root", "");
	            if (!conn.isClosed())
	                System.out.println("Successfully connected to MySQL server...");

	        } catch (Exception e) {

	        }
		for (int i = 1; i <= rowNumber; i++) {
			keyword = sheet.getRow(i).getCell(0).getStringCellValue();
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

			tablename = keyword.replaceAll("\\s", "");
			System.out.println("Table name: " + tablename);
			// for create table into database
			createtablesql = "create table " + tablename + "(Keyword VARCHAR(255), " + " CompanyName VARCHAR(255), "
					+ " Reviews VARCHAR(255), " + " Type VARCHAR(255), " + " Address VARCHAR(255), "
					+ " Phone VARCHAR(255), " + " Website VARCHAR(255))";
			pstmt = conn.prepareStatement(createtablesql);
			pstmt.executeUpdate();

			// For search in google
			driver.findElement(By.name("q")).sendKeys(keyword);
			driver.findElement(By.name("q")).sendKeys(Keys.ENTER);
			driver.findElement(By.linkText("Change to English")).click();
			driver.findElement(By.linkText("More places")).click();
			while (true)
			{
				Thread.sleep(1000);
				List<WebElement> element = driver.findElements(By.className("VkpGBb"));
				Thread.sleep(500);
				for (WebElement objelements : element) {
					List<WebElement> webEleList = objelements.findElements(By.className("cXedhc"));
					Thread.sleep(500);
					for (WebElement objText : webEleList) {

						Thread.sleep(500);
						String strlinkText = objText.getText();
						Thread.sleep(500);
						
						//for split newline string and extract phone number from string
						lines = strlinkText.split("\r?\n|\r");
						int h = lines.length;
						String phoneno = lines[h - 1];
						String fullNo = "";
						for (int m = phoneno.length() - 1; m > 0; m--) {
							if (phoneno.charAt(m) == '+') {
								break;
							} else {
								fullNo = fullNo + phoneno.charAt(m);

							}
						}
						StringBuilder input1 = new StringBuilder();
						input1.append(fullNo);
						input1.reverse();
						lines[h - 1] = "+" + input1;
						System.out.println(Arrays.asList(lines));

						Thread.sleep(500);

						List<WebElement> webEleList2 = objelements.findElements(By.linkText("WEBSITE"));
						for (WebElement objText2 : webEleList2) {
							Thread.sleep(500);
							strlinkText2 = objText2.getAttribute("href");
							Thread.sleep(500);
							System.out.println(strlinkText2);

							// ========for data send sql string=========
							int n = lines.length;
							String wh = "?,";
							switch (n) {
							case 1:
								sql = "insert into " + tablename + " value (?," + wh.repeat(n) + " ?)";
								break;
							case 2:
								sql = "insert into " + tablename + " value (?," + wh.repeat(n) + " ?)";
								break;
							case 3:
								sql = "insert into " + tablename + " value (?," + wh.repeat(n) + " ?)";
								break;
							case 4:
								sql = "insert into " + tablename + " value (?," + wh.repeat(n) + " ?)";
								break;
							case 5:
								sql = "insert into " + tablename + " value (?," + wh.repeat(n) + " ?)";
								break;

							default:
								System.out.println("Invalid key");
								break;
							}
							pstmt = conn.prepareStatement(sql);
							
							
							if (n == 5) {
								pstmt.setString(1, keyword);
								for (int k = 0; k < n; k++) {
									pstmt.setString(k + 2, lines[k]);
									
								}
								pstmt.setString(n + 2, strlinkText2);
								Thread.sleep(500);
								// For executeUpdate
								pstmt.executeUpdate();
							} else {
								continue;
							}

						}

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
