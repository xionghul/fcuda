//============================================================================//
//    FCUDA
//    Copyright (c) <2016> 
//    <University of Illinois at Urbana-Champaign>
//    <University of California at Los Angeles> 
//    All rights reserved.
// 
//    Developed by:
// 
//        <ES CAD Group & IMPACT Research Group>
//            <University of Illinois at Urbana-Champaign>
//            <http://dchen.ece.illinois.edu/>
//            <http://impact.crhc.illinois.edu/>
// 
//        <VAST Laboratory>
//            <University of California at Los Angeles>
//            <http://vast.cs.ucla.edu/>
// 
//        <Hardware Research Group>
//            <Advanced Digital Sciences Center>
//            <http://adsc.illinois.edu/>
//============================================================================//

package fcuda.transforms;
import fcuda.utils.*;
import java.util.*;


import cetus.hir.*;
import cetus.exec.*;
import cetus.transforms.*;

/**
 * Transforms a program such that every variable declaration 
 * within a program occurs before other (non-declarative) statements.
 */
public class AnsiDeclarations extends ProcedureTransformPass
{
  public AnsiDeclarations(Program program)
  {
    super(program);
  }

  public void gatherDecls(Procedure proc)
  {
    DepthFirstIterator iter = new DepthFirstIterator(proc);

    while(iter.hasNext()) {
      CompoundStatement scope;

      try {
        scope=(CompoundStatement)iter.next(CompoundStatement.class);
      } catch (NoSuchElementException e) {
        break;
      }

      List<Traversable> statements = scope.getChildren();
      List<Statement> nonDeclarations = new LinkedList<Statement>();

      //Remove and then replace all non-declaration statements, so they end up at the end
      for(Traversable i : statements) {
        Statement stmt = (Statement)i;

        if(!(stmt instanceof DeclarationStatement))
          nonDeclarations.add(stmt);
      }

      for(Statement d : nonDeclarations) {
        d.detach();
        scope.addStatement(d);
      }
    }
  }

  public void transformProcedure(Procedure proc)
  {
    List<Procedure> tskLst = FCUDAutils.getTaskMapping(proc.getSymbolName()); 
    if(tskLst != null) {
      Iterator<Procedure> itr = tskLst.iterator();
      while(itr.hasNext())
        gatherDecls(itr.next());
    }   
    gatherDecls(proc);
  }

  public String getPassName()
  {
    return new String("[AnsiDeclarations]");
  }
}
