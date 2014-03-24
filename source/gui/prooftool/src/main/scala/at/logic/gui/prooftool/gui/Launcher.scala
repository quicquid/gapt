package at.logic.gui.prooftool.gui

/**
 * Created by IntelliJ IDEA.
 * User: mrukhaia
 * Date: Oct 30, 2010
 * Time: 5:43:38 PM
 */

import at.logic.calculi.expansionTrees.ExpansionTree
import at.logic.calculi.lk.base.FSequent
import at.logic.calculi.proofs.{Proof, TreeProof}
import at.logic.gui.prooftool.parser.{UnLoaded, Loaded, ProofToolPublisher, StructPublisher}
import at.logic.language.hol.HOLFormula
import at.logic.utils.ds.trees.Tree

import java.awt.Font._
import java.awt.event.{MouseEvent, MouseMotionListener}
import javax.swing.border.TitledBorder
import scala.swing._
import event.{MouseWheelMoved, MouseReleased, MouseDragged}

class Launcher(private val option: Option[(String, AnyRef)], private val fSize: Int) extends GridBagPanel with MouseMotionListener {
  option match {
    case Some((name: String, obj: AnyRef)) =>
      val c = new Constraints
      c.grid = (0,0)
      c.insets.set(15, 15, 15, 15)
      obj match {
        case proof: TreeProof[_] =>
          layout(new DrawProof(proof, fSize, Set(), Set(), Set(), "")) = c
          ProofToolPublisher.publish(Loaded)
          StructPublisher.publish(UnLoaded)
        case resProof: Proof[_] =>
          layout(new DrawResolutionProof(resProof, fSize, Set(), Set(), "")) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
        case tree: Tree[_] =>
          layout(new DrawTree(tree, fSize, "")) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(Loaded)
        case list: List[AnyRef] =>
          layout(new DrawList(list, fSize)) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
        case set: Set[AnyRef] => // use the case for lists for sets, too
          layout(new DrawList(set.toList, fSize)) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
        case formula: HOLFormula => // use the case for lists for single formulas, too
          layout(new DrawList(formula::Nil, fSize)) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
        case fSequent: FSequent =>
          layout(new DrawHerbrandSequent[HOLFormula]((fSequent.antecedent,fSequent.succedent), fSize)) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
        case expTrees: (Seq[ExpansionTree],Seq[ExpansionTree]) =>
          layout(new DrawHerbrandSequent[ExpansionTree](expTrees, fSize)) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
        case _ =>
          layout(new Label("Cannot match the "+option.get._2.getClass.toString + " : " + option.get._2){
            font = new Font(SANS_SERIF, BOLD, 16)
          }) = c
          ProofToolPublisher.publish(UnLoaded)
          StructPublisher.publish(UnLoaded)
      }
//      val bd: TitledBorder = Swing.TitledBorder(Swing.LineBorder(new Color(0,0,0), 2), " "+name+" ")
      val nice_name:String = name match {
        case s:String if s == "\\psi" || s == "psi" => "ψ"
        case s:String if s == "\\chi" || s == "chi" => "χ"
        case s:String if s == "\\varphi" || s == "varphi" => "φ"
        case s:String if s == "\\phi" || s == "phi" => "ϕ"
        case s:String if s == "\\rho" || s == "rho" => "ρ"
        case s:String if s == "\\sigma" || s == "sigma" => "σ"
        case s:String if s == "\\tau" || s == "tau" => "τ"
        case s:String if s == "\\omega" || s == "omega" => "Ω"

        case _ => name
      }
      val bd: TitledBorder = Swing.TitledBorder(Swing.LineBorder(new Color(0,0,0), 2), " "+nice_name+" ")
      bd.setTitleFont(new Font(SANS_SERIF, BOLD, 16))
      border = bd
    case _ =>
  }

  background = new Color(255,255,255)

  def fontSize = fSize
  def getData = option

  listenTo(mouse.moves, mouse.clicks, mouse.wheel)
  reactions += {
    case e: MouseDragged =>
      Main.body.cursor = new java.awt.Cursor(java.awt.Cursor.MOVE_CURSOR)
    case e: MouseReleased =>
      Main.body.cursor = java.awt.Cursor.getDefaultCursor
    case e: MouseWheelMoved =>
      Main.body.peer.dispatchEvent(e.peer)
  }

  this.peer.setAutoscrolls(true)
  this.peer.addMouseMotionListener(this)

  def mouseMoved(e: MouseEvent) {
    //println("mouse: " + e.getX + "/" + e.getY)
  }
  def mouseDragged(e: MouseEvent) {
    //The user is dragging us, so scroll!
    val r = new Rectangle(e.getX, e.getY, 1, 1)
    this.peer.scrollRectToVisible(r)
  }

  // returns the location of the end-sequent
  // of a proof
  def getLocationOfProof[S](proof: TreeProof[S]) = 
  {
    val dp = contents.head.asInstanceOf[DrawProof]
    dp.getLocationOfProof(proof)
  }
}
