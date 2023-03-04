import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Tema2 {
    public static void main(String[] args) throws IOException {
        String ordersFileStr = args[0] + "/orders.txt";
        String productsFileStr = args[0] + "/order_products.txt";


        String productsOutStr ="order_products_out.txt";
        String ordersOutStr = "orders_out.txt";


        int threadsNr=Integer.parseInt(args[1]);

        File ordersFile = new File(ordersFileStr);
        File productsFile = new File(productsFileStr);

        File productsOut=new File(productsOutStr);
        File ordersOut=new File(ordersOutStr);


        /*delete all contents of file*/
        PrintWriter writer = new PrintWriter(ordersOutStr);
        writer.print("");
        writer.close();

        writer = new PrintWriter(productsOutStr);
        writer.print("");
        writer.close();


        BufferedReader brOrders = new BufferedReader(new FileReader(ordersFile));


        //thread pool
        ExecutorService executor= Executors.newFixedThreadPool(threadsNr);
        ExecutorService executorProducts= Executors.newFixedThreadPool(threadsNr);


        Runnable processor= (Runnable) new GetOrderTask(brOrders,productsFile, executor,executorProducts, threadsNr,productsOut,ordersOut);
        executor.execute(processor);

    }

}
