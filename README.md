# Ceylon Maven plugin

The `ceylon-maven-plugin` is a Maven plugin allowing you to execute the Ceylon
tools (`ceylonc`, `ceylond`) from inside Maven. 

## Scope

The plugin currently targets the 
limited use case of a Java project built with Maven wanting to provide a 
Ceylon wrapper (so the Java project can be used from Ceylon). The plugin can 
be configured to execute `ceylonc` (during the compile phase of the jar 
lifecycle to produce a `.car`) and 
`celyonr` (during the install and/or deploy phase of the jar lifecycle to 
the publish the `.car` file to a *Ceylon* repository).

This plugin does **not** seek to address building a pure Ceylon project using 
Maven, nor does it seek to support deploying Ceylon `.car` artifacts to 
Maven repositories.

## License

The content of this repository is released under the ASL v2.0
as provided in the LICENSE file that accompanied this code.

By submitting a "pull request" or otherwise contributing to this repository, you
agree to license your contribution under the license mentioned above.
