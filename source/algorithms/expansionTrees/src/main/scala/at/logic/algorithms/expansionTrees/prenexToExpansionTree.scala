
package at.logic.algorithms.expansionTrees

import at.logic.calculi.expansionTrees._
import at.logic.language.fol.{FOLFormula, FOLTerm, FOLExpression, AllVar => AllVarFOL, ExVar => ExVarFOL}
import at.logic.language.fol.Substitution
import at.logic.algorithms.unification.fol.FOLUnificationAlgorithm

// Builds an expansion tree given a *prenex first order* formula and 
// its instances (or substitutions) using only weak quantifiers. 
//
// NOTE: initially, this could be implemented for non-prenex formulas in HOL. 
// What needs to be implemented is a method to remove the quantifiers of a
// non-prenex formula (taking care about the renaming of variables) and a
// matching that would return the substitutions (there is a HOL matching
// implemented, but it is called NaiveIncompleteMatchingAlgorithm, and I am not
// sure whether I trust this name :P ).
object prenexToExpansionTree {
  def apply(f: FOLFormula, lst: List[FOLFormula]) : ExpansionTree = {
    val fMatrix = f.getMatrix

    // Each possible instance will generate an expansion tree, and they all 
    // have the same root.
    val children = lst.foldLeft(List[(ExpansionTree, HOLExpression)]()) { 
      case (acc, instance) =>
        val subs = FOLUnificationAlgorithm.unify(fMatrix, instance)
        // WARNING: Considering only the first substitution
        val expTree = subs match {
          case h::t => apply_(f, h) // WARNING: considering only the first substitution
          case Nil => throw new Exception("ERROR: prenexToExpansionTree: No substitutions found for:\n" + 
            "Matrix: " + fMatrix + "\nInstance: " + instance)
        }
        expTree match {
          case WeakQuantifier(_, lst) => lst.toList ++ acc
          case _ => throw new Exception("ERROR: Quantifier-free formula?")
        }
    }
    
    WeakQuantifier(f, children)
  }

  def apply_(f: FOLFormula, sub: Substitution) : ExpansionTree = f match {
    case AllVarFOL(v, _) =>
      val t = sub.map(v)
      val newf = f.instantiate(t.asInstanceOf[FOLTerm])
      WeakQuantifier(f, List(Pair(apply_(newf, sub), t)))
    case ExVarFOL(v, _) => 
      val t = sub.map(v)
      val newf = f.instantiate(t.asInstanceOf[FOLTerm])
      WeakQuantifier(f, List(Pair(apply_(newf, sub), t)))
    case _ => qFreeToExpansionTree(f)
  }
  
}
