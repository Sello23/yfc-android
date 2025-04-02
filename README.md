<p align="center"> 
    <a href="https://admin-panel-react-qa.yourfitness.coach/">
        <img width="280px" height="280px" src=".gitlab/images/logo.png" alt="YFC logo"/><br/>
    </a>
</p> 
<h1 align="center">YFC (Android)</h1>

---

### Common information
* The project is based on a single activity. 
* All content is implemented through fragments. 
* The project uses the MVI architecture. Fragments communicate with the view model through intents, 
  and view models send various states to the fragments
* The application uses DI with help of the Android Hilt library
* Room database is used in the application.
* Communication with the server side occurs through the API requests using the okHttp and retrofit libraries.
* All asynchronous work is done using coroutines.
* The Stripe service is used to make payments.
* The project uses Android View Binding via "viewbindingpropertydelegate" library by Kirill Rozov
* In addition to the local database, the application receives some settings from the Firebase remote config
* The app users have two roles 
   - simple user
   - personal trainer 
* The project has 5 modules 
   - app (main module)
   - common (common functionality for all modules)
   - shop
   - community
   - pt (personal trainers)
* The app has two flavours
   - qa
   - prod
* There is no any CI/CD for this project. All builds are compiled on the local machine
* All logic classes are stored in the "domain" folders
* All network classes are stored in the "network" folders
* All local database classes are stored in the "data" folder

---

## [PERMISSIONS](./app/src/main/AndroidManifest.xml)
 - **Location** permission (asked on the progress screen)
 - **Notifications** permission (asked on the progress screen)
 - **Activity recognition** permission (asked when user tries to connect the GoogleFit app) 

## API

Network config classes:

| Class                                                                                                   |                                                 Description                                                  |                                                                                                                                                                                                                                                                 
|---------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------:|
| [`YFCAuthenticator`](./common/src/main/java/com/yourfitness/common/network/YFCAuthenticator.kt)         |                            updates the token if the request receives a 401 error                             |
| [`AuthInterceptor`](./common/src/main/java/com/yourfitness/common/network/AuthInterceptor.kt)           |                                  adds a header with a token to all requests                                  |
| [`YFCSecureInterceptor`](./common/src/main/java/com/yourfitness/common/network/YFCSecureInterceptor.kt) | adds special encrypted headers to all requests so that the server can filter out third-party hacker requests |
| [`YFCErrorInterceptor`](./common/src/main/java/com/yourfitness/common/network/YFCErrorInterceptor.kt)   |             tries to redirect the request several times if received a 429 error from the server              |

---

## ANALYTICS

[The app has minor firebase and facebook analytics](./common/src/main/java/com/yourfitness/common/domain/analytics/AnalyticsManager.kt)

---

## BASE CLASSES

[`MviFragment`](./common/src/main/java/com/yourfitness/common/ui/mvi/MviFragment.kt)
[`MviViewModel`](./common/src/main/java/com/yourfitness/common/ui/mvi/MviViewModel.kt)
[`MviDialogFragment`](./common/src/main/java/com/yourfitness/common/ui/mvi/MviDialogFragment.kt)
[`MviBottomSheetDialogFragment`](./common/src/main/java/com/yourfitness/common/ui/mvi/MviBottomSheetDialogFragment.kt)

- Base classes for all fragments to implement mvi logic
- Contain accompanying additional basic logic

---

