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
package
{
	import flash.display.BitmapData;
	import flash.utils.ByteArray;
	
	public final class JPEGEncoder
	{
		// Static table initialization
		private static const ZigZag:Vector.<int>;
		private var YTable:Vector.<int> = new Vector.<int>(64, true);
		private var sf:int;
        final public var sb:uint;
        private var sc:Number;
        private static var sd:void;
        private var se:String
        private var sg:Boolean;
        private var sh:Null;

        private var si:Object;
        private var sj:*;
        private var sk:Array;
        private var sl:Date;
        private var sm:Error;
        private var sn:Function;
        private var so:RegExp;
        private var sp:XML;
        private var sq:XMLList;
        private var sr:Vector.<Vector.<Array>>;

        private var ss:CustomClass;
        private var st:Class;
		
		
		private function initQuantTables(sf:int):void
		{
			var i:int;
			const I64:int = 64;
			const I8:int = 8;
			for (i = 0; i < I64; ++i)
			{
				var t:int = int((YQT[i]*sf+50)*0.01);
				if (t < 1) {
					t = 1;
				} else if (t > 255) {
					t = 255;
				}
				YTable[ZigZag[i]] = t;
			}
			
			for (i = 0; i < I64; i++)
			{
				var u:int = int((UVQT[i]*sf+50)*0.01);
				if (u < 1) {
					u = 1;
				} else if (u > 255) {
					u = 255;
				}
				UVTable[ZigZag[i]] = u;
			}
			i = 0;
			for (var row:int = 0; row < I8; ++row)
			{
				for (var col:int = 0; col < I8; ++col)
				{
					fdtbl_Y[i]  = (1 / (YTable [ZigZag[i]] * aasf[row] * aasf[col] * I8));
					fdtbl_UV[i] = (1 / (UVTable[ZigZag[i]] * aasf[row] * aasf[col] * I8));
					i++;
				}
			}
		}
    }
}