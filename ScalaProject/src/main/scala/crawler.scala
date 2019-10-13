package ScalaSpells

import com.gaocegege.scrala.core.common.response.impl.HttpResponse
import com.gaocegege.scrala.core.spider.impl.DefaultSpider
import java.io._
import java.nio.file.{Paths, Files}

import scala.collection.mutable.ArrayBuffer

class TestSpider extends DefaultSpider {
  var startUrl = List[String]()

  def get_spell_informations(response: HttpResponse): Array[String] = {
    val spell_inf = new Array[String](4)

    val content = response.getContentParser()
    val spellInformation = content.select("div[class=SpellDiv]")
    val SpDet = spellInformation.select("p[class=SpDet]")

    // title
    val spell_title = spellInformation.select("div[class=heading]").first.text()
    spell_inf(0) = spell_title

    // levels
    val levels = SpDet.first.text()
    val spell_levels = levels.split(";").last.trim().substring(6)
    spell_inf(1) = spell_levels

    // components
    val components = SpDet.get(2).text()
    var spell_components = ""
    for (c <- components.substring(11)) {
      if (c.isUpper) {
        spell_components += c
      }
    }
    spell_inf(2) = spell_components

    // spell resistance
    var spellResist = "false"
    if (SpDet.html().contains("Spell Resistance")) {
      for (element <- SpDet.last().text().split(";")) {
        if (element.contains("Spell Resistance")) {
          spellResist = element.trim().substring(17)
        }
      }
    }
    if (spellResist.contains("yes")) {
      spellResist = "true"
    } else {
      spellResist = "false"
    }
    spell_inf(3) = spellResist

    spell_inf
  }

  def parse(response: HttpResponse): Unit = {

    val spell_information = get_spell_informations(response)
    val spellTitle = spell_information(0)
    val spellLevels = spell_information(1)
    val spellComponents = spell_information(2)
    val spellResistance = spell_information(3)

    val spell_line = spellTitle + ";" + spellLevels + ";" + spellComponents + ";" + spellResistance + "\n"

    val fileName = "spells_thread" + Thread.currentThread().getId() + ".txt"
    val fw = new FileWriter(fileName, true)
    try {
      fw.write( spell_line )
    }
    finally fw.close()
  }

  def printIt(response: HttpResponse): Unit = {
    println((response.getContentParser).title)
  }
}

object Crawling {
  def main(args: Array[String]) {
    val crawler = new TestSpider

    // close existing files
    for (i <- 0 to 30) {
      val fileName = "spells_thread" + i + ".txt"

      val file_to_check = new File(fileName)
      val exist = file_to_check.exists()
      if (exist) {
        file_to_check.delete()
      }
    }


    val last_page_index = 1600

    for (index <- 1 to last_page_index) {
      val newUrl = "http://www.dxcontent.com/SDB_SpellBlock.asp?SDBID=" + index.toString()
      val newUrlCollection = newUrl :: crawler.startUrl
      crawler.startUrl = newUrlCollection
    }

    crawler begin

  }
}