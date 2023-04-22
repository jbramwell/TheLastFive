#!/usr/bin/env bash
#
# Replace config settings prior to build

# This is the path to the app's gradle.properties file (containing the config settings to be modified)
GRADLE_PROPERTIES_FILE=$APPCENTER_SOURCE_DIRECTORY/gradle.properties

if [ -e "$GRADLE_PROPERTIES_FILE" ]
then
    echo "Updating _APP_CENTER_API_KEY_ API Key..."
    sed -i -e "s/_APP_CENTER_API_KEY_/$APP_CENTER_API_KEY/g" $GRADLE_PROPERTIES_FILE

    echo "Updating _CREATE_TEST_DATA_ Test Data flag..."
    sed -i -e "s/_CREATE_TEST_DATA_/$CREATE_TEST_DATA/g" $GRADLE_PROPERTIES_FILE

    # cat $GRADLE_PROPERTIES_FILE
fi