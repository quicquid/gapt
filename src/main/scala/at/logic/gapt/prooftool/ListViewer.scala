package at.logic.gapt.prooftool

import java.awt.event.{ KeyEvent, ActionEvent }
import javax.swing.KeyStroke

import at.logic.gapt.formats.arithmetic.HOLTermArithmeticalExporter
import at.logic.gapt.formats.latex.{ SequentsListLatexExporter, ProofToLatexExporter }
import at.logic.gapt.formats.tptp.TPTPFOLExporter
import at.logic.gapt.formats.writers.FileWriter
import at.logic.gapt.formats.xml.{ ProofDatabase, XMLExporter }
import at.logic.gapt.proofs.HOLSequent
import at.logic.gapt.proofs.lkNew.{ lkNew2Old, LKProof }
import java.io.{ BufferedWriter => JBufferedWriter, FileWriter => JFileWriter, ByteArrayInputStream, InputStreamReader, File }

import scala.swing.{ Separator, Menu, FileChooser, Action }
import scala.swing.event.Key

/**
 * Created by sebastian on 12/13/15.
 */
class ListViewer( name: String, list: List[HOLSequent] ) extends PTMain[List[HOLSequent]]( name, list ) { outer =>
  override type MainComponentType = DrawList
  override def createMainComponent( fSize: Int ) = new DrawList( this, list, fSize )

  override val mBar = new PTMenuBar( this ) {
    contents += new Menu( "File" ) {
      mnemonic = Key.F
      contents += new PTMenuItem( outer, canBeDisabled = false, Action( "Save as..." ) {
        outer.fSave( this.name, outer.content )
      } ) {
        mnemonic = Key.S
        this.peer.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK ) )
      }

      contents ++= Seq( new Separator(), exportToPDFButton, exportToPNGButton )
    }

    contents += new Menu( "View" ) {
      mnemonic = Key.V

      contents ++= Seq( zoomInButton, zoomOutButton )
    }
  }

  def fSave( name: String, list: List[HOLSequent] ) {
    chooser.fileFilter = chooser.acceptAllFileFilter
    chooser.showSaveDialog( mBar ) match {
      case FileChooser.Result.Approve =>
        scrollPane.cursor = new java.awt.Cursor( java.awt.Cursor.WAIT_CURSOR )
        val result = chooser.selectedFile.getPath
        // val pair = body.getContent.getData.get

        try {
          val ls = list.map {
            case fs: HOLSequent => fs
            case _              => throw new Exception( "Cannot save this kind of lists." )
          }
          if ( result.endsWith( ".xml" ) || chooser.fileFilter.getDescription == ".xml" ) {
            XMLExporter( result, new ProofDatabase( Map(), Nil, Nil, List( ( name, ls ) ) ) )
          } else if ( result.endsWith( ".tex" ) || chooser.fileFilter.getDescription == ".tex" ) {
            val filename = if ( result.endsWith( ".tex" ) ) result else result + ".tex"
            ( new FileWriter( filename ) with SequentsListLatexExporter with HOLTermArithmeticalExporter )
              .exportSequentList( ls, Nil ).close
          } else if ( result.endsWith( ".tptp" ) || chooser.fileFilter.getDescription == ".tptp" ) {
            val filename = if ( result.endsWith( ".tptp" ) ) result else result + ".tptp"
            val file = new JBufferedWriter( new JFileWriter( filename ) )
            file.write( TPTPFOLExporter.tptp_problem( ls ) )
            file.close()
          } else infoMessage( "Lists cannot be saved in this format." )
        } catch { case e: Throwable => errorMessage( "Cannot save the list! " + dnLine + getExceptionString( e ) ) }
        finally { scrollPane.cursor = java.awt.Cursor.getDefaultCursor }
      case _ =>
    }
  }

}
