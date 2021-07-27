package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.editor.node.EditorNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class EditorContent {

    public static List<Integer> calculatePageSizes(int nodesSize) {
        List<Integer> pageSizes = new ArrayList<>();
        for (int i = 0; i < nodesSize; i += EditorSettings.getInstance().pageSize.getValue()) {
            int size = Math.min(nodesSize - i, EditorSettings.getInstance().pageSize.getValue());
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
            shownNodes.set(List.of());
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
        shownNodes.set(filteredNodes.subList(offset, offset + length));
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

    public void changeScroll(double s) {
        this.scroll = s;
    }

    public EditorNavLocation navigateAndFocus(EditorNavPath path, EditorNode focus) {
        this.node = path.getEditorNode();
        rebuildEditorNodes();
        rebuildPageSizes();
        var newLoc = EditorNavLocation.nodeInFocus(filteredNodes, allNodes.size(), path, focus);
        goToPage(newLoc.page());
        changeScroll(newLoc.scroll());
        return newLoc;
    }

    public void previousPage() {
        changeScroll(1.0);
        goToPage(getPage() - 1);
    }

    public void nextPage() {
        changeScroll(0.0);
        goToPage(getPage() + 1);
    }

    public boolean canGoToPreviousPage() {
        return getPage() > 0;
    }

    public boolean canGoToNextPage() {
        return getPage() < pageSizes.size() - 1;
    }

    public void filterChange() {
        double vs = getViewShare();
        rebuildFilteredEditorNodes();
        rebuildPageSizes();
        goToViewShare(vs);
    }

    public void completeContentChange() {
        double vs = getViewShare();
        rebuildEditorNodes();
        rebuildPageSizes();
        goToViewShare(vs);
    }

    public void navigate(EditorNode node, int page, double scroll) {
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

    public int getPage() {
        return page;
    }

    public double getScroll() {
        return scroll;
    }

    public List<EditorNode> getShownNodes() {
        return shownNodes.get();
    }

    public ObjectProperty<List<EditorNode>> shownNodesProperty() {
        return shownNodes;
    }

    public List<Integer> getPageSizes() {
        return pageSizes;
    }
}
