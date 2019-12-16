package quickstart;

import xyz.ressor.Ressor;
import xyz.ressor.source.git.GitRepository;

import java.util.List;

public class GitRepositorySourceExample {

    public static void main(String[] args) {
        var ressor = Ressor.create();

        var uiManager = ressor.service(UIManager.class)
                .source(GitRepository.remote().repositoryURI("https://github.com/dmart28/ressor.git").build())
                .resource(GitRepository.path("examples/src/main/resources/git/menu.json", "develop"))
                .json(Window.class)
                .build();

        System.out.println(uiManager.getMenuLabel("OriginalView"));

        ressor.shutdown();
    }

    public static class UIManager {
        private Window window;

        public UIManager(Window window) {
            this.window = window;
        }

        public String getMenuLabel(String itemId) {
            return window.menu.items.stream()
                    .filter(i -> i.id.equals(itemId))
                    .map(i -> i.label)
                    .findFirst().orElse(null);
        }

    }

    public static class Window {
        public Menu menu;
    }

    public static class Menu {
        public String header;
        public List<MenuItem> items;
    }

    public static class MenuItem {
        public String id;
        public String label;
    }

}
