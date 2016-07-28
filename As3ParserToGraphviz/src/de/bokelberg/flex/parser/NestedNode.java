/**
 *    Copyright (c) 2009, Adobe Systems, Incorporated
 *    All rights reserved.
 *
 *    Redistribution  and  use  in  source  and  binary  forms, with or without
 *    modification,  are  permitted  provided  that  the  following  conditions
 *    are met:
 *
 *      * Redistributions  of  source  code  must  retain  the  above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions  in  binary  form  must reproduce the above copyright
 *        notice,  this  list  of  conditions  and  the following disclaimer in
 *        the    documentation   and/or   other  materials  provided  with  the
 *        distribution.
 *      * Neither the name of the Adobe Systems, Incorporated. nor the names of
 *        its  contributors  may be used to endorse or promote products derived
 *        from this software without specific prior written permission.
 *
 *    THIS  SOFTWARE  IS  PROVIDED  BY THE  COPYRIGHT  HOLDERS AND CONTRIBUTORS
 *    "AS IS"  AND  ANY  EXPRESS  OR  IMPLIED  WARRANTIES,  INCLUDING,  BUT NOT
 *    LIMITED  TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,  INCIDENTAL,  SPECIAL,
 *    EXEMPLARY,  OR  CONSEQUENTIAL  DAMAGES  (INCLUDING,  BUT  NOT  LIMITED TO,
 *    PROCUREMENT  OF  SUBSTITUTE   GOODS  OR   SERVICES;  LOSS  OF  USE,  DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY,  WHETHER  IN  CONTRACT,  STRICT  LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE  OR  OTHERWISE)  ARISING  IN  ANY  WAY  OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.bokelberg.flex.parser;

import java.util.ArrayList;
import java.util.List;

import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.NodeKind;

/**
 * @author xagnetti
 */
class NestedNode
{
   private List< IParserNode > children;
   private NodeKind            nodeId;

   /**
    * @param idToBeSet
    */
   protected NestedNode( final NodeKind idToBeSet )
   {
      nodeId = idToBeSet;
   }

   /**
    * @param idToBeSet
    * @param childToBeSet
    */
   protected NestedNode( final NodeKind idToBeSet,
                         final IParserNode childToBeSet )
   {
      this( idToBeSet );
      addChild( childToBeSet );
   }

   /**
    * @return
    */
   public final int computeCyclomaticComplexity()
   {
      int cyclomaticComplexity = 0;

      if ( is( NodeKind.FOREACH )
            || is( NodeKind.FORIN ) || is( NodeKind.CASE ) || is( NodeKind.DEFAULT ) )
      {
         cyclomaticComplexity++;
      }
      else if ( is( NodeKind.IF )
            || is( NodeKind.WHILE ) || is( NodeKind.FOR ) )
      {
         cyclomaticComplexity++;
         cyclomaticComplexity += getChild( 0 ).countNodeFromType( NodeKind.AND );
         cyclomaticComplexity += getChild( 0 ).countNodeFromType( NodeKind.OR );
      }

      if ( numChildren() > 0 )
      {
         for ( final IParserNode child : getChildren() )
         {
            cyclomaticComplexity += child.computeCyclomaticComplexity();
         }
      }

      return cyclomaticComplexity;
   }

   /**
    * @param type
    * @return
    */
   public final int countNodeFromType( final NodeKind type )
   {
      int count = 0;

      if ( is( type ) )
      {
         count++;
      }
      if ( numChildren() > 0 )
      {
         for ( final IParserNode child : getChildren() )
         {
            count += child.countNodeFromType( type );
         }
      }
      return count;
   }

   /**
    * @param index
    * @return
    */
   public final IParserNode getChild( final int index )
   {
      return getChildren() == null
            || getChildren().size() <= index ? null
                                            : getChildren().get( index );
   }

   /**
    * @return
    */
   public List< IParserNode > getChildren()
   {
      return children;
   }

   /**
    * @return
    */
   public NodeKind getId()
   {
      return nodeId;
   }

   /**
    * @return
    */
   public IParserNode getLastChild()
   {
      final IParserNode lastChild = getChild( numChildren() - 1 );

      return lastChild != null
            && lastChild.numChildren() > 0 ? lastChild.getLastChild()
                                          : lastChild;
   }

   /**
    * @param expectedType
    * @return
    */
   public final boolean is( final NodeKind expectedType ) // NOPMD
   {
      return getId().equals( expectedType );
   }

   /**
    * @return
    */
   public final int numChildren()
   {
      return getChildren() == null ? 0
                                  : getChildren().size();
   }

   /**
    * @param child
    * @return
    */
   final IParserNode addChild( final IParserNode child )
   {
      if ( child == null )
      {
         return child; // skip optional children
      }

      if ( children == null )
      {
         children = new ArrayList< IParserNode >();
      }
      children.add( child );
      return child;
   }

   /**
    * @param childId
    * @param childLine
    * @param childColumn
    * @param nephew
    * @return
    */
   final IParserNode addChild( final NodeKind childId,
                               final int childLine,
                               final int childColumn,
                               final IParserNode nephew )
   {
      return addChild( Node.create( childId,
                                    childLine,
                                    childColumn,
                                    nephew ) );
   }

   /**
    * @param childId
    * @param childLine
    * @param childColumn
    * @param value
    * @return
    */
   final IParserNode addChild( final NodeKind childId,
                               final int childLine,
                               final int childColumn,
                               final String value )
   {
      return addChild( Node.create( childId,
                                    childLine,
                                    childColumn,
                                    value ) );
   }

   /**
    * @param idToBeSet
    */
   final void setId( final NodeKind idToBeSet )
   {
      nodeId = idToBeSet;
   }
}
