package ScalaSpells
import javax.print.attribute.standard.PageRanges
import org.apache.spark.graphx.{Edge, Graph, TripletFields}
import org.apache.spark.{SparkConf, SparkContext}



class node(val id: Int, val pageRank: Double , val link: Int) extends Serializable {
    override def toString: String = s"id : $id pageRank : $pageRank nombre de lien : $link "
  }


  class FC2 extends Serializable {

    import org.apache.spark.{SparkConf}
    import org.apache.spark.SparkContext
    import org.apache.spark.graphx.{Edge, EdgeContext, Graph, _}


    def pageranking(g: Graph[node, String]): Array[Double] = {
      val aa = g.edges.collect()

      val vectice = g.vertices.collect()
      val buffer: Array[Double] = Array.ofDim(4)
      val bufferlink: Array[Double] = Array.ofDim(4)
      var res: Array[Double] = Array(0.15,0.15,0.15,0.15)

      for (i <- vectice) {
        buffer(i._2.id-1) = i._2.pageRank
        bufferlink(i._2.id-1)=i._2.link
      }
      for (i <- aa) {

        res(i.dstId.toInt-1)=res(i.dstId.toInt-1)+0.85*(buffer(i.srcId.toInt-1)/bufferlink(i.srcId.toInt-1))
      }
      res
    }

    def updatevectice(sommet: node, i :Int): node = {
      return new node(sommet.id, i, sommet.link)
    }
    def execute(g: Graph[node, String], maxIterations: Int, sc: SparkContext): Graph[node, String] = {

      var counter = 0
      var myGraph=g

      def loop1: Unit = {
        while (true) {

          if (counter == maxIterations) return
          println("ITERATION NUMERO : " + (counter + 1))
          counter += 1
          var nouveautab=pageranking(myGraph)
          val vectice = g.vertices.collect()
          val vecticevide=(0L,new node(0,0,0))
          var newvectice:Array[(Long,node)]=Array(vecticevide,vecticevide,vecticevide,vecticevide)
          for (i <- vectice) {
            newvectice(i._2.id-1)=(i._2.id.toLong,new node (i._2.id,nouveautab(i._2.id-1),i._2.link))
          }

          var myVertices = sc.makeRDD(newvectice)


          myGraph =Graph(myVertices,myGraph.edges)

          var printedGraph = myGraph.vertices.collect()
          printedGraph = printedGraph.sortBy(_._1)
          printedGraph.foreach(
            elem => println(elem._2)
          )

        }
      }

      loop1 //execute loop
      myGraph //return the result graph
    }
  }


  object pagerank extends App {
    val conf = new SparkConf()
      .setAppName("PageRank")
      .setMaster("local[*]")
    val sc = SparkContext.getOrCreate(conf)
    sc.setLogLevel("ERROR")


    var myVertices = sc.makeRDD(Array(
      (1L, new node(1, 1, 2)), //A
      (2L, new node(2, 1, 1)), //B
      (3L, new node(3, 1, 1)), //C
      (4L, new node(4, 1, 1)))) //D


    var myEdges = sc.makeRDD(Array(
      Edge(1L, 2L, "1to2"), Edge(1L, 3L, "1to3"),
      Edge(2L, 3L, "2to3"),
      Edge(3L, 1L, "3to1"),
      Edge(4L, 3L, "4to3")))


    var myGraph = Graph(myVertices, myEdges)
    val pagerank = new FC2()
    val res = pagerank.execute(myGraph, 20, sc)



  }
