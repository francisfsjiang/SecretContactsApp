package com.never.secretcontacts.util;


public class Contact {

    private String name_;   //显示的数据
    private String sortLetters_;  //显示数据拼音的首字母

    public String getName() {
        return name_;
    }
    public void setName(String name) {
        this.name_ = name;
    }
    public String getSortLetters() {
        return sortLetters_;
    }
    public void setSortLetters(String sortLetters) {
        this.sortLetters_ = sortLetters;
    }
}