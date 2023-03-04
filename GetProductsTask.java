import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;


public class GetProductsTask implements Runnable {
    BufferedReader sharedBufferProducts;
    ExecutorService executorProducts;
    String orderId;
    ArrayList<String> productIds;
    File ord_pr_outFile;


    public GetProductsTask(String orderId, BufferedReader buffer, ExecutorService executor, File ord_pr_outFile) {
        this.sharedBufferProducts = buffer;
        this.executorProducts = executor;
        this.orderId = orderId;
        this.productIds = new ArrayList<String>();
        this.ord_pr_outFile = ord_pr_outFile;
    }


    @Override
    public void run() {
        String line = null;
        StringTokenizer tokenStr = null;
        try {
            boolean foundOneProduct = false;
            while (!foundOneProduct) {
                line = sharedBufferProducts.readLine();
                if (line == null) {
                    break;
                } else {
                    tokenStr = new StringTokenizer(line, ",");
                    String orderId = tokenStr.nextToken();
                    String prId = tokenStr.nextToken();


                    if (orderId.equals(this.orderId)) {
                        foundOneProduct = true;

                        synchronized (this) {
                            this.productIds.add(prId);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Reading line.");
        }
    }
}
