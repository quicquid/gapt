import at.logic.gapt.cli.GAPScalaInteractiveShellLibrary.prooftool
import at.logic.gapt.expr._
import at.logic.gapt.expr.fol.FOLSubstitution
import at.logic.gapt.expr.hol.univclosure
import at.logic.gapt.proofs.lk.base.LKProof
import at.logic.gapt.proofs.{HOLSequent, Sequent}
import at.logic.gapt.formats.prover9.Prover9TermParserLadrStyle.parseFormula
import at.logic.gapt.provers.Prover
import at.logic.gapt.provers.inductionProver._
import SimpleInductionProof._
import at.logic.gapt.provers.prover9.Prover9Prover
import org.apache.log4j.{Logger, Level}

for (n <- Seq(classOf[SipProver].getName, FindFormulaH.getClass.getName))
  Logger.getLogger(n).setLevel(Level.DEBUG)

val factorialES = ( ( ( "s(0) = f(0)" +: "s(x)*f(x) = f(s(x))" +: "g(x,0) = x" +: "g(x*s(y),y) = g(x,s(y))" +: "x*s(0) = x" +: "s(0)*x = x" +: "(x*y)*z = x*(y*z)" +: Sequent() ) map parseFormula ) map univclosure.apply ) :+ FOLSubstitution( FOLVar("x") -> alpha )( parseFormula( "g(s(0), x) = f(x)" ) )

val theory = ( "x*s(0) = x" +: "s(0)*x = x" +: "(x*y)*z = x*(y*z)" +: Sequent() ) map parseFormula map univclosure.apply

val modThProver = new Prover {
  val p9 = new Prover9Prover

  override def getLKProof(seq: HOLSequent): Option[LKProof] =
    p9.getLKProof(theory ++ seq)

  override def isValid(seq: HOLSequent): Boolean =
    p9.isValid(theory ++ seq)
}

object patchingSolFinder extends SolutionFinder {
  val n = 0

  override def findSolution( sip: SimpleInductionProof ): Option[FOLFormula] = {
    // We could pass the whole theory here, but ForgetfulParamodulate only does ground
    // paramodulation--so we give just the correct instance.
    val canSolModTh = canonicalSolution( sip, n )
    val canSol = And(canSolModTh, FOLSubstitution(FOLVar("x") -> gamma)(parseFormula("x*s(0)=x")))

    FindFormulaH(canSol, sip, n, forgetClauses = true, prover = modThProver)
  }
}

val sipProver = new SipProver(
  quasiTautProver = modThProver,
  solutionFinder = patchingSolFinder,
  instances = 0 to 3,
  testInstances = 0 to 6,
  minimizeInstanceLanguages = true  // <- removes instances that are subsumed by the theory :-)
)

val Some(sip) = sipProver.getSimpleInductionProof(factorialES)
println(sip.inductionFormula)
println(sip.isSolved(modThProver))
//prooftool(sip.toLKProof(modThProver))

