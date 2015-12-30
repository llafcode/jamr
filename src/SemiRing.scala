package edu.cmu.lti.nlp.amr

import scala.collection.mutable.ArrayBuffer
import scala.collection.{mutable => m, immutable => i}

abstract class SemiRing[T] {
    def times(a: T) : T   // Tropical semiring plus
    def plus(a: T) : T    // Tropical semiring max
}

class Tropical[S](val path: List[S], val score: Double) extends SemiRing[Tropical[S]] {
    def times(a: Tropical[S]) : Tropical[S] = {
        return new Tropical(path ::: a.path, score + a.score)
    }
    def plus(a: Tropical[S]) : Tropical[S] = {
        return List(this,a).maxBy(_.score)
    }
}

object Tropical {
    def Identity[S] : Tropical[S] = {
        new Tropical[S](List(), 0.0)
    }
    def apply[S](s: S, score: Double) : Tropical[S] = {
        new Tropical[S](List(s), score)
    }
}

class KBest[S](val kbest: List[Tropical[S]], // assumes kbest is always sorted
               val k: Int) extends SemiRing[KBest[S]] {

    def times(a: KBest[S]) : KBest[S] = {
        assert(k == a.k, "Warning: kbest sizes differ.")
        var i = 0
        var list : List[Tropical[S]] = List()
        var list1 = kbest
        var list2 = a.kbest
        while (i < k) {
            if (list1.head.score > list2.head.score) {
                list = list1.head :: list
                list1 = list1.tail
            } else {
                list = list2.head :: list
                list2 = list2.tail
            }
            i += 1
        }
        return new KBest(list.reverse.take(k), k)
    }

    def plus(a: KBest[S]) : KBest[S] = {
        assert(k == a.k, "Warning: kbest sizes differ.")
        return new KBest((kbest ::: a.kbest).sortBy(x => -x.score).take(k), k)  // could use min(k, a.k) and not throw error
    }
}

object KBest {
    def Identity[S](k: Int) : KBest[S] = {
        new KBest[S](List(), k)
    }
    def apply[S](k: Int)(s: S, score: Double) : KBest[S] = {
        new KBest[S](List(new Tropical(List(s), score)), k)
    }
}

