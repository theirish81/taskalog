import com.github.theirish81.TalFS

object TalConfig {

    val appConfig : Map<*,*> = TalFS.parseYamlFile(TalFS.getEtcFile().resolve("application.yml"))
}