# Oasis QF

This project is a Grant and Donation service based on XRPL. Users can create Grants, make Donations, and administrators can distribute funding using a quadratic funding method. The grant creation period is one week, and the donation period is one month. The system is composed of a backend and a frontend. The frontend is developed with React, while the backend is developed with Spring and Express.

# Features

This system provides the following features:

* Login(KYC) : Google Account Social Login
 
* Wallet Connection : Scan the QR code using the Xumm app on your smartphone 

* Grant Creation : Users can create new grants. When a grant is created, its status is set to “pending.”

* Donation : Users can make donations to Grants. The system uses EscrowCreate transactions to securely hold the donated funds until the grant period ends. After the grant period concludes, the donations are released to the grant proposer.

* Cancel : Proposers can cancel a grant. Cancellation is only possible when the grant is in the “pending” state. A canceled grant will be updated to the “canceled” state.

* Funding Distribution : Once the grant period ends, the administrator distributes the total matching pool funding across the grants. The distribution applies quadratic funding based on the donated amounts. The status of the grants is updated to “Distributed.”

# Getting Start

## Prerequirement
* node 17+
* java 17+
* Xaman Wallet
  * Open the App Store on your smartphone
    *  iOS users can download the Xumm wallet from the App Store, and Android users can download it from the Google Play Store.
  * Search for Xumm in the App Store
    * Enter “Xumm” in the search bar and search for it.
  * Download the Xumm Wallet app
    * Select the Xumm Wallet app from the search results and download and install it.

## Installing
```
npm install express
npm install xumm
npm install xrpl-client
npm install cors
npm install axios
npm install xrpl
```

## How to Obtain API Key and API Secret for Xumm

* Visit the Xumm Developer Portal
  * Open your web browser and go to the Xumm developer portal at https://apps.xumm.dev/docs.
* Create a New Application
  * On the Xumm developer portal, click on the “Create new application” button.
* Fill in Application Details
  * Enter the required details for your new application (such as application name and description).
* Get API Key and API Secret
  * After creating the application, you will be provided with an API Key and an API Secret. Make sure to store these keys securely, as they will be used to integrate your application with the Xumm platform.
* Secure Your Credentials
  * Keep your API Key and API Secret confidential. Do not share them publicly or hardcode them directly into your application.

# Deployment

SPRING SERVER : http://158.179.169.106:8081/


# Built With

* Backend : Java, Spring, JavaScript, Express
* Frontend : React, Typescript
* Database : Oracle, aws S3
* CI/CD : oracle Cloud, jenkins, git
