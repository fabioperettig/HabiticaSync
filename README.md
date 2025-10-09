# 🧙‍♂️ HabiticaSync Project

**HabiticaSync** is a Java-based project currently under development.  
Its main goal is to integrate data from the official **Habitica API** with **Google Sheets**, allowing automatic synchronization of in-game inventory data.

In this first version, the application connects to Habitica’s API and retrieves the user's **Eggs** and **Hatching Potions**, displaying them in a clean and readable console output.

---

## 🚀 Current Features
- Authenticated connection to the official Habitica API  
- Secure environment variable handling (`User ID` and `API Token`)  
- Real-time retrieval of Eggs and Hatching Potions inventory  
- Clear console visualization of the current items  
- Basic HTTP response and error handling  

---

## 🧩 Technologies Used
- **Java 17**  
- **Maven**  
- **org.json** — for parsing JSON responses  
- **Apache HttpClient 5** — for managing HTTP requests  

---

## 🛠️ How to Run the Project
1. Clone this repository
   ````
   git clone https://github.com/fabioperettig/HabiticaSync.git
   ````
2. Set your Habitica credentials as environment variables
    ````
    export HABITICA_USER_ID="your_user_id"
    export HABITICA_API_TOKEN="your_api_token"
    ````
3. Run the project using IntelliJ IDEA or directly via Maven
    ````
    mvn clean compile exec:java -Dexec.mainClass="com.fabio.habiticasync.Main"
    ````

## 🚧 Next Steps
- Add a module to sync data automatically with Google Sheets
- Create a simple local storage system to track inventory changes between sessions
- Improve console formatting and table output
- Build a small GUI dashboard for easier visualization

## 📄 License
Personal learning project — open for study, testing, and contribution.
Feel free to fork and build upon it with proper attribution.

#### Version: 1.0
#### Author: Fabio Peretti Guimarães 🇧🇷
#### Date: October 2025