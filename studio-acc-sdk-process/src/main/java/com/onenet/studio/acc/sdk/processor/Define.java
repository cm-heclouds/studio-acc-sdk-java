package com.onenet.studio.acc.sdk.processor;

public class Define {

    private Integer bit;
    private String number0;
    private String number1;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Define)) {
            return false;
        }
        Define define = (Define) obj;
        return bit.equals(define.getBit()) && number0.equals(define.getNumber0()) && number1.equals(define.getNumber1());
    }

    public Integer getBit() {
        return bit;
    }

    public void setBit(Integer bit) {
        this.bit = bit;
    }

    public String getNumber0() {
        return number0;
    }

    public void setNumber0(String number0) {
        this.number0 = number0;
    }

    public String getNumber1() {
        return number1;
    }

    public void setNumber1(String number1) {
        this.number1 = number1;
    }
}
