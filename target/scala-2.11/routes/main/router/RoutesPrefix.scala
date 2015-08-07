
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kdoherty/dev/ideaProjects/ZipChat/conf/routes
// @DATE:Fri Aug 07 17:18:08 EDT 2015


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
