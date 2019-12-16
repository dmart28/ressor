package examples;

import xyz.ressor.Ressor;
import xyz.ressor.source.s3.S3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3SourceExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var invoiceRepository = ressor.service(InvoiceRepository.class)
                .source(S3.builder().region("us-east-2").build())
                .resource(S3.object("ressor-examples", "invoices.yaml"))
                .yamlList(Invoice.class)
                .build();

        System.out.println(invoiceRepository.find(120));

        ressor.shutdown();
    }

    public static class InvoiceRepository {
        private Map<Integer, Invoice> invoices = new HashMap<>();

        public InvoiceRepository(List<Invoice> invoices) {
            invoices.forEach(i -> this.invoices.put(i.invoice, i));
        }

        public Invoice find(int invoice) {
            return invoices.get(invoice);
        }
    }

    public static class Invoice {
        public int invoice;
        public String client;
        public List<Item> items;

        @Override
        public String toString() {
            return "Invoice{" +
                    "invoice=" + invoice +
                    ", client='" + client + '\'' +
                    ", items=" + items +
                    '}';
        }
    }

    public static class Item {
        public int item;
        public double unitvalue;
        public int units;

        @Override
        public String toString() {
            return "Item{" +
                    "item=" + item +
                    ", unitvalue=" + unitvalue +
                    ", units=" + units +
                    '}';
        }
    }

}
