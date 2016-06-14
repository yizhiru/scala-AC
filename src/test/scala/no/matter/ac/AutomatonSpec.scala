package no.matter.ac

import org.specs._


object AutomatonSpec extends Specification {
  val ac = new Automaton
  ac.addWords("ss", "he", "she", "his", "hers")
  ac.setFailTransitions()
  val results = ac.search("ushers")
    .map(t => t._1)

  "ushers" should {
    "must contains" in {
      "he" in {
        results must contain("he")
      }
      "she" in {
        results must contain("she")
      }
      "hers" in {
        results must contain("hers")
      }
    }
  }

}


