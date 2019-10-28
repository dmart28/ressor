package quickstart;

import xyz.ressor.Ressor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickStart {

    public static void main(String[] args) {
        var bookService = Ressor.service(BookRepository.class)
                .fileSource("classpath:quickstart/data.json")
                .jsonList(Book.class)
                .build();

        System.out.println(bookService.getTitle("0679760806"));
    }

    public static class Book {
        public String isbn;
        public String title;
    }

    public static class BookRepository{
        private final Map<String, String> data = new HashMap<>();

        public BookRepository(List<Book> node) {
            node.forEach(b -> data.put(b.isbn, b.title));
        }

        public String getTitle(String isbn) {
            return data.get(isbn);
        }
    }

}
