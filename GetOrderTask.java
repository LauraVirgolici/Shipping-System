import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class GetOrderTask implements Runnable {
    BufferedReader sharedBuffer;
    ExecutorService executor; //shared executor for first level employes
    ExecutorService executorProducts; //shared executor for second level employes
    File productsFile;
    int threadsNr;
    List<Future<?>> futures;
    File orders_outFile;
    File ord_pr_outFile;


    public GetOrderTask(BufferedReader buffer, File productsFile, ExecutorService executor,
                        ExecutorService executorProducts, int threadsNr, File ord_pr_outFile, File orders_outFile) {

        this.sharedBuffer = buffer;
        this.executor = executor;
        this.productsFile = productsFile;
        this.threadsNr = threadsNr;
        this.executorProducts = executorProducts;
        this.futures = new ArrayList<Future<?>>();
        this.ord_pr_outFile = ord_pr_outFile;
        this.orders_outFile = orders_outFile;

    }


    public synchronized void writeToPrOutputFile(GetProductsTask task, String orderId) throws IOException {
        String line = "";

        BufferedWriter writer = new BufferedWriter(new FileWriter(this.ord_pr_outFile, true));

        for (String product : task.productIds) {
            line = "";
            line = orderId + "," + product + ",shipped\n";
            writer.append(line);
    }
        writer.close();
    }


    public synchronized void writeToOrdOutputFile(String orderId, int n) throws IOException {
        if (n != 0) {
            String line = "";
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.orders_outFile, true));
            line = "";
            line = orderId + "," + n + ",shipped\n";
            writer.append(line);
            writer.close();
        }
    }


    @Override
    public void run() {
        String line = null;
        try {
            line = sharedBuffer.readLine();

            GetOrderTask taskOr = new GetOrderTask(this.sharedBuffer, this.productsFile, this.executor,
                    this.executorProducts, this.threadsNr, this.ord_pr_outFile, this.orders_outFile);

            if (line != null)
              executor.submit(taskOr);


            if (line == null) {
                executor.shutdown();
                executorProducts.shutdown();
            } else {
                StringTokenizer tokenStr = null;
                tokenStr = new StringTokenizer(line, ",");
                String orderId = tokenStr.nextToken();
                int nrProd = Integer.parseInt(tokenStr.nextToken());

                if(nrProd!=0)
                {
                    BufferedReader brProducts = new BufferedReader(new FileReader(productsFile));
                    futures.clear();

                    GetProductsTask task = new GetProductsTask(orderId, brProducts, this.executorProducts, this.ord_pr_outFile);

                    for (int i = 1; i <= nrProd; i++) {
                        Future<?> f = executorProducts.submit(task);
                        futures.add(f);
                    }

                    ///check if all tasks(all products) have been found
                    for (Future<?> future : futures)
                        future.get(); // get will block until the future is done


                    if (task.productIds.size() == nrProd) {
                        writeToOrdOutputFile(orderId, nrProd);
                        writeToPrOutputFile(task, orderId);
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Reading line.");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
