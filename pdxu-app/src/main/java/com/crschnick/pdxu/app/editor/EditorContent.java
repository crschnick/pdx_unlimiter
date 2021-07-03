package com.crschnick.pdxu.app.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class EditorContent {

    public static List<Integer> calculatePageSizes(List<EditorNode> nodes) {
        List<Integer> pageSizes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i += EditorSettings.getInstance().pageSize.getValue()) {
            int size = Math.min(nodes.size() - i, EditorSettings.getInstance().pageSize.getValue());
            pageSizes.add(size);
        }
        return pageSizes;
    }

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

    private void rebuildFilteredEditorNodes() {
        filteredNodes = state.getFilter().filter(allNodes);
    }

    private void rebuildEditorNodes() {
        if (navigation.getEditorNode() == null) {
            allNodes = new ArrayList<>(state.getRootNodes().values());
        } else {
            allNodes = navigation.getEditorNode().expand();
        }
        rebuildFilteredEditorNodes();
    }

    private boolean goToPage(int newPage) {
        if (newPage > pageSizes.size() - 1) {
            return false;
        }

        if (newPage < 0) {
            return false;
        }

        navigation.setPage(newPage);
        int offset = pageSizes.subList(0, navigation.getPage()).stream().mapToInt(Integer::intValue).sum();
        int length = pageSizes.get(navigation.getPage());
        shownNodes.set(filteredNodes.subList(offset, offset + length));
        return true;
    }

    private double getViewShare() {
        int focusedNode = EditorNavPath.getViewIndex(navigation, pageSizes);
        return (double) focusedNode / filteredNodes.size();
    }

    private void goToViewShare(double vs) {
        int index = (int) (vs * filteredNodes.size());
        var ne = EditorNavPath.createInView(navigation.getEditorNode(), index, pageSizes);
        if (!goToPage(ne.getPage())) {
            changeScroll(0.0);
            goToPage(0);
            return;
        }
        changeScroll(ne.getScroll());
    }

    public void changeScroll(double s) {
        navigation.setScroll(s);
    }

    public void previousPage() {
        navigation.setScroll(1.0);
        goToPage(navigation.getPage() - 1);
    }

    public void nextPage() {
        navigation.setScroll(0.0);
        goToPage(navigation.getPage() + 1);
    }

    public boolean canGoToPreviousPage() {
        return navigation.getPage() > 0;
    }

    public boolean canGoToNextPage() {
        return navigation.getPage() < pageSizes.size() - 1;
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

    public void navigate(EditorNavPath.NavEntry navEntry) {
        this.navigation = navEntry;
        rebuildEditorNodes();
        rebuildPageSizes();
        if (!goToPage(navEntry.getPage())) {
            changeScroll(0.0);
            goToPage(0);
        }
    }

    public int getPage() {
        return navigation.getPage();
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

    public List<Integer> getPageSizes() {
        return pageSizes;
    }
}
