# Validation Library
           
## Introduction

This library offers components that facilitate validation of arbitrary objects
and how they are serialized towards a client. It focuses on the wiring of the
proposed patterns and does not contain any concrete validator implementations.


### General Approach

There are two fundamentally different ways how to validate an object:
  
  1. Using [Bean Validations][bean-validations]:
     
     Bean Validations are a validation model available as part of Java EE,
     which adds constraints in the form of annotations placed on a field. There
     are already a bunch of built-in constraints, listed [here][built-in].
     
     What's important to notice is that Bean Validations should be applicable
     without any context, i.e. they shouldn't rely on any database or network
     or service in general but purely rely on the given object to validate.
     
  2. Using Complex Validations:
  
     Validations that rely on services based on our [IValidator][IValidator]
     interface. They are not annotated but applied explicitly in the code.
     These validations are explained more in detail below.
     

### Wiring 

Given the two different approaches, they need to be somehow interconnected and
aligned with a global validation process. That's what we call the 
[pre-processing chain][PreprocessingChain]. In short, the pre-processing chain
takes a number of [pre-processors][preprocessing] and executes them one by one
and collects eventual errors in a [validation context][IValidationContext].
This approach makes sure that all errors can be collected and presented to the
the user, as opposed to an exception that returns on the first error.


## Bean Validations

Bean Validations are quite straight forward. You basically put them on the 
field you want to validate and that's it. See the following examples:

```java
public class UserTO {
 
    @CheckNotNull   // ensure it is not null
    @CheckStringLength(min = 1, max = 50) // ensure its length is within the allowed range
    private String name;
 
    @Valid          // validate an optional sub-object (add @CheckNotNull if it's required)
    private AddressTO address;
 
    @Valid          // validate a list of sub-objects
    @CheckNotEmpty
    private List<ApplicationTO> applications;
}
 
public class AddressTO {
 
    private String street;
    @CheckNotNull   // in a nested structure, you can also apply validations to your sub-objects
    private Integer zipCode;
    @CheckNotNull
    private String city;
}
 
public class ApplicationTO {
 
    @CheckNotNull
    @CheckStringLength(min = 1, max = 255)
    private String name;
}
```

## Complex Validations

As soon as additional services are needed to validate an object, complex
validations come into play. Generally, implementing a complex validation means
creating a class which extends [AbstractValidator][AbstractValidator] and 
implement the `validate()` method. Find below a complete example of how this
would work for a simple user object:

