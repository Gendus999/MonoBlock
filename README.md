# Block Blast 🎮

Moderná, prémiová verzia obľúbenej hry **Block Blast** pre Android vytvorená v **Jetpack Compose** a **Kotlin**.

---

## 📲 Rýchla inštalácia (APK)

Pre okamžité nainštalovanie do telefónu:
1. Otvorte priečinok **[`release/`](./release/)**
2. Stiahnite súbor **[`BlockBlast.apk`](./release/BlockBlast.apk)**
3. V telefóne súbor otvorte a potvrďte inštaláciu (povoľte inštaláciu z neznámych zdrojov, ak sa zobrazí výzva).

---

## ✨ Hlavné funkcie

- 🕹️ **Klasická 8×8 mriežka**: Ukladajte bloky, vytvárajte komba a čistite riadky a stĺpce.
- 🧲 **Magnetické prichytávanie (Snapping)**: Extrémne presné umiestňovanie dielikov priamo pod prstom s inteligentnou asistenciou na okrajoch dosky.
- 🌟 **Extraordinary Blocks (Voliteľné v Nastaveniach)**:
  - Možnosť zapnúť špeciálne 3×3 tvary (C-tvary, U-tvary, dutý rám, kríž +, diagonály a kladivá).
  - V predvolenom stave (OFF) je hra v klasickom režime s tradičnými tvarmi a štandardným 3×3 plným blokom.
- 📱 **Výhradne Portrait režim**: Hra je uzamknutá na výšku pre pohodlné hranie jednou rukou.
- 📳 **Haptická odozva**: Pocit fyzického umiestnenia bloku a výbuchu radov.
- 🏆 **Ukladanie najvyššieho skóre**: Rekord sa ukladá lokálne a pretrváva medzi hrami.
- 🎨 **Moderný dark-mode dizajn**: Gradienty, neónové farby blokov, animácie a častice pri čistení línií.

---

## 🛠️ Zostavenie projektu zo zdrojových kódov

Ak chcete projekt zostaviť manuálne:
```bash
gradle :app:assembleDebug
```
Vygenerovaný APK súbor nájdete v `app/build/outputs/apk/debug/app-debug.apk`.
