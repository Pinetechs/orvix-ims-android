package com.pinetechs.orvix.ims.android.workarea.data.dto;

import java.util.List;

public class WorkAreaSliceResponse {
    private List<WorkAreaResponse> content;
    private int number;
    private int size;
    private int numberOfElements;
    private boolean last;

    public List<WorkAreaResponse> getContent() { return content; }
    public void setContent(List<WorkAreaResponse> content) { this.content = content; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
