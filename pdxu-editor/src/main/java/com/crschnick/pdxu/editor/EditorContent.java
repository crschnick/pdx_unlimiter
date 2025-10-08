package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.editor.node.EditorNode;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EditorContent {

    public static List<Integer> calculatePageSizes(int nodesSize) {
        var pageSize = AppPrefs.get().editorPageSize().getValue();
        List<Integer> pageSizes = new ArrayList<>();
        for (int i = 0; i < nodesSize; i += pageSize) {
            int size = Math.min(nodesSize - i, pageSize);
            pageSizes.add(size);
        }
        return pageSizes;
    }

    private EditorState state;
    private int page;
    private double scroll;
    private EditorNode node;
    private List<Integer> pageSizes;
    private List<EditorNode> allNodes;
    private List<EditorNode> filteredNodes;
    private ListProperty<EditorNode> shownNodes;

    public EditorContent(EditorState state) {
        this.state = state;
        this.pageSizes = new ArrayList<>();
        this.allNodes = new ArrayList<>();
        this.filteredNodes = new ArrayList<>();
        this.shownNodes = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public synchronized void withFixedContent(Consumer<EditorContent> c) {
        c.accept(this);
    }

    private void rebuildPageSizes() {
        var pageSize = AppPrefs.get().editorPageSize().getValue();
        pageSizes.clear();
        for (int i = 0; i < filteredNodes.size(); i += pageSize) {
            int size = Math.min(filteredNodes.size() - i, pageSize);
            pageSizes.add(size);
        }
    }

    private void rebuildFilteredEditorNodes() {
        filteredNodes = state.getFilter().filter(allNodes);
    }

    private void rebuildEditorNodes() {
        if (node == null) {
            allNodes = new ArrayList<>(state.getRootNodes().values());
        } else {
            allNodes = node.expand();
        }
        rebuildFilteredEditorNodes();
    }

    private boolean goToPage(int newPage) {
        // If there are no nodes to show
        if (newPage == 0 && pageSizes.size() == 0) {
            this.page = newPage;
            shownNodes.clear();
            return true;
        }

        if (newPage > pageSizes.size() - 1) {
            return false;
        }

        if (newPage < 0) {
            return false;
        }

        this.page = newPage;
        int offset = pageSizes.subList(0, page).stream().mapToInt(Integer::intValue).sum();
        int length = pageSizes.get(page);

        // Ugly but necessary in case the editor nodes didn't change but the content values did
        shownNodes.clear();
        shownNodes.setAll(filteredNodes.subList(offset, offset + length));

        return true;
    }

    private int getViewIndex() {
        if (pageSizes.size() == 0) {
            return 0;
        }

        if (getPage() >= pageSizes.size()) {
            return 0;
        }

        int index = 0;
        for (int i = 0; i < getPage(); i++) {
            index += pageSizes.get(i);
        }
        return index + (int) Math.floor(getScroll() * pageSizes.get(getPage()));
    }

    private double getViewShare() {
        if (filteredNodes.size() == 0) {
            return 0.0;
        }

        int focusedNode = getViewIndex();
        return (double) focusedNode / filteredNodes.size();
    }

    private void goToViewShare(double vs) {
        if (vs == 0.0 || pageSizes.size() == 0) {
            changeScroll(0.0);
            goToPage(0);
            return;
        }

        int index = (int) (vs * filteredNodes.size());
        int sum = 0;
        for (int page = 0; page < pageSizes.size(); page++) {
            if (sum + pageSizes.get(page) > index) {
                double scroll = (double) (index - sum) / pageSizes.get(page);
                if (!goToPage(page)) {
                    changeScroll(0.0);
                    goToPage(0);
                    return;
                } else {
                    changeScroll(scroll);
                }
                return;
            }

            sum += pageSizes.get(page);
        }

        goToPage(pageSizes.size() - 1);
        changeScroll(1.0);
    }

    public synchronized void changeScroll(double s) {
        this.scroll = s;
    }

    public synchronized EditorNavLocation navigateAndFocus(EditorNavPath path, EditorNode focus) {
        this.node = path.getEditorNode();
        rebuildEditorNodes();
        rebuildPageSizes();
        var newLoc = EditorNavLocation.nodeInFocus(filteredNodes, allNodes.size(), path, focus);
        goToPage(newLoc.page());
        changeScroll(newLoc.scroll());
        return newLoc;
    }

    public synchronized void previousPage() {
        changeScroll(1.0);
        goToPage(getPage() - 1);
    }

    public synchronized void nextPage() {
        changeScroll(0.0);
        goToPage(getPage() + 1);
    }

    public synchronized boolean canGoToPreviousPage() {
        return getPage() > 0;
    }

    public synchronized boolean canGoToNextPage() {
        return getPage() < pageSizes.size() - 1;
    }

    public synchronized void filterChange() {
        double vs = getViewShare();
        rebuildFilteredEditorNodes();
        rebuildPageSizes();
        goToViewShare(vs);
    }

    public synchronized void completeContentChange() {
        double vs = getViewShare();
        rebuildEditorNodes();
        rebuildPageSizes();
        goToViewShare(vs);
    }

    public synchronized void navigate(EditorNode node, int page, double scroll) {
        if (node != null && !node.isValid()) {
            throw new IllegalArgumentException("Node is not valid");
        }

        this.node = node;
        this.page = page;
        this.scroll = scroll;

        rebuildEditorNodes();
        rebuildPageSizes();
        if (!goToPage(getPage())) {
            changeScroll(0.0);
            goToPage(0);
        }
    }

    public synchronized int getPage() {
        return page;
    }

    public synchronized double getScroll() {
        return scroll;
    }

    public synchronized List<EditorNode> getShownNodes() {
        return shownNodes.get();
    }

    public synchronized ListProperty<EditorNode> shownNodesProperty() {
        return shownNodes;
    }
}
