package quickstart;

import com.fasterxml.jackson.databind.JsonNode;
import xyz.ressor.Ressor;

import java.util.HashMap;
import java.util.Map;

public class QuickStart {

    public static void main(String[] args) {
        var bookService = Ressor.service(BookRepository.class)
                .fileSource("classpath:quickstart/data.json")
                .json()
                .build();

        System.out.println(bookService.getTitle("0679760806"));
    }

    public static class BookRepository{
        private final Map<String, String> data = new HashMap<>();

        public BookRepository(JsonNode node) {
            if (node != null) {
                node.forEach(n -> data.put(n.get("isbn").asText(), n.get("title").asText()));
            }
        }

        public String getTitle(String isbn) {
            return data.get(isbn);
        }
    }

}
