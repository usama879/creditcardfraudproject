# Part 1: Machine Learning Implementation (Google Colab)

This section explains how to run the **Machine Learning part** of the project, which was implemented in **Google Colab**.

---

## Steps to Run the Colab Notebook

1. **Download** the `creditcardfraud.ipynb` notebook file from this GitHub repository.  
2. **Open Google Colab:** [https://colab.research.google.com/](https://colab.research.google.com/)  
3. **Upload Notebook:** Click on **“Upload Notebook”**, then browse and select the downloaded `creditcardfraud.ipynb` file.  
4. **Run the Notebook:** Once opened, go to **Runtime → Run all** to execute all cells automatically.  
5. **Automatic Setup:** All required libraries will be installed automatically during execution.

![Colab Screenshot](https://drive.google.com/uc?export=view&id=1hnt9azheETgT67whIyGqrAIJDXetN9km)

---

## Description of the ML Workflow

- **Environment Setup:** Install and import all required Python libraries.  
- **Dataset Loading and Exploration:** Load the dataset and view its structure, size, and columns.  
- **Exploratory Data Analysis:** Generate summary statistics and perform visual analysis.  
- **Feature Analysis:** Identify correlations and relationships with the target variable.  
- **Data Preprocessing:** Handle missing values, outliers, and encode categorical variables.  
- **Feature Engineering:** Derive additional useful features such as user age and transaction frequency.  
- **Data Splitting:** Divide data into training, validation, and test sets.  
- **Data Balancing:** Apply SMOTE to balance fraudulent and non-fraudulent classes.  
- **Model Definition and Training:** Train multiple machine learning models for fraud detection.  
- **Model Evaluation:** Evaluate models using accuracy, recall, precision, F1-score, and AUC metrics.  
- **Feature Selection and Hyperparameter Tuning:** Improve model performance and optimize parameters.  
- **Final Model Selection:** Choose the best model based on test results.  
- **Model Conversion:** Save the best model in `.pkl` format and convert it to `.tflite` for Android integration.

---

## Dataset Used for Training and Testing

The dataset used in this project is the **Credit Card Fraud Detection Dataset** available on Kaggle: [https://www.kaggle.com/datasets/kartik2112/fraud-detection](https://www.kaggle.com/datasets/kartik2112/fraud-detection)

When you run the notebook, the dataset is automatically fetched and used for training, testing, and evaluation.

---

## Commands Used for Training and Testing

- All **training**, **evaluation**, and **conversion** commands are included inside the notebook.  
- Simply **run all cells** in Google Colab to perform model training, testing, and **TensorFlow Lite conversion** automatically.  
- **No manual command execution** is required beyond clicking **“Run all.”**


# Part 2: Running the FraudShield Android Application

This section explains how to **set up the environment and run the FraudShield Android App** on your local machine.

---

## Step 1: Install Java Development Kit (JDK)

1. **Download JDK 25** from the official Oracle link:  
   [https://download.oracle.com/java/25/latest/jdk-25_windows-x64_bin.exe](https://download.oracle.com/java/25/latest/jdk-25_windows-x64_bin.exe)
2. **Install JDK** by running the downloaded installer and following the on-screen instructions.  
3. After installation, go to the directory:  
   `C:\Program Files\Java\jdk-25\bin`
4. **Copy this path** to set up the environment variable.
5. **Set Environment Variable Path:**
   - Open **Start Menu → Search “Environment Variables” → Click “Edit the system environment variables.”**
   - In the **System Properties** window, click on **“Environment Variables…”**
   - Under **System variables**, find and select **Path**, then click **Edit**.
   - Click **New**, and **paste** the copied JDK path (`C:\Program Files\Java\jdk-25\bin`).
   - Click **OK** to save the changes.

---

## Step 2: Install Android Studio

1. **Download Android Studio** from the official link:  
   [https://redirector.gvt1.com/edgedl/android/studio/install/2024.2.1.12/android-studio-2024.2.1.12-windows.exe](https://redirector.gvt1.com/edgedl/android/studio/install/2024.2.1.12/android-studio-2024.2.1.12-windows.exe)
2. **Install Android Studio** by running the downloaded file and following the setup wizard.
3. Wait for the installation to complete. This will also install the required Android SDK components automatically.

---

## Step 3: Download and Run the FraudShield App

1. Go to this GitHub repository and **navigate to the `FraudShieldApp` folder**.  
2. Inside it, you will find another folder named **`FraudShield`**.  
3. **Download this folder as a ZIP file** and extract it to your desktop.
4. Open **Android Studio**, then click on **“Open”**.
5. Browse and select the extracted folder named **`FraudShield`** on your desktop.
6. Wait for Android Studio to **sync and build the project** completely.  
7. In case you encounter any Gradle-related errors:
   - Go to **File → Project Structure** in Android Studio.
   - Under the **Gradle Version** section, **set it to version 8.9**.
   - Click **OK** and wait for the build to complete again.

---

Once the build is finished successfully, the **FraudShield App** is ready to be run on your Android physical device and Android studio will show Android folder on top to shwo that proejct is successfully build

![Colab Screenshot](https://drive.google.com/uc?export=view&id=1FSqFQLyJQnp63zVDY0YRXBIE27L6kSOk)


## Part 3: Running the App on an Android Real Device

Follow the steps below to run the FraudShield app on a real Android device.

1. **Connect Devices to the Same Wi-Fi Network**  
   Make sure your **laptop** and **Android phone** are connected to the **same Wi-Fi network**.

2. **Check Android Version**  
   Your Android device should be running **API level 33 (Android 13)** or higher.

3. **Enable Developer Options**  
   - On your Android device, go to **Settings → About phone**.  
   - Tap **Build number** seven times to enable **Developer Options**.  
   - You will see a message confirming that **Developer Options** are now enabled.

4. **Enable Wireless Debugging**  
   - Go to **Settings → Developer options**.  
   - Turn on the **Developer options** toggle (if it’s off).  
   - Scroll down and enable **Wireless debugging**.  
   - Tap **Pair device with QR code** — this will open a QR scanner on your device.

5. **Pair Device in Android Studio**  
   - Open **Android Studio** on your laptop.  
   - Click on **No Devices** (at the top toolbar).  
   - Select **Pair Devices Using Wi-Fi** — a QR code will appear.  
   - Use your Android phone (from the previous step) to scan this QR code.

![Colab Screenshot](https://drive.google.com/uc?export=view&id=1LPQZ_lEraYSTaoRp0-NsYVmXWbtFnmTi)
  

6. **Wait for Successful Connection**  
   Once pairing is successful, Android Studio will display a **green checkmark** confirming the connection and you will see real device name instead of no devices in top center of android studio menu bar.

7. **Run the App**  
   - Click on the **Run ▶️** button in Android Studio.  
   - The **FraudShield app** will automatically launch on your real Android device.

![Colab Screenshot](https://drive.google.com/uc?export=view&id=1iw_s7wnTbkb5wcwMmU3l9GS-fG6sm4N7)


## Part 4: Using the FraudShield App to Test Model Functionality

---

### Steps to Test the App

1. **Enable Location** on your real Android device before opening the app.

2. Open the **FraudShield App** and create a new account by entering:
   - **Full Name**
   - **Email**
   - **Password** (must include **1 uppercase**, **1 or more than 1 lowercase**, **1 number**, and **1 special symbol**)
   - **Phone Number**

3. After creating an account, **log in** using your credentials.

4. On the **Dashboard**, tap **“Allow Current Location”** when prompted.

5. Tap **“Add Transaction”** to create a new transaction record.

6. Enter the transaction details:
   - **Amount**
   - **Merchant Type**
   - **Card Number** (for testing, use dummy/test card numbers from [BlueSnap Test Cards](https://developers.bluesnap.com/reference/test-credit-cards))
   - **City Name**, **Latitude**, and **Longitude** (auto-detected; you can also enter manually)
   - **Transaction Date and Time**

7. Tap **“Save Transaction”** to store the entry in the app.

8. To test fraud detection, go to **“Fraud Detection”** on the dashboard.

9. Select the transaction you just added — the **ML model** will run in the background and predict whether the transaction is **fraudulent** or **legitimate**.

10. If a transaction is detected as **fraudulent**, go to the **“Alerts”** section → tap **“View All Alerts”** → and choose **“Take Action”** to block card of fraud transactions to prevent further fraudulent transactions.