## LOGIC CLASSES

 - [`BackendFitnessDataRepository`](./app/src/main/java/com/yourfitness/coach/domain/fitness_info/BackendFitnessDataRepository.kt)
   * used to work with fitness data that is synchronized with the server
   * saving, updating the status of records
   * obtaining certain data for display on ui  
 - [`ProviderFitnessDataRepository`](./app/src/main/java/com/yourfitness/coach/domain/fitness_info/ProviderFitnessDataRepository.kt)
   * used to work with data received from a fitness application
   * saves and sends data, including data that should be sent to the server  
 - [`GoogleFitRepository`](./app/src/main/java/com/yourfitness/coach/domain/google_fit/GoogleFitRepository.kt)
   * checks and requests permissions to access the device’s fitness data
   * used to obtain fitness data from Google Fit
   * filtering manual data
   * distribution of data by day, taking into account the time zone of the region  
 - [`CloudMessagingRepository`](./app/src/main/java/com/yourfitness/coach/domain/fcm/CloudMessagingRepository.kt)
   * used to receive and delete a firebase push token for sending notifications from the server  
 - [`FacilityRepository`](./app/src/main/java/com/yourfitness/coach/domain/facility/FacilityRepository.kt)
   * downloads from the server and saves gyms, studios, personal trainers
   * downloads the necessary related data, such as information about the balance of purchased sessions from personal trainers
   * searches, filters, sorts data for display on ui
   * loads and sorts personal trainers for the corresponding gyms
   * configures the default state of filters
   * works with location and calculates the distance to the gyms  
 - [`FirebaseRemoteConfigRepository`](./app/src/main/java/com/yourfitness/coach/domain/fb_remote_config/FirebaseRemoteConfigRepository.kt)
   * establishes a connection with firebase
   * receives the necessary data for the application configuration, which is stored remotely
   * automatically updates data when it changes on the firebase side

 - [`LocationRepository`](./common/src/main/java/com/yourfitness/common/domain/location/LocationRepository.kt)
   * used to obtain the location of the device
 - [`ProfileRepository`](./common/src/main/java/com/yourfitness/common/domain/location/ProfileRepository.kt)
   * used to load, save and retrieve data from the user profile
 
 - [`BlockRepository`](./pt/src/main/java/com/yourfitness/pt/domain/pt/BlockRepository.kt)
   * used to work with blocked time slots 
     > findConflicts - scans all occupied slots and looks for time intersections with input data (considering the number of weekly repetitions)
 - [`DashboardRepository`](./pt/src/main/java/com/yourfitness/pt/domain/dashboard/DashboardRepository.kt)
   * used only for the role of pt
   * loads information for the main personal trainer screen
   * completes free trial sessions
 - [`PtRepository`](./pt/src/main/java/com/yourfitness/pt/domain/pt/PtRepository.kt)
   * used to load data from another module necessary for display in the current module, such as information about the halls
   * configures working hours for halls
   * used to work with data about personal trainers (downloading from the server, saving, reading, searching)
   * used to work with the balance of purchased sessions from personal trainers
   * works with information about free trial workouts
   * works with information about trainers with whom the user has interacted
 - [`SessionsRepository`](./pt/src/main/java/com/yourfitness/pt/domain/calendar/SessionsRepository.kt)
   * used for everything related to sessions with personal trainers (both from the user’s side and from the personal trainer’s side)
   * loads, saves, changes session states
   * works with time slots blocked by the trainer
   * saves intermediate states of working with the booking calendar
   * configures data for specific time periods supported on the screen
 - [`UserCalendarRepository`](./pt/src/main/java/com/yourfitness/pt/domain/calendar/UserCalendarRepository.kt)
   * configures booking calendar time slots for display on ui
 
 - [`CartRepository`](./shop/src/main/java/com/yourfitness/shop/domain/orders/CartRepository.kt)
   * used to work with recycle bin objects
   * basic functions of saving, deleting, updating, reading
 - [`OrderRepository`](./shop/src/main/java/com/yourfitness/shop/domain/orders/OrderRepository.kt)
   * used to download, store and display purchase history in the shop
   * performs basic filtering and sorting
 - [`ProductsRepository`](./shop/src/main/java/com/yourfitness/shop/domain/products/ProductsRepository.kt)
   * used to load, save and display products for each store category
   * uses the last synchronization time to request changes to the product list
   * processes and stores products marked as favorites
   * searches for products and sorts them
   * prepares default filters
   * filters products depending on their category and user settings
 - [`FriendsProfileRepository`](./community/src/main/java/com/yourfitness/community/domain/FriendsProfileRepository.kt)
   * used to search for users (with pagination function)
   * sending requests to other users, accepting/rejecting requests from other users, deleting from friends
   * for working with details of other profiles, loading and displaying basic data
   * processing workout likes

 - [`FitnessInfoService`](./app/src/main/java/com/yourfitness/coach/domain/fitness_info/FitnessInfoService.kt)
   * used to perform an algorithm for periodically updating fitness data, using it locally and synchronizing with the server
   * used to generate a file with information from a local database to be sent to Firebase

 - [`PaymentService`](./common/src/main/java/com/yourfitness/common/domain/payment/PaymentService.kt)
   * used for making payments through the stripe service using a secret key from the server and processing the result

 - [`PrepareCartDataService`](./shop/src/main/java/com/yourfitness/shop/domain/cart_service/PrepareCartDataService.kt)
   * used to configure information about the cost, quantity, used coins in the user’s cart before purchasing

 - [`LeaderboardManager`](./app/src/main/java/com/yourfitness/coach/domain/leaderboard/LeaderboardManager.kt)
   * used to download data from the server for different types of leaderboards with pagination function
 - [`PointsManager`](./app/src/main/java/com/yourfitness/coach/domain/progress/points/PointsManager.kt)
   * used to download data from the server about the user’s accrued points, configure data for the level system and to calculate new accruals for various repositories (for example, for the configuration of reward screens)
 - [`ProfileManager`](./app/src/main/java/com/yourfitness/coach/domain/ProfileManager.kt)
   * used to load the user profile and update data in it

 - [`RegionSettingsManager`](./common/src/main/java/com/yourfitness/common/domain/settings/RegionSettingsManager.kt)
   * used to download from the server and save regional settings that affect a large amount of third-party data in the application
   * updated no more than once in a specified duration value or by mandatory requirement
 - [`SettingsManager`](./app/src/main/java/com/yourfitness/coach/domain/settings/SettingsManager.kt)
   * used to download from the server and save global settings that affect a large amount of third-party data in the application
   * updated no more than once in a specified duration value or by mandatory requirement
 - [`SubscriptionManager`](./app/src/main/java/com/yourfitness/coach/domain/subscription/SubscriptionManager.kt)
   * used to work with user subscription
   * updates the subscription status from the server, deletes it locally when it expires
   * determines whether there is access to rooms depending on the availability of a subscription
   * updated no more than once in a specified duration value or by mandatory requirement