```java
@Stateless
@Path("users")
public class UserResource extends AbstractResource {
 
    @EJB   // inject the session beans needed by your validators
    private IUserDao userDao;
 
    // The custom validator is typed with your TO class.
    protected static class UserTOValidator extends AbstractValidator<UserTO> {
 
        private IUserDao userDao;
 
        // You must get session beans manually; they cannot be automatically injected.
        public UserTOValidator(IUserDao userDao) {
            this.userDao = userDao;
        }
 
        @Override
        protected void validate(UserTO transferObject, IValidationContext context) {
 
            // What you basically do in a validator is add errors to the validation context if you find that the data is invalid.
            if (isInvalid(transferObject)) {
 
                // An error is composed of four things:
                // - a JSON Pointer indicating which value of the JSON document is invalid (see http://tools.ietf.org/html/rfc6901#section-5)
                // - an indicator defining what type of value is invalid
                // - a code identifying the error type
                // - an English message describing the error
                context.addError("/json/pointer/to/invalid/value", EApiErrorLocationType.JSON, EApiErrorCodes.ERROR_TYPE, "This data is invalid.");
 
                // Additional arguments will be interpolated into the message with String#format.
                context.addError("/path", EApiErrorCodes.ERROR_TYPE, EApiErrorLocationType.JSON, "Message with %s, %s and %s.", "a", "b", "c");
            }
 
            // You can extract a field validation into its own validator, for example for the uniqueness of the user name.
            // Call this validator with #validateObject and specify the path of that field relative to the current object.
            context.validateObject(transferObject.getName(), "/name", new UniqueUserNameValidator(userDao));
 
            // You can also write validators for sub-objects which you call with the path to the sub-object.
            // All errors added by this "sub-validator" will be scoped under the specified path.
            context.validateObject(transferObject.getAddress(), "/address", new AddressValidator());
 
            // You can also apply a validator to each item in a list with #validateObjects.
            // All errors added by this validator will be scoped under the specified path and the index of the item (e.g. "/applications/0").
            context.validateObjects(transferObject.getApplications(), "/applications", new ApplicationValidator());
        }
    }
 
    @SkipValidationOnPreviousErrorsAtCurrentLocation // see comments in class
    protected static class UniqueUserNameValidator extends AbstractValidator<String> {
 
        // This is a value validator. It validates a value directly instead of a transfer object (AbstractValidator<String>).
        // It is also a database validation which requires manual injection of the necessary session beans.
        private IUserDao userDao;
 
        public UniqueUserNameValidator(IUserDao userDao) {
            this.userDao = userDao;
        }
 
        // Note the @SkipValidationOnPreviousErrorsAtCurrentLocation annotation on the class.
        // If the value is already invalid after bean validations (null or wrong length), there's no need to run this validation (and the database access).
        // Simply apply this annotation to skip the validation if there are previous errors.
        // Here you must use the "AtCurrentLocation" annotation because you don't know the location of the value you are validating (it is defined by the parent caller).
 
        @Override
        protected void validate(String userName, IValidationContext context) {
            if (userDao.findByName(userName) != null) {
                context.addErrorAtCurrentLocation(EApiErrorCodes.NON_UNIQUE, EApiErrorLocationType.JSON, "This name is already taken.");
            }
        }
    }
 
    // Validators for sub-objects must be typed with the sub-object class.
    protected static class AddressValidator extends AbstractValidator<AddressTO> {
 
        @Override
        protected void validate(AddressTO transferObject, IValidationContext context) {
            if (isUnknownCity(transferObject.getCity())) {
 
                // In this example, the error added at "/city" will be relative to "/address" since that is the
                // context in which this validator was called. The full error location will therefore be "/address/city".
                context.addError(EApiErrorCodes.ADDRESS_UNKNOWN_CITY, EApiErrorLocationType.JSON, "Unknown city \"" + transferObject.getCity() + "\".", "/city");
            }
        }
    }
 
    // You can specify sub-object properties that will cause validation to be skipped if they have previous errors.
    // These locations are relative to the context in which the validator will be called (see comments in #validate).
    @SkipValidationOnPreviousErrors(locations = {"/name"})
    protected static class ApplicationValidator extends AbstractValidator<ApplicationTO> {
 
        @Override
        protected void validate(ApplicationTO transferObject, IValidationContext context) {
            if (expensiveDatabaseValidationOnApplicationName(transferObject.getName())) {
 
                // In this example the application is validated as part of a list. The error will be added in the context
                // of "/applications/{i}", so for the first application the full error location will be "/applications/0/name".
                context.addError("/name", EApiErrorCodes.WEIRD_ERROR, EApiErrorLocationType.JSON, "You can't name it like that.", );
            }
        }
    }
}
``` 

Note that the validation classes can be defined internally and get access to
the services by a variable passed from the parent class through the constructor.


## Execute for Result

Once you've added the Validation Beans and created your complex validation
classes, it's time to hook them up and execute them for a result.
 
By extending the [RestResource][RestResource] from the REST library, this is
pretty easy to do:

```java
@Stateless
@Path("users")
public class UserResource extends AbstractResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(UserTO transferObject) {
 
        // Add complex validator using #validateWith and process the chain
        preprocessing().validateWith(new UserTOValidator(userDao)).process(transferObject);
        
        // if you reach this point, the TO is valid and can be processed
    }
}
```

If any component of the pre-processing chain produced an error, the `process()`
method throws an `ApiErrorsException`. Using an exception mapper such as [the 
one from REST Library][exception-mapper], the error will be serialized to the 
client. Due to the nature of the exception, processing at that point will
be halted.


## Output

One of the key aspects of any validation component is to present the user or 
client with a comprehensive list of points to fix, in the most convenient way
possible.

When the validation context gets serialized and returned, it consists always of
the same thing: A list of errors. That means even if you've added one single 
error without any field location describing a generic problem of the request,
it will be wrapped into a list.

A typical serialized response looks like this:

```json
{
    "errors": [
        { "message": "name is already taken", "location": "/name", "code": 242, "type": "json" }
        { "message": "unknown city \"foo\"", "location": "/address/city", "code": 266, "type": "json" }
        { "message": "must not be null", "location": "/applications/0/name", "code": 101, "type": "json" }
    ]
}
```

An entry corresponds to the structure of [IError][IError] and is basically what
you provide when adding the error to the validation context.


## Partial Validation

There are cases where you might want to validate only parts of an object. A use
case of such a behavior might be a `PATCH` request from a REST API where only
a certain amount of fields are provided.

In order to make this work the object you're validating must implement the
[IPatchObject][IPatchObject] (or extend [AbstractPatchTransferObject][AbstractPatchTransferObject]
directly). The idea is that in your setters, instead of applying the value
directly to your member, you do it through `markPropertyAsSet()`, so we can
later determine if a value needs to be validated or not. For an example, see
the class documentation of [AbstractPatchTransferObject][AbstractPatchTransferObject].

