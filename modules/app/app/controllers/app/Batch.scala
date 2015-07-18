package controllers.app

import java.util.concurrent.Future
import java.util.concurrent.Future

import com.fasterxml.jackson.databind.node.{ ArrayNode, ObjectNode }
import com.twitter.util.Future
import controllers.bache.{ BatchUtils, BatchImpl }
import formatter.FormatterFactory
import formatter.taozi.geo.SimpleCountryFormatter
import misc.TwitterConverter._
import models.geo.Country
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Utils
import utils.Implicits._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by topy on 2015/7/14.
 */
object Batch extends Controller {

  def createExpertTrackByCountry() = Action.async(
    request => {
      val formatter = FormatterFactory.getInstance(classOf[SimpleCountryFormatter])
      for {
        countries <- BatchImpl.getCountriesByNames(Seq("日本", "韩国", "中国"), 0, 999)
      } yield {
        val node = formatter.formatNode(countries).asInstanceOf[ArrayNode]
        dealwithCountries(countries)
        Utils.status(node.toString).toScala
      }
    })

  def dealwithCountries(countries: Seq[Country]): Seq[Country] = {
    val userCnt = BatchImpl.getCountryToUserCntMap(countries.map(_.getId))
    writeCountries(countries, userCnt)
    null
  }

  def writeCountries(countries: Seq[Country], userCnt: Map[ObjectId, Int]): Seq[String] = {
    val subKey = "country"
    val keyMid = "."
    val keys = (subKey + Country.FD_ZH_NAME, subKey + Country.FD_EN_NAME, subKey + Country.fnImages, subKey + "expertCnt")

    def writeOneCountry(country: Country, userCnt: Map[ObjectId, Int]): Seq[String] = {
      val imageUrl = country.getImages match {
        case nulll => "null"
        case _ if country.getImages.get(0) == null => "null"
        case _ if country.getImages.get(0) != null => String.format("http://images.taozilvxing.com/%s?imageView2/2/w/ 640", country.getImages.get(0).getKey)
      }
      Seq(
        BatchUtils.writeLine(keys._1 + keyMid + country.getId.toString, country.getZhName),
        BatchUtils.writeLine(keys._2 + keyMid + country.getId.toString, country.getEnName),
        BatchUtils.writeLine(keys._3 + keyMid + country.getId.toString, imageUrl),
        BatchUtils.writeLine(keys._4 + keyMid + country.getId.toString, userCnt.get(country.getId).getOrElse(0).toString)
      )
    }
    val contents = countries.flatMap(writeOneCountry(_, userCnt))
    BatchUtils.makeConfFile(contents)
    null
  }

}