---

<h1 align="center">REQUESTS USAGE</h1>

### MAIN 
  - **Pre login/signup screen**
    - GET [base_url]/healthz/startup (will not allow any actions to be taken if an error is returned + will show the user a notification)

  - **Enter phone screen (login/signup)**
    - GET [base_url]/profile/check-phone?phone_number=%2B375000000002
    - GET [base_url]/auth/create-code?phoneNumber=%2B375000000002&otpSender=sms
    - GET [base_url]/auth/resend-code?otpID=abbdbe88-8532-4b0d-93c5-f8f1a18e1f2c&phone_number=%2B971203004000&otpSender=whatsapp

  - **Enter sms code screen**
    - GET [base_url]/auth/verify-code?code=000000&otpID=a39ef04f-f429-4eef-89d1-f2c20dc38784&phone_number=%2B375000000002

  - **Requests before app content loading**
    - POST [base_url]/auth/login

  - **Requests before app content loading (log in)**
   _All requests are needed to build UI on the start screen_
    - GET [base_url]/profile/subscription 
    - GET [base_url]/settings/region
    - GET [base_url]/profile
    - GET [base_url]/settings/global
    - POST [base_url]/profile (update push token)

  - **Registration enter email screen**
    - GET [base_url]/profile/check-email?email=dc%40f.vfp

  - **Registration add profile photo screen**
    - POST [base_url]/auth/login (creates account)
    - POST [base_url]/media (uploads photo bytes)

  - **Requests before app content loading (sign up)**
   _All requests are needed to build UI on the start screen_
    - POST [base_url]/profile (update push token)
    - GET [base_url]/profile
    - GET [base_url]/settings/region
    - GET [base_url]/settings/global

  - **Enter voucher code (after registration)**
    - GET [base_url]/code?code=FD (checks code type to decide what flow to use)
      - PUT [base_url]/profile/corporation
      - PUT [base_url]/profile/subscription-voucher
      - POST [base_url]/voucher/redeem
        - POST [base_url]/auth/refresh-token (needed to receive correct challenges after code applying)

  - **Progress screen**
    - GET [base_url]/profile/coins
    - GET [base_url]/v2/facility (needed to find nearest gym by tapping fab)
    - GET [base_url]/friends/workout/likes/1699992000 (sends when user taps on any date on the calendar, but only if there is no data for this date)
    - GET [base_url]/progress-level/all
    - GET [base_url]/personal-trainer-session/balance/user-list (needed for pt shortcut)

  - **Profile screen**
    - GET [base_url]/settings/global (optional - loads if data is old)
    - GET [base_url]/profile/subscription (optional - loads if data is old)
    - GET [base_url]/profile/coins
    - GET [base_url]/settings/region (optional - loads if data is old)
  - **Subscription screen**
    - GET [base_url]/profile/subscription
    - GET [base_url]/subscription/v2/price
    - GET [base_url]/profile (needed to fetch 100% actual params that used in subscription flow)
  - **Payment screen**
    - GET [base_url]/profile/cards (can be only 1 saved card)
    - POST [base_url]/subscription/v2/create
  - **Shop orders screen**
    - GET [base_url]/order/goods/list
    - GET [base_url]/order/vouchers/list
  - **Payment history screen**
    - GET [base_url]/profile/payments
  - **Challenge screen**
    - GET [base_url]/challenge/joined?limit=0&offset=0
    - GET [base_url]/challenge/available?limit=0&offset=0
  - **Leaderboards**
    _(Join)_
    - POST [base_url]/challenge/8b7397e9-a815-4c59-acbc-63f2858370de/join
    - POST [base_url]/auth/refresh-token (needed to receive correct list of available challenges after joining or leaving to some specific challenges)
    _(Leave)_
    - POST [base_url]/challenge/8b7397e9-a815-4c59-acbc-63f2858370de/leave

    - GET [base_url]/challenge/dubai3030/global-with-rank?limit=20&offset=0
    - GET [base_url]/challenge/dubai3030/global-with-rank?limit=20&offset=20
    ...

    - GET [base_url]/challenge/dubai3030/corporate?limit=20&offset=0&type=private
    - GET [base_url]/challenge/dubai3030/corporate?limit=20&offset=0&type=government
    ...

    - GET [base_url]/challenge/8b7397e9-a815-4c59-acbc-63f2858370de/leaderboard-with-rank?limit=20&offset=0&period=all-time
    - GET [base_url]/challenge/8b7397e9-a815-4c59-acbc-63f2858370de/leaderboard-with-rank?limit=20&offset=20&period=all-time
    ...