For Bean Validations, partial validations are applied automatically, while for
complex validations, they must be applied manually. In order to do that, 
execute the `validatePatch()` in your chain before validating:

```java
@Stateless
@Path("users")
public class UserResource extends AbstractResource {

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(UserTO transferObject) {
 
        // Add complex validator using #validateWith and process the chain
        preprocessing().validatePatch().validateWith(new UserTOValidator(userDao)).process(transferObject);
    }
}
```

## Modificators

In addition to validations, this library also supports processing of so-called
*Modificators*, which are the first thing processed in the chain. A modificator
allows modifying a value before it gets validated. 

We've included the `Trim` modificator, which trims off white spaces. In order
apply it, simply add it as an annotation to the field in question:

```java
public class UserTO {
 
 	@Trim
    private String name;
    
    // ...
}
```

## Further Reading

On how to implement a Bean Validator and a Modifier, check out the 
[Confluence page][confluence-howto-write].


## Maven Integration

In a standard Maven multi-module project like we have (EAR / EJB / WAR / JAR), you'll need to setup the dependency as
follows.

The first thing to do is to add the dependency in the `dependencyManagement` section in the `<artifactIdPrefix>/pom.xml`. 
You can copy/paste the following dependency definition:

```xml
<!-- Validation -->
<dependency>
	<groupId>com.forbesdigital.jee</groupId>
	<artifactId>validation</artifactId>
	<version>[[ version ]]</version>
</dependency>
```

**Note:** Replace `[[ version ]]` by the correct version you need in your project. At each version update, you can then
bump the version in here. This avoids tricky issues where different versions are defined for a same dependency.

Secondly, you'll need to put the dependency in your EJB and EJB-Test modules. (`<artifactIdPrefix>/<artifactIdPrefix>-ejb/pom.xml`
and `<artifactIdPrefix>/<artifactIdPrefix>-ejb-test/pom.xml`). This time, you will add the dependency under 
`dependencies`:

```xml
<dependency>
	<groupId>com.forbesdigital.jee</groupId>
	<artifactId>validation</artifactId>
	<scope>provided</scope>
</dependency>
```

**Note:** You will not specify the version because this already done in the parent `pom.xml` file. This means that the
version is inherited. The `<scope>` is there to manage properly the packaging and the dependencies packaged in the 
different jar/war/ear files.

Finally, you need to put the dependency in your WAR and WAR-Test modules. (`<artifactIdPrefix>/<artifactIdPrefix>-war/pom.xml`
and `<artifactIdPrefix>/<artifactIdPrefix>-war-test/pom.xml`). Again, dependency goes under `dependencies`:

```xml
<dependency>
	<groupId>com.forbesdigital.jee</groupId>
	<artifactId>validation</artifactId>
</dependency>
```

**Note:** No `<version>` for the same reason than before. No `<scope>` because we need to package the dependency in the
war.


## Authors

  - [Laurent Prevost][lprevost]
  - [Simon Oulevay][soulevay]


   
[bean-validations]: http://en.wikipedia.org/wiki/Bean_Validation
[built-in]: http://docs.oracle.com/javaee/6/api/javax/validation/constraints/package-summary.html
[IValidator]: src/main/java/com/forbesdigital/jee/validation/IValidator.java
[IValidationContext]: src/main/java/com/forbesdigital/jee/validation/IValidationContext.java
[preprocessing]: src/main/java/com/forbesdigital/jee/validation/preprocessing
[PreprocessingChain]: src/main/java/com/forbesdigital/jee/validation/preprocessing/PreprocessingChain.java
[AbstractValidator]: src/main/java/com/forbesdigital/jee/validation/AbstractValidator.java
[RestResource]: /projects/LIB/repos/fd-jee-rest/browse/src/main/java/com/forbesdigital/jee/rest/AbstractResource.java
[IError]: src/main/java/com/forbesdigital/jee/validation/IError.java
[IPatchObject]: src/main/java/com/forbesdigital/jee/validation/IPatchObject.java
[AbstractPatchTransferObject]: src/main/java/com/forbesdigital/jee/validation/AbstractPatchTransferObject.java
[confluence-howto-write]: https://lotaris.atlassian.net/wiki/display/DC/REST+API+Validations
[exception-mapper]: /projects/LIB/repos/fd-jee-rest/browse/src/main/java/com/forbesdigital/jee/rest/mappers/ApiErrorsExceptionMapper.java
[lprevost]: /users/lprevost
[soulevay]: /users/soulevay