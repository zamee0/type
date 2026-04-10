<div align="center">

# ⌨️ KEYY
### Typing Speed Test — Desktop Application

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven)
![License](https://img.shields.io/badge/License-Academic-green?style=for-the-badge)

**Test your typing speed. Compete with friends. Track your progress.**

<!-- Add your app screenshot here -->
<!-- ![KEYY Screenshot](screenshots/dashboard.png) -->

</div>

## Installation Guide

### Prerequisites

- [Java JDK 21+](https://www.oracle.com/java/technologies/downloads/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/zamee0/type.git

# 2. Open in IntelliJ IDEA
# File → Open → select the Type folder

# 3. Let Maven load dependencies automatically

# 4. Run the application
# Right-click Launcher.java → Run 'Launcher.main()'
```



---

## Features

| Feature | Description |
|---|---|
|  **Typing Game** | 3 difficulty levels × 4 time options |
|  **Local Multiplayer** | Up to 4 players on the same WiFi |
|  **Statistics** | WPM & accuracy charts for last 15 games |
|  **Leaderboard** | Top 10 players by best WPM |
|  **Profile** | Personal stats and full game history |
|  **Dark Mode** | Full dark/light theme support |
|  **Shortcuts** | `Ctrl+D` to toggle theme anywhere |

---
## Login Page
![Login Page](https://github.com/user-attachments/assets/8d59daed-0efa-4647-8051-66a8116ed3da)
## Dashboard
![Dashboard](https://github.com/user-attachments/assets/4a479e43-129b-4f22-88e5-807f79317b3c)
## Start game 
![Start game](https://github.com/user-attachments/assets/1b5751ee-b4dc-47a1-8c4f-6e34c84b6780)

![Image Alt](https://github.com/user-attachments/assets/5832f762-c413-483a-b310-0d63ff6cab0f)
## Result
![Image Alt](https://github.com/user-attachments/assets/2703bf7c-0526-4948-abd3-ae8037cc70ee)

## Profile
![Image Alt](https://github.com/user-attachments/assets/e894daa4-1666-443f-8c5b-e2eb92777ba3)

## Local Host
![Image Alt](https://github.com/user-attachments/assets/cf880f02-9a03-43bc-9461-6d7549591d9c)

## Multiplayer Result
![Image Alt](https://github.com/user-attachments/assets/ec5db217-7d7c-4bdf-a84d-b2bdd6cadb47)

# Leaderboard
![Image Alt](https://github.com/user-attachments/assets/66ba9f40-c12c-4d81-889e-fc930dbb627b)

## Settings
![Image Alt](https://github.com/user-attachments/assets/7b86765c-83f8-45da-a325-d54de4f1268d)

##  How to Play

### Single Player

1. **Register** a new account or **Login**
2. From the dashboard, click **Start Game**
3. Choose your settings:

```
Difficulty:  Easy  |  Normal  |  Hard
Time:        15s   |   30s    |  60s  |  120s
```

4. Start typing the words shown on screen
5. **Color feedback:**
   - 🟢 **Green** — correct character
   - 🔴 **Red** — wrong character
   - 🟠 **Orange** — corrected after a mistake
6. Results appear when time runs out

### WPM Formula
```
WPM = Words Completed ÷ (Time in seconds ÷ 60)
```

### Accuracy Formula
```
Accuracy = (Error-free characters ÷ Total characters typed) × 100
```
> A character with any error counts against accuracy — even if corrected with backspace.

---

## 👥 Multiplayer

Both players must be on the **same WiFi network.**

### Host a Game
```
Dashboard → Local Host → Host a Game
→ Select difficulty & time
→ Share the Room Code with other players
```

### Join a Game
```
Dashboard → Local Host → Join a Game
→ Enter the Room Code
→ Click Connect → Wait for host to start
```

### How Room Codes Work
The host's IP address is encoded into a unique number:
```
192.168.1.5  →  3232235781
```
The joiner enters this code, it decodes back to the IP, and the connection is made automatically.

### After the Game
- 🥇 **1st place** — Gold podium
- 🥈 **2nd place** — Silver podium
- 🥉 **3rd place** — Bronze podium
- 4th+ players ranked below by WPM

---

## ⚙️ Settings & Shortcuts

### Settings (Dashboard → Settings)

| Option | Description |
|---|---|
|  Dark Mode | Toggle dark / light theme |
|  Backspace | Enable or disable corrections during typing |
|  Clear History | Delete all game history permanently |

### Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Ctrl + D` | Toggle dark / light mode |
| `Backspace` | Delete last character (if enabled) |


## 👨‍💻 About

This project was developed as a university assignment.

<div align="center">

| Developer | Student ID |
|---|---|
| **Abdullah Al Zabir Zamee** | 2405145 |
| **Mahir Morshed** | 2405150 |

*Built with using JavaFX*

</div>