### COMMUNITY

  - **Likes block (progress screen)**
    - GET [base_url]/friends/workout/likes/1699992000  
  - **Friends and requests screen**
    - GET [base_url]/friends/list
    - GET [base_url]/friends/requests-in  
  - **Search screen**
    - GET [base_url]/friends/search?limit=20&offset=0&searchText=opus
    - PUT [base_url]/friends/requests/send/19075c12-b011-4bb2-871b-13ef1ee3c529
    - PUT [base_url]/friends/requests/accept/f3abbd9c-fa4a-4f34-996d-6a80eb3eeb2f
    - PUT [base_url]/friends/requests/f3abbd9c-fa4a-4f34-996d-6a80eb3eeb2f  
  - **Friend details screen**
    - GET [base_url]/friends/activities/0407d81a-993f-496c-b893-68d88acf9b4b
    - PUT [base_url]/friends/activities/like (on screen exit)
    - DELETE [base_url]/friends/f3abbd9c-fa4a-4f34-996d-6a80eb3eeb2f (unfriend)  

### SHOP

  - **Intro screen**
    - GET [base_url]/settings/region (needed to show correct coins equivalent) - one time screen
  - **Categories screen**
    - GET [base_url]/settings/region
    - GET [base_url]/profile/coins
  - **Apparels screen** (similar as others)
    - GET [base_url]/v2/product/apparel (with short names in response for optimization)
    - GET [base_url]/product/favorites (needed to mark some of products as favorites - for example, if user has favorites on the other device)
    - PUT [base_url]/product/favorites/replace (sends on screen exit only, to minify sent requests amount)
  - **Payment options screen**
    - GET [base_url]/profile/cards
    - GET [base_url]/settings/region (needed to fetch 100% actual params that used in subscription flow)
    - POST [base_url]/order/goods
    - GET [base_url]/order/status/1b4e46af-57b4-4aaa-b430-72b77bf41a97 (needed to periodically check whether the order has been paid, since the server does not immediately receive information from the stripe)

### PT

  # USER ROLE
  - **Schedule calendar**
    - GET [base_url]/profile/personal-trainer-session?syncedAt=2023-11-15T23%3A23%3A01Z (all user sessions updates)
    - GET [base_url]/personal-trainer-session/balance/user-list
  - **Booking calendar**
    - GET [base_url]/profile/personal-trainer-session (all user sessions, needed to find out about changes in user sessions that the trainer could make)
    - GET [base_url]/personal-trainer-session?personalTrainerId=3eb8d9f3-4438-4f60-aeb2-6ebb859bf242 (all pt sessions, needed to learn about changes in trainer sessions to display current data)
    - GET [base_url]/personal-trainer-session/balance/user-list
    - POST [base_url]/personal-trainer-session (session booking - only for user role)

  # PT ROLE
  - **Dashboard screen**
    - GET [base_url]/personal-trainer/session (all pt's sessions to display nearest ones)
    - GET [base_url]/personal-trainer/dashboard
  - **Schedule calendar**
    - GET [base_url]/personal-trainer/session?syncedAt=2023-11-15T23%3A27%3A42Z (all pt's sessions changes to display actual data)
    - PUT [base_url]/personal-trainer-session/b256c582-0c40-4fad-b95f-d48204a10eef/status (changes sessions statuses)

---