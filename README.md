# webpay-token-android

webpay-token-android is a Android library for creating a WebPay token from a credit card.

## Requirements

Android 2.2(API Level 8) and above

## Installation

Retrieving from `mavenCentral()` repository.

In your Android project's `build.gradle` file:


```
dependencies {
  compile 'jp.webpay.android:webpay-token:1.0.0@aar'`
}
```

## Sample application

You can try a sample application downloading from Google Play: https://play.google.com/store/apps/details?id=jp.webpay.android.token.sample

It's including a sample for integration with [card.io](https://www.card.io/).

## How to use

webpay-token provides:

- Button switching by result of creating token
- User interface for inputting user's credit card information
- Client for creating token

You can use whichever of them by your application as necessary.

### Button and payment form

1. Implement WebPayTokenCompleteListener in your Activity

```diff
+ public class MyFragmentActivity implements WebPayTokenCompleteListener {
- public class MyFragmentActivity {
```

```java
@Override
public void onTokenCreated(Token token) {
    // do when Token created
}

@Override
public void onCancelled(Throwable lastException) {
    // do when error raised
}
```

2. Add FrameLayout to place of indicating button

```xml
<FrameLayout
    android:id="@+id/webpay_token_button_fragment"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

3. Call to replace id of above FrameLayout with WebPayTokenFragment

```java
WebPayTokenFragment tokenFragment = WebPayTokenFragment.newInstance(WEBPAY_PUBLISHABLE_KEY);
getFragmentManager().beginTransaction()
    .replace(R.id.webpay_token_button_fragment, tokenFragment)
    .commit();
```

See also: [sample/TokenCreateActivity](https://github.com/webpay/webpay-token-android/blob/master/sample/src/main/java/jp/webpay/android/token/sample/TokenCreateActivity.java)

### Payment form

You can call the form creating Token directly.
So, you need to implement the trigger calling this form on your own.

1. Implement WebPayTokenCompleteListener in your Activity

```diff
+ public class MyFragmentActivity implements WebPayTokenCompleteListener {
- public class MyFragmentActivity {
```

```java
@Override
public void onTokenCreated(Token token) {
    // do when Token created
}

@Override
public void onCancelled(Throwable lastException) {
    // do when error raised
}
```

2. Call CardDialogFragment directly

```java
// You can specify supporting card types manually
List<CardType> supportedCardTypes = CardType.VM();

CardDialogFragment fragment = CardDialogFragment.newInstance(WEBPAY_PUBLISHABLE_KEY, supportedCardTypes);
fragment.setSendButtonTitle(R.string.your_button_title);
fragment.show(getFragmentManager(), CARD_DIALOG_FRAGMENT_TAG);
```

See also: [sample/CardDialogActivity](https://github.com/webpay/webpay-token-android/blob/master/sample/src/main/java/jp/webpay/android/token/sample/CardDialogActivity.java)

### Client library

You can also create Token using WebPay class directly.
But it's necessary to implement user interface by your own.

```java
RawCard rawCard = new RawCard()
    .number(cardNumber)
    .expMonth(cardExpMonth)
    .expYear(cardExpYear)
    .cvc(cardCvc)
    .name(cardName);


new WebPay(WEBPAY_PUBLISHABLE_KEY).createToken(rawCard, new WebPayListener<Token>() {
    @Override
    public void onCreate(Token result) {
        // do when Token created
    }

    @Override
    public void onException(Throwable cause) {
        // do when error raised
    }
});
```

See also: [sample/HandleWebPayActivity](https://github.com/webpay/webpay-token-android/blob/master/sample/src/main/java/jp/webpay/android/token/sample/HandleWebPayActivity.java)

## License

Copyright (c) 2015- WebPay, Inc.

Released under [the MIT license](http://opensource.org/licenses/mit-license.html).
