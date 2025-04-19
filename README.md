# bw-calendar-common [![Build Status](https://travis-ci.org/Bedework/bw-util2.svg)](https://travis-ci.org/Bedework/bw-util2)
Common classes for bedework calendar client and engine modules. 

## Requirements

1. JDK 17
2. Maven 3

## Building Locally

> mvn clean install

## Releasing

Releases of this fork are published to Maven Central via Sonatype.

To create a release, you must have:

1. Permissions to publish to the `org.bedework` groupId.
2. `gpg` installed with a published key (release artifacts are signed).

To perform a new release use the release script:

> ./bedework/build/quickstart/linux/util-scripts/release.sh <module-name> "<release-version>" "<new-version>-SNAPSHOT"

When prompted, indicate all updates are committed

For full details, see [Sonatype's documentation for using Maven to publish releases](http://central.sonatype.org/pages/apache-maven.html).

## Release Notes
### 4.0.0
* Initial commit. Create new common project with modules annotations, convert and facade
* Add ESTIMATED-DURATION support
* Pass class loader as parameter when creating new objects. JMX interactions were failing.

### 4.1.0
* Update library versions
* Fix creation of combined values. Would miss all leading field delimiters.
* Redo the packing of attendee parameters in sent by field and add email
* Simplify the configuration utilities.
* Add a concept entity with a couple of methods and use it.
* Add a primaryCollection flag to allow us to locate the actual main calendar collection.
* Add a do not use flag to locations
* Need to manufacture href for indexer
* Fix a recurrence instance bug in which invalid recurrenceids would be allowed.
  Shows up when the url to fetch an instance is edited. Made it appear instances were not being deleted.
* Add information to the view data so that we can sort on the displayed field rather than the path.
* Add code to query opensearch to get scroll context info.
* Remove some throws clauses. Don't wrap exceptions in RunTimeException.
* Try not invalidating the session. There are multiple requests and this may cause errors in the one that got through
* Removed bw-xml module

### 4.1.1
* Update library versions

### 4.1.2
* Update library versions
* Add dependencies to the project parent to ensure all transitive dependencies during builds of this project resolve to project.version and NOT the version set in bedework-parent

### 4.1.3
* Update library versions
* Fix bad gtext for vpoll sub-component
* Fix handling of UNTIL element in recurrences when converting to/from xml
* Changes for vpoll participant use
* Changes needed for immutable code in ical4j
* Xutil treated NumberList as a String list - fix that.
* Missing checks for absent values
* Added a refresh operation to the synch engine and added associated code to the client side.
* Remove (nearly) all old references to recurrence instances in db. Some comments need updating.
* Fix content type
* Check for null collection. Can happen if we do propfind and get non-existant special collection.
* New class to handle event participants.
* Small fix - call getParticipantsSet()
* Add an attendee object we will use to handle attendee represented as either ical ATTENDEE and/OR PARTICIPANT.
* Make webdavexception subclass of runtimeexception and tidy up a bit. Should be no noticable changes.
* Redo set/getSupportedComponents in BwCollection and remove explicit setting in other code.
* Add, but don't process, participants with no calendar address.
* Improve owner check.
* Mostly switch to using BwParticipants and Attendee objects to manipulate event attendees and participants.
* Get rid of SchedulingInfo.
* Add a class to represent votes. Remove unused and incorrect poll item id class.
* Mostly add code to correctly save and restore participants in the indexer.
* Use getParticpantAddrs for BwCalDAVEvent.getAttendeeUris
* Update mailer to send correctly formed messages.
* Split jdkim into api and library. Use api only as a dependency. Obtain library by dynamic loading.

### 4.1.4
* Update library versions
* dd url for new event reg web service to configs. Use it in client to post notifications.
* Complete the unspringing of event reg.
* Remove all unnecessary refs to CalFacadeException.
  Move error codes out of CalFacadeException into CalFacadeErrorCode.
* Move most of the exceptions into the new bw-base module.
* Move response classes and ToString into bw-base module.
* Many bw-database related changes
* Remove ref to javax.xml
* Pre-jakarta release
