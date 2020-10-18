LyricsTagger
============
Identify and add missing lyrics to your music library.

REQUIREMENTS
------------
- Java 8 and up
- Internet connection
- API Key for Genius.com (free)

CONFIGURATION
-------------
- Create an `application.yml` under resources
    ```
    genius:
      apiClient:
        id: your-client-id
        secret: your-client-secret
        accessToken: your-access-token
    ```    
- Fill in your personal API Keys

INSTALLATION Mac OS X
---------------------
- Create disk image (DMG): `mvn clean package appbundle:bundle`
- Open image and move app to Applications folder

USAGE
-----
1. Add path to your music library
2. Find lyrics for found audio file in library
3. Write found lyrics as tag into each audio file




