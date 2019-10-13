import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Spell(tc :  String, lc : String, cc : String, sc : Boolean) extends Serializable {
  var title : String = tc
  var levels : String = lc
  var components : String = cc
  var spellResistance : Boolean = sc

  def selfPrint(): Unit = {
    println(title + ": levels " + levels + " / components " + components + " / spell resistance " +  spellResistance)
  }
}

object Pito {

  def collect_spells(filename : String, spell_collection : ArrayBuffer[Spell]): Unit = {
    for (line <- Source.fromFile(filename).getLines) {
      val spell_infos =  line.split(";")
      //                        title           levels          components      spell resistance
      val new_spell = new Spell(spell_infos(0), spell_infos(1), spell_infos(2), spell_infos(3).toBoolean)
      spell_collection.append(new_spell)
    }
  }

  def get_spell_wizard_level(spell : Spell): Int = {
    val levels = spell.levels.split(",")
    var def_level = levels(0).trim().last
    for (element <- levels) {
      if (element.contains("wizard")) {
        def_level = element.trim().last
      }
    }
    val level = def_level.toInt - '0'.toInt
    level
  }

  def is_wizard_spell(spell : Spell): Boolean = {
    if (spell.levels.contains("wizard")) true else false
  }

  def is_verbal_spell(spell : Spell): Boolean = {
    if (spell.components.equals("V")) true else false
  }

  def main(args: Array[String]): Unit = {
    val spell_collection = new ArrayBuffer[Spell]()

    for (i <- 0 to 30) {
      val fileName = "spells_thread" + i + ".txt"
      val file_to_check = new File(fileName)

      if (file_to_check.exists()) {
        collect_spells(fileName, spell_collection)
      }
    }

    val conf = new SparkConf()
      .setAppName("Spells")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    val spellsRDD = sc.makeRDD(spell_collection)

    val Pito_spells = spellsRDD.filter(
      spell => if (is_wizard_spell(spell) && is_verbal_spell(spell) && (get_spell_wizard_level(spell) < 5))  true else false
    )

    print(Pito_spells.count() + " spells found:")
    print("\n")
    for (element <- Pito_spells.collect()) {
      element.selfPrint()
    }
    println("\n\n")
  }
}


