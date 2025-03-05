import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;

public class MultiThreadedWebCrawlerJSON {
    private static final int MAX_THREADS = 5; 
    private static final int MAX_PAGES = 20; 
    private static final String JSON_FILE = "crawled_data.json"; 

    private final ExecutorService executorService;
    private final Queue<String> urlQueue;
    private final Set<String> visitedUrls;
    private final List<Map<String, String>> crawledData;
    private final CountDownLatch latch;

    public MultiThreadedWebCrawlerJSON() {
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.urlQueue = new ConcurrentLinkedQueue<>();
        this.visitedUrls = ConcurrentHashMap.newKeySet(); 
        this.crawledData = loadExistingData(); 
        this.latch = new CountDownLatch(1); 
    }

    public void startCrawling(String startUrl) {
        urlQueue.add(startUrl);
        while (!urlQueue.isEmpty() && visitedUrls.size() < MAX_PAGES) {
            String url = urlQueue.poll();
            if (url != null && !visitedUrls.contains(url)) {
                visitedUrls.add(url);
                executorService.submit(() -> {
                    crawlPage(url);
                    latch.countDown(); 
                });
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Crawling interrupted: " + e.getMessage());
        }

        // After the initial crawling, keep checking if new URLs have been added to the queue
        while (!urlQueue.isEmpty() && visitedUrls.size() < MAX_PAGES) {
            String url = urlQueue.poll();
            if (url != null && !visitedUrls.contains(url)) {
                visitedUrls.add(url);
                executorService.submit(() -> {
                    crawlPage(url);
                    latch.countDown(); // Decrement latch after completing a task
                });
            }
        }

        shutdown();
    }

    private void crawlPage(String url) {
        try {
            System.out.println("Crawling: " + url);
            Document doc = Jsoup.connect(url).get();

            // Extracting metadata
            String title = doc.title();
            String description = doc.select("meta[name=description]").attr("content");
            Elements headers = doc.select("h1, h2, h3");

            // Store extracted data
            saveToJSON(url, title, description, headers);

            // Extract and queue new URLs (BFS approach)
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String newUrl = link.absUrl("href");
                if (!visitedUrls.contains(newUrl) && newUrl.startsWith("http")) {
                    urlQueue.add(newUrl);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to crawl: " + url + " - " + e.getMessage());
        }
    }

    private void saveToJSON(String url, String title, String description, Elements headers) {
        Map<String, String> entry = new HashMap<>();
        entry.put("url", url);
        entry.put("title", title);
        entry.put("description", description);
        entry.put("headers", headers.text());
        entry.put("timestamp", new Date().toString());

        crawledData.add(entry);
        try (Writer writer = new FileWriter(JSON_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(crawledData, writer);
            System.out.println("Saved to JSON: " + url);
        } catch (IOException e) {
            System.err.println("Failed to save JSON: " + e.getMessage());
        }
    }

    private List<Map<String, String>> loadExistingData() {
        File file = new File(JSON_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(JSON_FILE)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("Failed to load JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        System.out.println("Crawling complete. Visited " + visitedUrls.size() + " pages.");
    }

    public static void main(String[] args) {
        String startUrl = "https://crawlbase.com/blog/"; // Change this to the target website
        MultiThreadedWebCrawlerJSON crawler = new MultiThreadedWebCrawlerJSON();
        crawler.startCrawling(startUrl);
    }
}