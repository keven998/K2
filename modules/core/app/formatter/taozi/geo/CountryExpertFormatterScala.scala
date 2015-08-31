package formatter.taozi.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import formatter.BaseFormatter
import formatter.taozi.ImageItemSerializerScala
import models.geo.{ Continent, CountryExpert }
import models.misc.ImageItem

/**
 * Created by pengyt on 2015/8/27.
 */
class CountryExpertFormatterScala extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[CountryExpert], new CountryExpertSerializerScala)
    module.addSerializer(classOf[Continent], new SimpleContinentSerializerScala)
    module.addSerializer(classOf[ImageItem], ImageItemSerializerScala())
    mapper.registerModule(module)
    mapper
  }
}
