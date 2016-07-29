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
		private const ZigZag:Vector.<int>;
		
		// Static table initialization
		private var YTable:Vector.<int> = new Vector.<int>(64, true);
		private var UVTable:Vector.<int> = new Vector.<int>(64, true);
		private var sf:int;
		
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
		
		private var YDC_HT:Vector.<BitString>;
		private var UVDC_HT:Vector.<BitString>;
		private var YAC_HT:Vector.<BitString>;
		private var UVAC_HT:Vector.<BitString>;
		
		private function computeHuffmanTbl(nrcodes:Vector.<int>, std_table:Vector.<int>):Vector.<BitString>
		{
			var codevalue:int = 0;
			var pos_in_table:int = 0;
			var HT:Vector.<BitString> = new Vector.<BitString>(251, true);
			var bitString:BitString;
			for (var k:int=1; k<=16; ++k)
			{
				for (var j:int=1; j<=nrcodes[k]; ++j)
				{
					HT[std_table[pos_in_table]] = bitString = new BitString();
					bitString.val = codevalue;
					bitString.len = k;
					pos_in_table++;
					codevalue++;
				}
				codevalue<<=1;
			}
			return HT;
		}
		
		private function initHuffmanTbl():void
		{
			YDC_HT = computeHuffmanTbl(std_dc_luminance_nrcodes,std_dc_luminance_values);
			UVDC_HT = computeHuffmanTbl(std_dc_chrominance_nrcodes,std_dc_chrominance_values);
			YAC_HT = computeHuffmanTbl(std_ac_luminance_nrcodes,std_ac_luminance_values);
			UVAC_HT = computeHuffmanTbl(std_ac_chrominance_nrcodes,std_ac_chrominance_values);
		}
		
		private var bitcode:Vector.<BitString> = new Vector.<BitString>(65535, true);
		private var category:Vector.<int> = new Vector.<int>(65535, true);
		
		private function initCategoryNumber():void
		{
			var nrlower:int = 1;
			var nrupper:int = 2;
			var bitString:BitString;
			const I15:int = 15;
			var pos:int;
			for (var cat:int=1; cat<=I15; ++cat)
			{
				//Positive numbers
				for (var nr:int=nrlower; nr<nrupper; ++nr)
				{
					pos = int(32767+nr);
					category[pos] = cat;
					bitcode[pos] = bitString = new BitString();
					bitString.len = cat;
					bitString.val = nr;
				}
				//Negative numbers
				for (var nrneg:int=-(nrupper-1); nrneg<=-nrlower; ++nrneg)
				{
					pos = int(32767+nrneg);
					category[pos] = cat;
					bitcode[pos] = bitString = new BitString();
					bitString.len = cat;
					bitString.val = nrupper-1+nrneg;
				}
				nrlower <<= 1;
				nrupper <<= 1;
			}
		}
		
		// IO functions
		
		private var byteout:ByteArray;
		private var bytenew:int = 0;
		private var bytepos:int = 7;
		
		private function writeBits(bs:BitString):void
		{
			var value:int = bs.val;
			var posval:int = bs.len-1;
			while ( posval >= 0 )
			{
				if (value & uint(1 << posval) )
					bytenew |= uint(1 << bytepos);
				posval--;
				bytepos--;
				if (bytepos < 0)
				{
					if (bytenew == 0xFF)
					{
						byteout.writeByte(0xFF);
						byteout.writeByte(0);
					}
					else byteout.writeByte(bytenew);
					bytepos=7;
					bytenew=0;
				}
			}
		}
		
		// DCT & quantization core
		
		private function fDCTQuant(data:Vector.<Number>, fdtbl:Vector.<Number>):Vector.<int>
		{
			/* Pass 1: process rows. */
			var dataOff:int=0;
			var d0:Number, d1:Number, d2:Number, d3:Number, d4:Number, d5:Number, d6:Number, d7:Number;
			var i:int;
			const I8:int = 8;
			const I64:int = 64;
			for (i=0; i<I8; ++i)
			{	
				d0 = data[int(dataOff)];
				d1 = data[int(dataOff+1)];
				d2 = data[int(dataOff+2)];
				d3 = data[int(dataOff+3)];
				d4 = data[int(dataOff+4)];
				d5 = data[int(dataOff+5)];
				d6 = data[int(dataOff+6)];
				d7 = data[int(dataOff+7)];
				
				var tmp0:Number = d0 + d7;
				var tmp7:Number = d0 - d7;
				var tmp1:Number = d1 + d6;
				var tmp6:Number = d1 - d6;
				var tmp2:Number = d2 + d5;
				var tmp5:Number = d2 - d5;
				var tmp3:Number = d3 + d4;
				var tmp4:Number = d3 - d4;
				
				/* Even part */
				var tmp10:Number = tmp0 + tmp3;	/* phase 2 */
				var tmp13:Number = tmp0 - tmp3;
				var tmp11:Number = tmp1 + tmp2;
				var tmp12:Number = tmp1 - tmp2;
				
				data[int(dataOff)] = tmp10 + tmp11; /* phase 3 */
				data[int(dataOff+4)] = tmp10 - tmp11;
				
				var z1:Number = (tmp12 + tmp13) * 0.707106781; /* c4 */
				data[int(dataOff+2)] = tmp13 + z1; /* phase 5 */
				data[int(dataOff+6)] = tmp13 - z1;
				
				/* Odd part */
				tmp10 = tmp4 + tmp5; /* phase 2 */
				tmp11 = tmp5 + tmp6;
				tmp12 = tmp6 + tmp7;
				
				/* The rotator is modified from fig 4-8 to avoid extra negations. */
				var z5:Number = (tmp10 - tmp12) * 0.382683433; /* c6 */
				var z2:Number = 0.541196100 * tmp10 + z5; /* c2-c6 */
				var z4:Number = 1.306562965 * tmp12 + z5; /* c2+c6 */
				var z3:Number = tmp11 * 0.707106781; /* c4 */
				
				var z11:Number = tmp7 + z3;	/* phase 5 */
				var z13:Number = tmp7 - z3;
				
				data[int(dataOff+5)] = z13 + z2;	/* phase 6 */
				data[int(dataOff+3)] = z13 - z2;
				data[int(dataOff+1)] = z11 + z4;
				data[int(dataOff+7)] = z11 - z4;
				
				dataOff += 8; /* advance pointer to next row */
			}
			
			/* Pass 2: process columns. */
			dataOff = 0;
			for (i=0; i<I8; ++i)
			{
				d0 = data[int(dataOff)];
				d1 = data[int(dataOff + 8)];
				d2 = data[int(dataOff + 16)];
				d3 = data[int(dataOff + 24)];
				d4 = data[int(dataOff + 32)];
				d5 = data[int(dataOff + 40)];
				d6 = data[int(dataOff + 48)];
				d7 = data[int(dataOff + 56)];
				
				var tmp0p2:Number = d0 + d7;
				var tmp7p2:Number = d0 - d7;
				var tmp1p2:Number = d1 + d6;
				var tmp6p2:Number = d1 - d6;
				var tmp2p2:Number = d2 + d5;
				var tmp5p2:Number = d2 - d5;
				var tmp3p2:Number = d3 + d4;
				var tmp4p2:Number = d3 - d4;
				
				/* Even part */
				var tmp10p2:Number = tmp0p2 + tmp3p2;	/* phase 2 */
				var tmp13p2:Number = tmp0p2 - tmp3p2;
				var tmp11p2:Number = tmp1p2 + tmp2p2;
				var tmp12p2:Number = tmp1p2 - tmp2p2;
				
				data[int(dataOff)] = tmp10p2 + tmp11p2; /* phase 3 */
				data[int(dataOff+32)] = tmp10p2 - tmp11p2;
				
				var z1p2:Number = (tmp12p2 + tmp13p2) * 0.707106781; /* c4 */
				data[int(dataOff+16)] = tmp13p2 + z1p2; /* phase 5 */
				data[int(dataOff+48)] = tmp13p2 - z1p2;
				
				/* Odd part */
				tmp10p2 = tmp4p2 + tmp5p2; /* phase 2 */
				tmp11p2 = tmp5p2 + tmp6p2;
				tmp12p2 = tmp6p2 + tmp7p2;
				
				/* The rotator is modified from fig 4-8 to avoid extra negations. */
				var z5p2:Number = (tmp10p2 - tmp12p2) * 0.382683433; /* c6 */
				var z2p2:Number = 0.541196100 * tmp10p2 + z5p2; /* c2-c6 */
				var z4p2:Number = 1.306562965 * tmp12p2 + z5p2; /* c2+c6 */
				var z3p2:Number= tmp11p2 * 0.707106781; /* c4 */
				
				var z11p2:Number = tmp7p2 + z3p2;	/* phase 5 */
				var z13p2:Number = tmp7p2 - z3p2;
				
				data[int(dataOff+40)] = z13p2 + z2p2; /* phase 6 */
				data[int(dataOff+24)] = z13p2 - z2p2;
				data[int(dataOff+ 8)] = z11p2 + z4p2;
				data[int(dataOff+56)] = z11p2 - z4p2;
				
				dataOff++; /* advance pointer to next column */
			}
			
			// Quantize/descale the coefficients
			var fDCTQuant:Number;
			for (i=0; i<I64; ++i)
			{
				// Apply the quantization and scaling factor & Round to nearest integer
				fDCTQuant = data[int(i)]*fdtbl[int(i)];
				outputfDCTQuant[int(i)] = (fDCTQuant > 0.0) ? int(fDCTQuant + 0.5) : int(fDCTQuant - 0.5);
			}
			return outputfDCTQuant;
		}
		
		// Chunk writing
		private function writeAPP0():void
		{
			byteout.writeShort(0xFFE0); // marker
			byteout.writeShort(16); // length
			byteout.writeByte(0x4A); // J
			byteout.writeByte(0x46); // F
			byteout.writeByte(0x49); // I
			byteout.writeByte(0x46); // F
			byteout.writeByte(0); // = "JFIF",'\0'
			byteout.writeByte(1); // versionhi
			byteout.writeByte(1); // versionlo
			byteout.writeByte(0); // xyunits
			byteout.writeShort(1); // xdensity
			byteout.writeShort(1); // ydensity
			byteout.writeByte(0); // thumbnwidth
			byteout.writeByte(0); // thumbnheight
		}
		
		private function writeSOF0(width:int, height:int):void
		{
			byteout.writeShort(0xFFC0); // marker
			byteout.writeShort(17);   // length, truecolor YUV JPG
			byteout.writeByte(8);    // precision
			byteout.writeShort(height);
			byteout.writeShort(width);
			byteout.writeByte(3);    // nrofcomponents
			byteout.writeByte(1);    // IdY
			byteout.writeByte(0x11); // HVY
			byteout.writeByte(0);    // QTY
			byteout.writeByte(2);    // IdU
			byteout.writeByte(0x11); // HVU
			byteout.writeByte(1);    // QTU
			byteout.writeByte(3);    // IdV
			byteout.writeByte(0x11); // HVV
			byteout.writeByte(1);    // QTV
		}
		
		private function writeDQT():void
		{
			byteout.writeShort(0xFFDB); // marker
			byteout.writeShort(132);	   // length
			byteout.writeByte(0);
			
			var i:int;
			const I64:int = 64;
			for (i=0; i<I64; ++i)
				byteout.writeByte(YTable[i]);
			
			byteout.writeByte(1);
			
			for (i=0; i<I64; ++i)
				byteout.writeByte(UVTable[i]);
		}
		
		private function writeDHT():void
		{
			byteout.writeShort(0xFFC4); // marker
			byteout.writeShort(0x01A2); // length
			
			byteout.writeByte(0); // HTYDCinfo
			var i:int;
			const I11:int = 11;
			const I16:int = 16;
			const I161:int = 161;
			for (i=0; i<I16; ++i)
				byteout.writeByte(std_dc_luminance_nrcodes[int(i+1)]);
			
			for (i=0; i<=I11; ++i)
				byteout.writeByte(std_dc_luminance_values[int(i)]);
			
			byteout.writeByte(0x10); // HTYACinfo
			
			for (i=0; i<I16; ++i)
				byteout.writeByte(std_ac_luminance_nrcodes[int(i+1)]);
			
			for (i=0; i<=I161; ++i)
				byteout.writeByte(std_ac_luminance_values[int(i)]);
			
			byteout.writeByte(1); // HTUDCinfo
			
			for (i=0; i<I16; ++i)
				byteout.writeByte(std_dc_chrominance_nrcodes[int(i+1)]);
			
			for (i=0; i<=I11; ++i)
				byteout.writeByte(std_dc_chrominance_values[int(i)]);
			
			byteout.writeByte(0x11); // HTUACinfo
			
			for (i=0; i<I16; ++i)
				byteout.writeByte(std_ac_chrominance_nrcodes[int(i+1)]);
			
			for (i=0; i<=I161; ++i)
				byteout.writeByte(std_ac_chrominance_values[int(i)]);
		}
		
		private function writeSOS():void
		{
			byteout.writeShort(0xFFDA); // marker
			byteout.writeShort(12); // length
			byteout.writeByte(3); // nrofcomponents
			byteout.writeByte(1); // IdY
			byteout.writeByte(0); // HTY
			byteout.writeByte(2); // IdU
			byteout.writeByte(0x11); // HTU
			byteout.writeByte(3); // IdV
			byteout.writeByte(0x11); // HTV
			byteout.writeByte(0); // Ss
			byteout.writeByte(0x3f); // Se
			byteout.writeByte(0); // Bf
		}
		
		// Core processing
		internal var DU:Vector.<int> = new Vector.<int>(64, true);
		
		private function processDU(CDU:Vector.<Number>, fdtbl:Vector.<Number>, DC:Number, HTDC:Vector.<BitString>, HTAC:Vector.<BitString>):Number
		{
			var EOB:BitString = HTAC[0x00];
			var M16zeroes:BitString = HTAC[0xF0];
			var pos:int;
			const I16:int = 16;
			const I63:int = 63;
			const I64:int = 64;
			var DU_DCT:Vector.<int> = fDCTQuant(CDU, fdtbl);
			//ZigZag reorder
			for (var j:int=0;j<I64;++j) {
				DU[ZigZag[j]]=DU_DCT[j];
			}
			var Diff:int = DU[0] - DC; DC = DU[0];
			//Encode DC
			if (Diff==0) {
				writeBits(HTDC[0]); // Diff might be 0
			} else {
				pos = int(32767+Diff);
				writeBits(HTDC[category[pos]]);
				writeBits(bitcode[pos]);
			}
			//Encode ACs
			const end0pos:int = 63;
			for (; (end0pos>0)&&(DU[end0pos]==0); end0pos--) {};
			//end0pos = first element in reverse order !=0
			if ( end0pos == 0) {
				writeBits(EOB);
				return DC;
			}
			var i:int = 1;
			var lng:int;
			while ( i <= end0pos ) {
				var startpos:int = i;
				for (; (DU[i]==0) && (i<=end0pos); ++i) {}
				var nrzeroes:int = i-startpos;
				if ( nrzeroes >= I16 ) {
					lng = nrzeroes>>4;
					for (var nrmarker:int=1; nrmarker <= lng; ++nrmarker)
						writeBits(M16zeroes);
					nrzeroes = int(nrzeroes&0xF);
				}
				pos = int(32767+DU[i]);
				writeBits(HTAC[int((nrzeroes<<4)+category[pos])]);
				writeBits(bitcode[pos]);
				i++;
			}
			if ( end0pos != I63 ) {
				writeBits(EOB);
			}
			return DC;
		}
		
		private var YDU:Vector.<Number> = new Vector.<Number>(64, true);
		private var UDU:Vector.<Number> = new Vector.<Number>(64, true);
		private var VDU:Vector.<Number> = new Vector.<Number>(64, true);
		
		private function RGB2YUV(img:BitmapData, xpos:int, ypos:int):void
		{
			var pos:int=0;
			const I8:int = 8;
			for (var y:int=0; y<I8; ++y) {
				for (var x:int=0; x<I8; ++x) {
					var P:uint = img.getPixel32(xpos+x,ypos+y);
					var R:int = (P>>16)&0xFF;
					var G:int = (P>> 8)&0xFF;
					var B:int = (P    )&0xFF;
					YDU[int(pos)]=((( 0.29900)*R+( 0.58700)*G+( 0.11400)*B))-0x80;
					UDU[int(pos)]=(((-0.16874)*R+(-0.33126)*G+( 0.50000)*B));
					VDU[int(pos)]=((( 0.50000)*R+(-0.41869)*G+(-0.08131)*B));
					++pos;
				}
			}
		}
		
		public function JPEGEncoder(quality:int=50)
		{
			if (quality <= 0)
				quality = 1;
			
			if (quality > 100)
				quality = 100;
			
			sf = quality < 50 ? int(5000 / quality) : int(200 - (quality<<1));
			init();
		}
		
		private function init():void
		{
			ZigZag.fixed = true;
			aasf.fixed = true;
			YQT.fixed = true;
			UVQT.fixed = true;
			std_ac_chrominance_nrcodes.fixed = true;
			std_ac_chrominance_values.fixed = true;
			std_ac_luminance_nrcodes.fixed = true;
			std_ac_luminance_values.fixed = true;
			std_dc_chrominance_nrcodes.fixed = true;
			std_dc_chrominance_values.fixed = true;
			std_dc_luminance_nrcodes.fixed = true;
			std_dc_luminance_values.fixed = true;
			// Create tables
			initHuffmanTbl();
			initCategoryNumber();
			initQuantTables(sf);
		}
		
		public function encode(image:BitmapData):ByteArray
		{
			// Initialize bit writer
			byteout = new ByteArray();
			
			bytenew=0;
			bytepos=7;
			
			// Add JPEG headers
			byteout.writeShort(0xFFD8); // SOI
			writeAPP0();
			writeDQT();
			writeSOF0(image.width,image.height);
			writeDHT();
			writeSOS();
			
			// Encode 8x8 macroblocks
			var DCY:Number=0;
			var DCU:Number=0;
			var DCV:Number=0;
			bytenew=0;
			bytepos=7;
			
			var width:int = image.width;
			var height:int = image.height;
			
			for (var ypos:int=0; ypos<height; ypos+=8)
			{
				for (var xpos:int=0; xpos<width; xpos+=8)
				{
					RGB2YUV(image, xpos, ypos);
					DCY = processDU(YDU, fdtbl_Y, DCY, YDC_HT, YAC_HT);
					DCU = processDU(UDU, fdtbl_UV, DCU, UVDC_HT, UVAC_HT);
					DCV = processDU(VDU, fdtbl_UV, DCV, UVDC_HT, UVAC_HT);
				}
			}
			
			// Do the bit alignment of the EOI marker
			if ( bytepos >= 0 )
			{
				var fillbits:BitString = new BitString();
				fillbits.len = bytepos+1;
				fillbits.val = (1<<(bytepos+1))-1;
				writeBits(fillbits);
			}
			byteout.writeShort(0xFFD9); //EOI
			return byteout;
		}
	}
}