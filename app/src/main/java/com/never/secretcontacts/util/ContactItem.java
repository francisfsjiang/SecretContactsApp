package com.never.secretcontacts.util;

public class ContactItem {

    public int type;
    public int inner_type;
    public String content;

    public ContactItem(int type, int inner_type, String content) {
        this.type = type;
        this.inner_type = inner_type;
        this.content = content;
    }

    public enum ItemType {
        PHONE(0), EMAIL(1), ADDRESS(2), MEMO(3);
        private final int value_;
        ItemType(int value) {
            this.value_ = value;
        }
        public int getValue() {
            return value_;
        }
    }
    public enum PhoneType {
        PERSON(0),
        COM(1),
        HOME(2),
        FAX(3),
        OTHER(4);
        private final int value_;
        PhoneType(int value) {
            this.value_ = value;
        }
        public int getValue() {
            return value_;
        }
    }
    public enum EmailType {
        PERSON(0),
        COM(1),
        OTHER(2);
        private final int value_;
        EmailType(int value) {
            this.value_ = value;
        }
        public int getValue() {
            return value_;
        }
    }
    public enum AddressType {
        COM(0),
        HOME(1),
        OTHER(2);
        private final int value_;
        AddressType(int value) {
            this.value_ = value;
        }
        public int getValue() {
            return value_;
        }
    }
    public enum MemoType {
        MEMO(0),
        OTHER(1);
        private final int value_;
        MemoType(int value) {
            this.value_ = value;
        }
        public int getValue() {
            return value_;
        }
    }

}
