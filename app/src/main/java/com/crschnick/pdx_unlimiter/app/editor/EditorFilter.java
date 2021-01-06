package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.util.List;
import java.util.stream.Collectors;

public class EditorFilter {

    private enum Scope {
        KEY,
        VALUE,
        BOTH
    }

    private String filterString;
    private boolean recursive;
    private boolean caseSensitive;
    private Scope scope;

    private boolean contains(String s) {
        if (caseSensitive) {
            return s.contains(filterString);
        } else {
            return s.toLowerCase().contains(filterString.toLowerCase());
        }
    }

    public List<Node> filter(List<Node> input) {
        return input.stream().filter(n -> {
            if (n instanceof KeyValueNode) {
                var kv = n.getKeyValueNode();
                if ((scope == Scope.KEY || scope == Scope.BOTH) && contains(kv.getKeyName())) {
                    return true;
                }
                if (filter(List.of(kv.getNode())).size() > 0) {
                    return true;
                }
            }

            if (n instanceof ValueNode && (scope == Scope.VALUE || scope == Scope.BOTH)) {
                var v = n.getString();
                return contains(v);
            }
            return false;
        }).collect(Collectors.toList());
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
