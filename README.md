# 🌱 **BloomBotanica - Plant Care App**

### By [Joao Pedro Marques](https://github.com/jp-marques), [Ethan D'Mello](https://github.com/ethan-dmello), Evan Ellig, [Arhan Ikram](https://github.com/arhanikram), and Zorah Jawadi


**BloomBotanica** is an Android app designed to simplify plant care by providing users with a personalized dashboard, a calendar for task tracking, and a dedicated plant management interface. The app's vision is to be a reliable assistant for plant owners, reminding them of essential plant care tasks and organizing plant-related information in a user-friendly way.

### OR

**BloomBotanica** is an Android app designed to simplify plant care for busy individuals who struggle to keep track of the unique needs of each plant. From succulents to flowering plants and cacti, each plant has its own specific requirements to stay healthy and vibrant. BloomBotanica aims to be a reliable assistant for plant owners, providing timely reminders and an organized interface that ensures each plant receives the right care at the right time. With BloomBotanica, users can easily manage watering schedules, view a history of plant care, and record observations for each plant, helping prevent common care mistakes and keeping their plants happy and thriving.

---

## 📋 **Project Status**

### Currently Implemented

- **Onboarding**: The app begins with an onboarding screen where users enter their name, which is saved in `SharedPreferences` for personalized messages.
- **Dashboard**:
  - A `DashboardFragment` displays a welcome message that includes the user’s name and a RecyclerView list of daily plant care tasks.
  - (__*Layout is there but tasks and backend are not yet implemented*__)
- **Plant List**: The `PlantsFragment` displays plants in a RecyclerView as square cards in a grid layout. Users can add new plants (temporary placeholders for now) and view plants as a list.
- **Calendar View**:
  - `CalendarFragment` integrates a calendar view that highlights dates with scheduled plant care tasks
  - (__*not fully implemented, only have calendar with placeholder icons*__).
- **Bottom Navigation**:
  - `MainActivity` provides bottom navigation, allowing smooth switching between Dashboard, Plants, and Calendar fragments.

### Planned Features (Not Yet Implemented)

- **Persistent Data Storage**: Integration of a Room database to store plant information (e.g., name, care schedule, last watered date) to maintain data across app sessions.
- **Detailed Plant Profiles**: Each plant card will link to a detailed view for specific plant information, including editing options and task completion marking.
- **Timeline of Tasks on Plant Profiles**: Each plant profile will display a timeline of past tasks, such as watering and fertilizing, to help users track plant care history.
- **Plant Journal/Log Book**: A journal feature where users can log notes, observations, and photos for each plant over time.
- **Task Notifications**: Push notifications to remind users of essential plant care tasks like watering, fertilizing, etc.
- **General UI Improvements**: Refinements and polishing of the user interface to improve user experience and app aesthetics.
- **Multilingual Support**

---
