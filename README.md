# 🌱 **BloomBotanica - Plant Care App**

### By [Joao Pedro Marques](https://github.com/jp-marques), [Ethan D'Mello](https://github.com/ethan-dmello), [Evan Ellig](https://github.com/ellig0130), [Arhan Ikram](https://github.com/arhanikram), and [Zorah Jawadi](https://github.com/ZJawadi)

**BloomBotanica** is an Android app designed to simplify plant care for enthusiasts, beginners, and casual gardeners. Whether you have one succulent or an entire collection of plants, BloomBotanica helps you manage essential care tasks efficiently. With personalized reminders, an intuitive interface, and a historical log of completed tasks, users can ensure their plants stay healthy and thriving.

---

<img src="https://github.com/user-attachments/assets/d327cebf-e856-4e3f-bdf9-23cdedf65a4c" alt="Onboarding" width="400"/>
<img src="https://github.com/user-attachments/assets/4e75986b-32fd-479d-be7f-127d69dd0fa4" alt="Home" width="400"/>
<img src="https://github.com/user-attachments/assets/7a96f5d2-4931-43f9-8e34-f7428cf56227" alt="HomePopulated" width="400"/>
<img src="https://github.com/user-attachments/assets/e42b9f41-698d-46be-9e90-6bb6558ffd7a" alt="Calendar" width="400"/>
<img src="https://github.com/user-attachments/assets/0e54a931-6987-42b0-9d92-2b6c77f9d8f4" alt="Prediction" width="400"/>
<img src="https://github.com/user-attachments/assets/fc038599-79cc-48c0-a0a6-0189f6b8a86f" alt="MyPlants" width="400"/>
<img src="https://github.com/user-attachments/assets/62e80e1e-c0f9-4374-a35b-1351ad6b2f19" alt="PlantProfile" width="400"/>
<img src="https://github.com/user-attachments/assets/05a7a914-8ee8-46a1-908a-c2eae8e94fab" alt="Journal" width="400"/>
<img src="https://github.com/user-attachments/assets/d954f652-b7e3-41f3-9634-a21ed74bb353" alt="NewLog" width="400"/>
<img src="https://github.com/user-attachments/assets/0c63ff5e-8533-4a82-ae60-faf61e19e8fe" alt="Settings" width="400"/>

---

## 📋 **Project Status**

### **Current Features**

- **Onboarding**:
  - Welcome screen where users enter their name and preferred language (saved using `SharedPreferences`).

- **Dashboard**:
  - Displays personalized greetings with the user's name.
  - Shows care reminders for plants scheduled for today.

- **Plant List**:
  - Add, view, edit, and delete plants.
  - Displays all added plants in a clean layout.
  - Supports uploading plant photos and displaying them.

- **Plant Details**:
  - Detailed view for each plant, including:
    - Nickname, species name, description, last watered date, and care history.
    - Users can upload journal entries and observations with photos.

- **Care Reminders**:
  - Users can set notifications for watering and rotating plants.
  - Completed reminders are logged in the plant’s historical journal.

- **Historical Logs**:
  - Users can view completed care tasks and add personal journal entries for plants.

- **Calendar Integration**:
  - A calendar view highlights dates with upcoming plant care tasks.

- **Plant Identification**:
  - Upload a photo to identify plant species using an AI model based on PyTorch EfficientNet architecture with **94% accuracy**.
  - Real-time, on-device species identification and care suggestions.

- **Dark Mode**:
  - Supports light and dark themes for enhanced user experience.

- **Persistent Data Storage**:
  - Uses a Room database to store plant information, reminders, and user entries, ensuring data persists across app sessions.

- **Notifications**:
  - Users receive reminders for scheduled care tasks (watering, rotating, etc.).

- **Settings Page**:
  - Includes customization options for notifications, theme preferences, and user information.

---

## 🌿 **Tech Stack**
- **Languages**: Java
- **Frameworks**: Android Studio, ONNX Runtime (AI Model Integration)
- **Database**: Room Database
- **Testing**: JUnit, Espresso
- **AI Model**: PyTorch EfficientNet (converted to ONNX for mobile compatibility)
- **Version Control**: Git and GitHub

---

## 📊 **Contributors**

- [Joao Pedro Marques](https://github.com/jp-marques)
- [Ethan D'Mello](https://github.com/ethan-dmello)
- [Evan Ellig](https://github.com/ellig0130)
- [Arhan Ikram](https://github.com/arhanikram)
- [Zorah Jawadi](https://github.com/ZJawadi)
