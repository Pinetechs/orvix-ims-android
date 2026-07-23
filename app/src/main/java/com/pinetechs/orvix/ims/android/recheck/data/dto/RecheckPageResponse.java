package com.pinetechs.orvix.ims.android.recheck.data.dto;

import java.util.Collections;
import java.util.List;

public class RecheckPageResponse {
    private List<RecheckRequestResponse> content;
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private boolean empty;

    public List<RecheckRequestResponse> getContent() {
        return content == null ? Collections.emptyList() : content;
    }

    public void setContent(List<RecheckRequestResponse> content) {
        this.content = content;
    }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}
