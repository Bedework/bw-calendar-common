# bw-calendar-common
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
