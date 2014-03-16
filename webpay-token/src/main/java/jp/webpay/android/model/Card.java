package jp.webpay.android.model;

import lombok.Getter;

public class Card {

    @Getter private String number;
    @Getter private Integer expYear;
    @Getter private Integer expMonth;
    @Getter private String name;
    @Getter private String cvc;

}
