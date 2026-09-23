# Fuel Cost Helper — Setup Guide

## Ye app kya karta hai
1. Aap vehicle mileage (km/l), current fuel price, aur Google Maps API key ek baar daalte ho.
2. Accessibility Service ON karne par app Zomato/Swiggy delivery-partner app ki screen padhta hai.
3. Jab order screen pe pickup + drop address dikhe, app Google Directions API se distance nikaalta hai aur mileage ke hisaab se fuel cost calculate karke ek chhota overlay dikhata hai.

## Build karne ke 2 tareeke

### Tareeka A: GitHub Actions (Android Studio ki zaroorat NAHI, sirf browser)
1. https://github.com pe free account banao (agar nahi hai).
2. Naya **public ya private repository** banao (koi bhi naam, jaise "FuelCostApp").
3. Is poore `FuelCostApp` folder ka saara content us repo mein upload karo —
   GitHub website pe "Add file > Upload files" se seedha drag-drop kar sakte ho
   (`.github` folder bhi zaroor upload karna, ye hidden dikh sakta hai apne
   file manager mein — "show hidden files" ON karo).
4. Upload ho jaane ke baad repo ke andar **"Actions"** tab pe click karo.
5. "Build APK" naam ka workflow dikhega — usme click karke **"Run workflow"**
   button dabao.
6. 2-5 minute wait karo (green tick aane tak).
7. Green tick wale run pe click karo, neeche **"Artifacts"** section mein
   "app-debug-apk" milega — usko download karke zip extract karo, andar
   APK file hogi. Wahi APK phone mein copy karke install kar lo.

### Tareeka B: Android Studio (agar upar wala try karna ho)
1. Android Studio install karo (agar nahi hai): https://developer.android.com/studio
2. Ye poora `FuelCostApp` folder Android Studio mein **Open** karo.
3. Gradle sync hone do (internet chahiye, Android Studio khud kar lega).
4. `Build > Build Bundle(s) / APK(s) > Build APK(s)` se APK bana lo, ya phone connect karke Run dabao.

## Google Maps API Key kaise le
1. https://console.cloud.google.com pe jao, naya project banao.
2. "Directions API" enable karo.
3. Credentials mein API key generate karo.
4. App ke MainActivity screen mein wo key daal do.
   (Free tier limited hai — zyada use pe billing lag sakti hai, Google Cloud console mein check karo.)

## SABSE ZAROORI STEP — text-matching adjust karna
Real Zomato/Swiggy delivery-partner app ka UI text exactly kaisa dikhta hai, ye
mujhe pata nahi (har app version mein badalta rehta hai). Maine
`OrderAccessibilityService.kt` mein `extractPickupAndDrop()` function mein ek
starting-point regex diya hai jo "Pickup:", "Drop:", "Restaurant:", "Customer:"
jaise keywords dhundta hai.

Isko sahi karne ke liye:
1. App install karke Accessibility Service ON karo, ek real order accept karo.
2. Android Studio ke **Layout Inspector** ya **Accessibility Scanner** app se
   dekho ki order screen pe exact kaun se text/labels hain.
3. `extractPickupAndDrop()` ke regex patterns ko us exact text ke hisaab se
   update karo.

## Permissions jo manually enable karni padengi (app ke andar buttons diye hain)
- **Accessibility Service**: Settings > Accessibility > Fuel Cost Helper > ON
- **Display over other apps**: overlay dikhane ke liye zaroori hai

## Important limitations
- Play Store pe publish nahi ho sakta (dusre app ka screen content padhna
  policy violate karta hai) — sirf apne phone mein sideload/personal use ke liye.
- Zomato/Swiggy app update hone par text-matching logic dobara adjust karni
  pad sakti hai.
- Directions API ko exact pickup/drop coordinates milna zaroori hai — agar
  screen pe sirf area ka naam ho (poora address nahi), distance thoda
  approximate ho sakta hai.
