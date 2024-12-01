# 🌱 **BloomBotanica - Plant Care App**

### By [Joao Pedro Marques](https://github.com/jp-marques), [Ethan D'Mello](https://github.com/ethan-dmello), [Evan Ellig](https://github.com/ellig0130), [Arhan Ikram](https://github.com/arhanikram), and [Zorah Jawadi](https://github.com/ZJawadi)


**BloomBotanica** is an Android app designed to simplify plant care for busy individuals who struggle to keep track of the unique needs of each plant. From succulents to flowering plants and cacti, each plant has its own specific requirements to stay healthy and vibrant. BloomBotanica aims to be a reliable assistant for plant owners, providing timely reminders and an organized interface that ensures each plant receives the right care at the right time. With BloomBotanica, users can easily manage watering schedules, view a history of plant care, and record observations for each plant, helping prevent common care mistakes and keeping their plants happy and thriving.

---

## 📋 **Project Status**

### Currently Implemented

- **Onboarding**:
  - The app begins with an onboarding screen where users enter their name, which is saved in `SharedPreferences` for personalized messages.
- **Dashboard**:
  - A `DashboardFragment` displays a welcome message that includes the user’s name and a RecyclerView list of daily plant care tasks.
  - (__*Layout is there but tasks and backend are not yet implemented*__)
- **Plant List**:
  - The `PlantsFragment` displays plants in a RecyclerView as square cards in a grid layout. Users can add new plants to their collection, which are saved in the database, and view all plants in the list. Users can also delete plants from their collection.
  - Editing and reordering features are not yet implemented.
- **Calendar View**:
  - `CalendarFragment` integrates a calendar view that highlights dates with scheduled plant care tasks
  - (__*not fully implemented, only have calendar with placeholder icons*__).
- **Persistent Data Storage**:
  - Utilizes a Room database to save user-added plants and their care schedules, ensuring data persists across app sessions.
- **Simple Plant Profile**:
  - A dedicated view for each plant displays its nickname and an image. While users cannot yet view the care schedule or care history, the backend for watering date updates is nearly complete.
- **Add Plant**:
  - Users can add plants from the Plant List, choosing a plant nickname and selecting what plant it is based on our Plant Care Database.
- **Plant Suggestions**:
  - Provides name suggestions when adding a plant, querying a database of plant information.
  - Filters and prioritizes suggestions based on the user’s input.
- **Mark Plant as Watered**:
  - Updates the plant's last watered and next watering date when users mark a plant as watered. The backend implementation is fully functional.
- **Bottom Navigation**:
  - `MainActivity` provides bottom navigation, allowing smooth switching between Dashboard, Plants, and Calendar fragments.

## 🚀 **Planned Features (Not Yet Implemented)**

### **Core Features**
- **Detailed Plant Profiles**:
  - Enhance the plant profile view to include editing options, task completion marking, and more detailed information.
- **Edit Plant Details**:
  - Enable users to edit a plant's nickname, care schedule, soil type, and other editable attributes (based on the design document).
- **Task Notifications**:
  - Push notifications to remind users of essential plant care tasks, including daily summaries and follow-ups for missed tasks.
- **Calendar Integration**:
  - Link water schedules and other tasks to the calendar. Highlight days with tasks, and allow users to view tasks for a specific date.
- **Dashboard Enhancements**:
  - Add sections for overdue tasks, today’s tasks, completed tasks, and weather updates.
- **Plant List Enhancements**:
  - Prioritize plants with tasks to-do and put them above plants that have no pending tasks in the plant list.

### **Advanced Features**
- **Plant Timeline and Journal**:
  - A unified feature to track past care tasks (e.g., watering, fertilizing) and allow users to log notes, observations, and photos for each plant over time.
- **Historical Logs**:
  - Provide logs of completed tasks for specific plants or an overall view for all plants.
- **Dynamic Task Adjustments**:
  - Automatically adjust schedules if a user misses a task (e.g., rescheduling overdue tasks).
- **Weather Integration**:
  - Incorporate weather data to offer tailored care recommendations based on conditions like humidity and temperature, as well as displaying in dashboard.

### **General Improvements**
- **Settings**:
  - Add a settings page for customization options like notification preferences and task scheduling.
- **Dark Theme**:
  - Implement a dark mode for better user accessibility and aesthetics.
- **UI Refinements**:
  - Polish the user interface to improve overall usability and aesthetics.
- **Tasks Across All Sections**:
  - Ensure each area (Dashboard, Calendar, Plant List) supports task management, allowing users to complete tasks from anywhere in the app.
- **Multilingual Support**

---
