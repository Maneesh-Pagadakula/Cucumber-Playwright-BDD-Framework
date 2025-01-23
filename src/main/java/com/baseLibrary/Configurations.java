package com.baseLibrary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class Configurations {

	private static Playwright playwright;
	private static Browser browser;
	private static BrowserContext context;
	private static Page page;
//    public Properties prop;
	private ExcelDataOperations excelBrowserConfig;

	public static ExtentReports reports;
	public static ExtentSparkReporter spark;

	private static Map<String, ExtentTest> featureParentTests = new HashMap<>();
	public static ExtentTest extentParentTest;

	private static final DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy_HHmmss");
	private static String baseDirectory;
	
	// Maintain a map to track configuration-specific folders
	private static Map<String, String> configDirectories = new HashMap<>();


	// Initialize configuration by loading properties from a config file
	public void initializeConfiguration() throws IOException, InvalidFormatException {
		String timestamp = dateformat.format(new Date());
		String folderName = "TestReports_" + timestamp;
	    String baseReportDirectory = Paths.get(
	        System.getProperty("user.dir"), 
	        "Reports", 
	        folderName
	    ).toString();

	    // Ensure the base directory exists
	    File baseDir = new File(baseReportDirectory);
	    if (!baseDir.exists()) {
	        if (!baseDir.mkdirs()) {
	            throw new IOException("Failed to create base report directory: " + baseReportDirectory);
	        }
	    }
		excelBrowserConfig = new ExcelDataOperations("./TestData.xlsx");

		try {
			List<Map<String, String>> configData = excelBrowserConfig.getTestDataWithFlag("BrowserConfig", "X");

			if (configData.isEmpty()) {
				throw new IOException("No valid configuration data found in Excel file.");
			}

			for (Map<String, String> config : configData) {
				String testGroupName = config.get("TestGroupName");
				timestamp = dateformat.format(new Date());

				// Check if a folder for this configuration already exists in the map
				if (!configDirectories.containsKey(testGroupName)) {
					// Generate a unique folder for this configuration
					String configDirectory = Paths
							.get(System.getProperty("user.dir") + "/Reports", folderName, testGroupName + "_" + timestamp).toString();
					File dir = new File(configDirectory);

					if (!dir.exists() && !dir.mkdirs()) {
						throw new IOException("Failed to create directory for configuration: " + testGroupName);
					}

					// Save the folder path in the map
					configDirectories.put(testGroupName, configDirectory);
				}

				// Use the folder path for this configuration
				baseDirectory = configDirectories.get(testGroupName);

				// Initialize browser configurations
				String browserName = config.get("Browser");
				boolean headless = Boolean.parseBoolean(config.get("Headless"));
				initializeBrowser(browserName, headless);

				// Navigate to URL if provided
				String url = config.get("URL");
				if (url != null && !url.isEmpty()) {
					page.navigate(url);
				} else {
					System.out.println("URL is missing or empty in the configuration for TestGroup: " + testGroupName);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Failed to initialize configuration from Excel file.");
		}
	}

	// Initialize Playwright browser
	public void initializeBrowser(String browserName, boolean isHeadless) {
		playwright = Playwright.create();
		if (browserName.equalsIgnoreCase("Chrome")) {
			browser = playwright.chromium()
					.launch(new BrowserType.LaunchOptions().setChannel("chrome").setHeadless(isHeadless));
		} else if (browserName.equalsIgnoreCase("Firefox")) {
			browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(isHeadless));
		} else {
			System.out.println("Unsupported browser: " + browserName);
			return;
		}
		context = browser.newContext();
		page = context.newPage();
	}


	// Initialize ExtentReports once for all features
	public void initializeReportsOnce() {
		if (reports == null) {
			String reportPath = Paths.get(baseDirectory).toString();

			reports = new ExtentReports();
			reports.setAnalysisStrategy(AnalysisStrategy.CLASS);

			spark = new ExtentSparkReporter(reportPath);
			reports.attachReporter(spark);

			spark.config().setDocumentTitle("Cucumber Automation Test Execution Report");
			spark.config().setReportName("Cucumber ExtentSpark Report");
		}
	}

	// Create or reuse feature-level parent test
	public void createFeatureParentTest(String feature) {
		if (!featureParentTests.containsKey(feature)) {
			ExtentTest parentTest = reports.createTest(feature);
			featureParentTests.put(feature, parentTest);
		}
	}

	// Get parent test for a feature
	public ExtentTest getFeatureParentTest(String feature) {
		return featureParentTests.get(feature);
	}

	// Start the Extent test for a specific scenario
	public void startExtentParentTest(String feature, String scenarioName) {
		ExtentTest parentTest = getFeatureParentTest(feature);
		if (parentTest != null) {
			extentParentTest = parentTest.createNode(scenarioName);
		}
	}

	// Take a screenshot and return the file path
	public String takeScreenShot() {
		String fileName = UUID.randomUUID() + ".png";
		String filePath = Paths.get(baseDirectory, fileName).toString();

		page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filePath)).setFullPage(true));
		return filePath;
	}

	// Log pass scenario in ExtentReports with screenshots in a separate column
	public void LOGPASS(String expected, String actual, String elementName) {
		String screenshotPath = takeScreenShot();
		String screenshotHtml = "<table style='width:100%'><tr>" + "<td style='width:50%; vertical-align: top;'>"
				+ expected + " " + elementName + " - " + actual + "</td>"
				+ "<td style='width:50%; text-align: center;'>" + "<a href='" + screenshotPath + "' target='_blank'>"
				+ "<img src='" + screenshotPath + "' style='width:150px; height:auto;' /></a></td></tr></table>";

		extentParentTest.pass(screenshotHtml);
	}

	// Log fail scenario in ExtentReports with screenshots in a separate column
	public void LOGFAIL(String expected, String actual, String elementName, Exception e) {
		String screenshotPath = takeScreenShot();
		String screenshotHtml = "<table style='width:100%'><tr>" + "<td style='width:50%; vertical-align: top;'>"
				+ expected + " " + elementName + " - " + actual + "<br/>" + e.getMessage() + "</td>"
				+ "<td style='width:50%; text-align: center;'>" + "<a href='" + screenshotPath + "' target='_blank'>"
				+ "<img src='" + screenshotPath + "' style='width:150px; height:auto;' /></a></td></tr></table>";

		extentParentTest.fail(screenshotHtml);
	}

	// Cleanup browser and context after execution
	public void tearDown() {
		if (browser != null) {
			browser.close();
		}
		if (playwright != null) {
			playwright.close();
		}
	}

	// Getter for the page object
	public static Page getPage() {
		return page;
	}
}
