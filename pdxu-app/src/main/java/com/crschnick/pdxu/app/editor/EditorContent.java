package com.crschnick.pdxu.app.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class EditorContent {

    private EditorState state;
    private EditorNavPath.NavEntry navigation;
    private List<Integer> pageSizes;
    private List<EditorNode> allNodes;
    private List<EditorNode> filteredNodes;
    private ObjectProperty<List<EditorNode>> shownNodes;

    public EditorContent(EditorState state) {
        this.state = state;
        this.pageSizes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.filteredNodes = new ArrayList<>();
        this.shownNodes = new SimpleObjectProperty<>();
    }

    private void rebuildPageSizes() {
        pageSizes.clear();
        for (int i = 0; i < filteredNodes.size(); i += EditorSettings.getInstance().pageSize.getValue()) {
            int size = Math.min(filteredNodes.size() - i, EditorSettings.getInstance().pageSize.getValue());
            pageSizes.add(size);
        }
    }

    private void rebuildEditorNodes() {
        if (navigation.getEditorNode() == null) {
            allNodes = new ArrayList<>(state.getRootNodes().values());
        } else {
            allNodes = navigation.getEditorNode().expand();
        }
        filteredNodes = state.getFilter().filter(allNodes);
    }

    private boolean goToPage(int newPage) {
        if (newPage > pageSizes.size() - 1) {
            return false;
        }

        if (newPage < 0) {
            return false;
        }

        boolean front = newPage > navigation.getPage();
        if (front) {
            navigation.scrollProperty().set(0.0);
        } else {
            navigation.scrollProperty().set(1.0);
        }
        navigation.pageProperty().set(newPage);
        int offset = pageSizes.subList(0, navigation.getPage()).stream().mapToInt(Integer::intValue).sum();
        int length = pageSizes.get(navigation.getPage());
        shownNodes.set(filteredNodes.subList(offset, offset + length));
        return true;
    }

    public void changeScroll(double s) {
        navigation.scrollProperty().set(s);
    }

    public void previousPage() {
        goToPage(navigation.getPage() - 1);
    }

    public void nextPage() {
        goToPage(navigation.getPage() + 1);
    }

    public boolean canGoToPreviousPage() {
        return navigation.getPage() > 0;
    }

    public boolean canGoToNextPage() {
        return navigation.getPage() < pageSizes.size() - 1;
    }

    public void basicContentChange() {
        rebuildPageSizes();

        int oldPage = navigation.getPage();
        if (!goToPage(oldPage)) {
            goToPage(0);
        }
    }

    public void completeContentChange() {
        rebuildEditorNodes();
        rebuildPageSizes();

        int oldPage = navigation.getPage();
        if (!goToPage(oldPage)) {
            goToPage(0);
        }
    }

    public void navigate(EditorNavPath.NavEntry navEntry) {
        this.navigation = navEntry;
        this.completeContentChange();
    }

    public double getScroll() {
        return navigation.getScroll();
    }

    public List<EditorNode> getShownNodes() {
        return shownNodes.get();
    }

    public ObjectProperty<List<EditorNode>> shownNodesProperty() {
        return shownNodes;
    }
}
